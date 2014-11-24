package ulisboa.tecnico.SIRSsms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import android.content.Context;

public abstract class PKManager {

	public static Context ctx = null;

	public static void initialize(Context context) {
		ctx = context;
	}

	public static boolean initialized() {
		if(ctx == null)
			return true;
		return false;
	}

	public static PrivateKey get() throws Exception {
		InputStream in = ctx.openFileInput("SIRSsmsPK");
		ObjectInputStream oin =
				new ObjectInputStream(new BufferedInputStream(in));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(keySpec);
			return privKey;
		} catch (Exception e) {
			throw e;
		} finally {
			oin.close();
		}
	}

	public static void store(BigInteger mod, BigInteger exp) throws Exception{
		ObjectOutputStream oout = new ObjectOutputStream(
				new BufferedOutputStream(ctx.openFileOutput("SIRSsmsPK", Context.MODE_PRIVATE)));
		try{
			oout.writeObject(mod);
			oout.writeObject(exp);
			oout.close();
		} catch (Exception e) {
			throw e;
		}
	}

	public static boolean has() {
		try{
			ctx.openFileInput("SIRSsmsPK");
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

}
