package ulisboa.tecnico.SIRSsms.networking;

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

import ulisboa.tecnico.SIRSsms.R;

public class CaPublicKey  extends Activity{
	
	private PublicKey publicKey;

	public CaPublicKey(Context ctx){
		try {
			AssetManager mngr = ctx.getResources().getAssets();
		    InputStream in = mngr.open("CAPUB.key");		
		    DataInputStream dis = new DataInputStream(in);
		    byte[] keyBytes = new byte[294];
		    dis.readFully(keyBytes);
		    dis.close();
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    publicKey =  kf.generatePublic(spec);
		    
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
