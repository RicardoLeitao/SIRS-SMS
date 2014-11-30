package ulisboa.tecnico.SIRSsms;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import android.annotation.SuppressLint;

public class RSAKeyPair {

	private PrivateKey privateKey;

	private PublicKey publicKey;

	@SuppressLint("TrulyRandom")
	public RSAKeyPair(int keyLength)throws Exception{
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

			kpg.initialize(keyLength);
			KeyPair kp = kpg.genKeyPair();

			privateKey = (RSAPrivateKey) kp.getPrivate();
			publicKey = (RSAPublicKey) kp.getPublic();

			PKManager.store(privateKey,publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final PrivateKey getPrivateKey() {
		return privateKey;
	}

	public final PublicKey getPublicKey() {
		return publicKey;
	}

}
