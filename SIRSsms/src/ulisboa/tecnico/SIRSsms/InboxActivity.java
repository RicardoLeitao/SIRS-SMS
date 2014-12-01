package ulisboa.tecnico.SIRSsms;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
					String srcNumber = c.getString(c.getColumnIndexOrThrow("address")).toString();
					sms.setNumber(srcNumber);
					String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
					c.moveToNext();
					body += c.getString(c.getColumnIndexOrThrow("body")).toString();
					c.moveToNext();
					body += c.getString(c.getColumnIndexOrThrow("body")).toString();


					Log.d("body", body);
					String originalBody;
					try {
						byte[] encryptedBody = Base64.decode(body, Base64.DEFAULT);
						byte[] decryptedBody = cipher.doFinal(encryptedBody);
						
						byte[] messageBytes = new byte[decryptedBody.length - 2];
						byte[] hash = new byte[2];
						
						System.arraycopy(decryptedBody, 0, 
								messageBytes, 0, decryptedBody.length - 2);
						System.arraycopy(decryptedBody, decryptedBody.length - 2, 
								hash, 0,2);
						
						originalBody = new String(messageBytes);
						String stringToHash = srcNumber + originalBody;
						//Verify integrity with HMac
						Mac hmac = Mac.getInstance("HmacSHA256");
						hmac.init(new SecretKeySpec(PKManager.getHmackey(), "HmacSHA256"));
						
						byte[] signature = hmac.doFinal(stringToHash.getBytes());
						byte[] lastBlock = new byte[2];
						lastBlock[0] = signature[signature.length - 2];
						lastBlock[1] = signature[signature.length - 1];
						
						if(Base64.encodeToString(hash, Base64.DEFAULT).equals(
								Base64.encodeToString(lastBlock, Base64.DEFAULT)))
							Log.d("Integrity check","Yup it was not modified" + " = " +
									Base64.encodeToString(lastBlock, Base64.DEFAULT));
						else Log.d("Integrity check",Base64.encodeToString(hash, Base64.DEFAULT) + "!=" + 
								Base64.encodeToString(lastBlock, Base64.DEFAULT) + 
								" Hmm isto ja nao esta de acordo com o que devia ser");
						//
						
						Log.d("OriginalBody",originalBody + Base64.encodeToString(hash, Base64.DEFAULT) +
								Base64.encodeToString(lastBlock, Base64.DEFAULT));
					} catch(Exception e) {
						originalBody = "Could not decipher the message...";
					}

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
