package ulisboa.tecnico.SIRSsms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
public class HomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
    }
    
    public void sendSmsOnClickEvent(View view) {
    	Intent intent = new Intent(getApplicationContext(), SendSmsActivity.class);
        startActivity(intent);
    }
    public void inboxOnClickEvent(View view) {
    	Intent intent = new Intent(getApplicationContext(), InboxActivity.class);
        startActivity(intent);
    }
    
    
}
