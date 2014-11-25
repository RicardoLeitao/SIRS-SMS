package ulisboa.tecnico.SIRSsms;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

import ulisboa.tecnico.SIRSsms.networking.LoadKey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ListView;

@SuppressLint("TrulyRandom")
public class InboxActivity extends Activity {
	private ListView lvMsg;
	@SuppressLint("TrulyRandom")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inbox_activity);
		List<SMS> smsList = new ArrayList<SMS>();
		lvMsg = (ListView) findViewById(R.id.smsListView);

		// Create Inbox box URI
		Uri inboxURI = Uri.parse("content://sms/inbox");

		// List required columns
		String[] reqCols = new String[] { "_id", "address", "body" };

		// Get Content Resolver object, which will deal with Content Provider
		ContentResolver cr = getContentResolver();

		// Fetch Inbox SMS Message from Built-in Content Provider
		Cursor c = cr.query(inboxURI, reqCols, null, null, null);

		try {
			PrivateKey pk = PKManager.get();

			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, pk);

			if(c.moveToFirst()) {
				for(int i=0; i < c.getCount(); i = i + 3) {
					SMS sms = new SMS();

					sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
					String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
					c.moveToNext();
					body += c.getString(c.getColumnIndexOrThrow("body")).toString();
					c.moveToNext();
					body += c.getString(c.getColumnIndexOrThrow("body")).toString();


					Log.d("body", body);
					byte[] encryptedBody = Base64.decode(body, Base64.DEFAULT);
					byte[] decryptedBody = cipher.doFinal(encryptedBody);
					String originalBody = new String(decryptedBody);

					sms.setBody(originalBody);
					smsList.add(sms);

					c.moveToNext();
				}
			}
			c.close();
			lvMsg.setAdapter(new SMSListAdapter(this, smsList));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
