package ulisboa.tecnico.SIRSsms;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ulisboa.tecnico.SIRSsms.networking.CaPublicKey;
import ulisboa.tecnico.SIRSsms.networking.DBConnector;
import ulisboa.tecnico.SIRSsms.networking.StoreKey;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PKManager.initialize(getBaseContext());
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void loginOnClickEvent(View view) 
    {
        EditText passwordET= (EditText)findViewById(R.id.passInput);
        TelephonyManager telemamanger = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String pwd = passwordET.getText().toString();
        String getSimSerialNumber = telemamanger.getLine1Number();
        TextView tv = (TextView)findViewById(R.id.textView1);
        new DBConnector(this,tv).execute(getSimSerialNumber,pwd,"login");
    }
    
    public void registerOnClickEvent(View view) 
    {
        EditText passwordET= (EditText)findViewById(R.id.passInput);
        TelephonyManager telemamanger = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String pwd = passwordET.getText().toString();
        String getSimSerialNumber = telemamanger.getLine1Number();
        TextView tv = (TextView)findViewById(R.id.textView1);


        try{
        	RSAKeyPair keyPair =  new RSAKeyPair(2048);
        	new DBConnector(this,tv).execute(getSimSerialNumber,pwd,"register",keyPair.getPublicKey().getEncoded().toString());
        	new StoreKey(this, getSimSerialNumber).execute(keyPair.getPublicKey());
        } catch(Exception e){ 
        	Toast.makeText(getBaseContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
    }
}
