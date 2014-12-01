package ulisboa.tecnico.SIRSsms;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import android.content.Context;

public abstract class PKManager {

	private static final String BC = BouncyCastleProvider.PROVIDER_NAME;
	private static final String FILENAME = ".PK";
	public static final byte[] hmacKey = "d6cfaad283353507".getBytes();
	public static Context ctx = null;
	public static char[] passWord = null;

	private static X509Certificate genCertificate(PrivateKey pk, PublicKey pubKey) throws OperatorCreationException, 
	CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());
		// Generate self-signed certificate
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		X509Certificate cert;

		builder.addRDN(BCStyle.CN, "SIGMA");

		long currentTime = System.currentTimeMillis();
		Date notBefore = new Date(currentTime);
		Date notAfter = new Date(currentTime - (2 * 365 * 24 * 60 * 60 * 1000));
		BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

		X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
				builder.build(), serial, notBefore, notAfter, builder.build(),
				pubKey);
		ContentSigner sigGen = new JcaContentSignerBuilder(
				"SHA256WithRSAEncryption").setProvider(BC).build(
						pk);

		cert = new JcaX509CertificateConverter().setProvider(BC)
				.getCertificate(certGen.build(sigGen));
		cert.checkValidity(new Date());
		cert.verify(cert.getPublicKey());

		return cert;
	}

	public static void initialize(Context context) {
		ctx = context;
	}
	
	public static void initialize(Context context, char[] pass) {
		ctx = context;
		passWord = pass;
	}

	public static boolean initialized() {
		if(ctx == null)
			return true;
		return false;
	}

	public static void store(PrivateKey pk, PublicKey pubKey) throws InvalidKeyException, 
	OperatorCreationException, CertificateException, NoSuchAlgorithmException, 
	NoSuchProviderException, SignatureException, KeyStoreException, IOException {
		X509Certificate cert = genCertificate(pk, pubKey);

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null);

		ks.setKeyEntry("PrivKey", pk, passWord,
				new java.security.cert.Certificate[] { cert });
		
		FileOutputStream fos = null;
		try {
			fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			ks.store(fos, passWord);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	public static PrivateKey get() throws Exception, KeyStoreException, NoSuchAlgorithmException, CertificateException, 
	IOException, UnrecoverableKeyException {
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		
		FileInputStream fis = null;
		try {
			fis = ctx.openFileInput(FILENAME);
			ks.load(fis, passWord);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		if(ks.getKey("PrivKey", null) instanceof PrivateKey)
			return (PrivateKey)ks.getKey("PrivKey", passWord);
		throw new Exception("The key with alias \"PrivKey\" is not a private key!");
	}
	
	public static Context getCtx() {
		return ctx;
	}

	public static void setCtx(Context ctx) {
		PKManager.ctx = ctx;
	}

	public static char[] getPassWord() {
		return passWord;
	}

	public static void setPassWord(char[] passWord) {
		PKManager.passWord = passWord;
	}

	public static byte[] getHmackey() {
		return hmacKey;
	}

}
