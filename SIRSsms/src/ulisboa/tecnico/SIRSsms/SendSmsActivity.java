package ulisboa.tecnico.SIRSsms;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import ulisboa.tecnico.SIRSsms.networking.DBConnector;
import ulisboa.tecnico.SIRSsms.networking.LoadKey;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendSmsActivity extends Activity {
	EditText phoneNumber;
	EditText message;
	TextView textLimit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_sms_activity);
		phoneNumber = (EditText) findViewById(R.id.phoneNumberInput);
		phoneNumber.setText("1555521555");
		message = (EditText) findViewById(R.id.messageInput);
		textLimit = (TextView) findViewById(R.id.textLimit);
		
		message.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				textLimit.setText(message.length()+"/160");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
	       });
	}

	public void sendSmsOnClickEvent(View view) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		TelephonyManager telemamanger = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String srcNumber = telemamanger.getLine1Number();
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		
		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS sent",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS delivered",
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));
		
		SmsManager sms = SmsManager.getDefault();
		String encMessage = cipherMessage(srcNumber,phoneNumber.getText().toString(), message
				.getText().toString());
		ArrayList<String> parts = sms.divideMessage(encMessage);
		Log.d("debugparts",parts.toString());
		
		for(int i = parts.size() - 1; i >= 0; i--) {
			sms.sendTextMessage(phoneNumber.getText().toString(), null, parts.get(i), sentPI, deliveredPI);
		}
		
	}
	
	private String cipherMessage(String srcNumber, String dstNumber, String message){
		String encMessage = "";
		byte[] encBytes = null;
		String format = "%02d%02d%02d%02d";
		Calendar c = Calendar.getInstance();
		String timestamp = String.format(format, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		String timedMessage = message + timestamp;
		try {
			//Applies HMac with SHA256 to the body message
			String stringToHash = srcNumber + timedMessage;
			Mac hmac = Mac.getInstance("HmacSHA256");
			hmac.init(new SecretKeySpec(PKManager.getHmackey(), "HmacSHA256"));
			byte[] signature = hmac.doFinal(stringToHash.getBytes());
			
			//Last block for sha-256 has block_size = 512 last 4 hexs
			byte[] encBlock = new byte[2];
			encBlock[0] = signature[signature.length - 2];
			encBlock[1] = signature[signature.length - 1];
			Log.d("Hash",Base64.encodeToString(encBlock , Base64.DEFAULT));
			byte[] bytes = new byte[message.getBytes().length + 2];
			System.arraycopy(message.getBytes(), 0, bytes, 0, message.getBytes().length);
			System.arraycopy(encBlock, 0, bytes, message.getBytes().length, 2);
			
			//Ciphers hash with public key
			PublicKey pubKey = new LoadKey(this).execute(dstNumber).get();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			encBytes = cipher.doFinal(bytes);
			
			encMessage = Base64.encodeToString(encBytes , Base64.DEFAULT);
			
			Log.e("MSG", encMessage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encMessage;
	}
	
	
}
