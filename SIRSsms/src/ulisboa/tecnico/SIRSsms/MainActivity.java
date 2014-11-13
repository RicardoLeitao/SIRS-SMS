package ulisboa.tecnico.SIRSsms;

import ulisboa.tecnico.SIRSsms.networking.DBConnector;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new DBConnector(this,tv).execute(getSimSerialNumber,pwd,"register");
    }
}
