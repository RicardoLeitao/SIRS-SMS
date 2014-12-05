package ulisboa.tecnico.SIRSsms.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;
import android.util.Log;

import ulisboa.tecnico.SIRSsms.R;

public class CaPublicKey  extends Activity{
	
	private PublicKey publicKey;

	public CaPublicKey(Context ctx){
		try {
			byte[] keyBytes = new byte[294];
			AssetManager mngr = ctx.getResources().getAssets();
		    InputStream in = mngr.open("public_key.dem");	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int nread;
			while((nread = in.read(keyBytes, 0, keyBytes.length)) != -1)
				baos.write(keyBytes, 0, nread);
			baos.flush();
			X509EncodedKeySpec spec = new X509EncodedKeySpec(baos.toByteArray());
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    publicKey = kf.generatePublic(spec);   
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PublicKey getCAKey(){
		return publicKey;
	}
}