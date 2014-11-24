package ulisboa.tecnico.SIRSsms;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import android.annotation.SuppressLint;

public class RSAKeyPair {

	private PrivateKey privateKey;

	private PublicKey publicKey;

	@SuppressLint("TrulyRandom")
	public RSAKeyPair(int keyLength)throws Exception{
		if(!PKManager.has()) {
			try {
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				KeyFactory fact = KeyFactory.getInstance("RSA");

				kpg.initialize(keyLength);
				KeyPair kp = kpg.genKeyPair();

				privateKey = (RSAPrivateKey) kp.getPrivate();
				publicKey = (RSAPublicKey) kp.getPublic();
				
				RSAPrivateKeySpec priv = fact.getKeySpec(privateKey,
						RSAPrivateKeySpec.class);
				PKManager.store(priv.getModulus(), priv.getPrivateExponent());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			throw new Exception("Device is already Registered.");
		}
	}

	public final PrivateKey getPrivateKey() {
		return privateKey;
	}

	public final PublicKey getPublicKey() {
		return publicKey;
	}

}
