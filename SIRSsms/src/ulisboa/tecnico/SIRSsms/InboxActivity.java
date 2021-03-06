package ulisboa.tecnico.SIRSsms;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
	@SuppressLint({ "TrulyRandom", "SimpleDateFormat" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inbox_activity);
		List<SMS> smsList = new ArrayList<SMS>();
		lvMsg = (ListView) findViewById(R.id.smsListView);
		HashMap<String,PublicKey> publicKeys = new HashMap<String,PublicKey>();

		// Create Inbox box URI
		Uri inboxURI = Uri.parse("content://sms/inbox");

		// List required columns
		String[] reqCols = new String[] { "_id", "address", "body", "date" };

		// Get Content Resolver object, which will deal with Content Provider
		ContentResolver cr = getContentResolver();

		// Fetch Inbox SMS Message from Built-in Content Provider
		Cursor c = cr.query(inboxURI, reqCols, null, null, null);

		try {
			if(c.moveToFirst()) {
				for(int i=0; i < c.getCount(); i++) {
					SMS sms = new SMS();
					String srcNumber = c.getString(c.getColumnIndexOrThrow("address")).toString();
					sms.setNumber(srcNumber);
					String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
					
					if(body.startsWith(":encryptedsms:")) {
						//The encrypted correspond to 3 messages
						body = body.replace(":encryptedsms:", "");
						c.moveToNext();
						body += c.getString(c.getColumnIndexOrThrow("body")).toString();
						c.moveToNext();
						body += c.getString(c.getColumnIndexOrThrow("body")).toString();

						i += 2; // increments cursor
						
						//Gets the timestamp from the header
						Long ms = c.getLong(c.getColumnIndexOrThrow("date"));
						DateFormat format = new SimpleDateFormat("ddMMHHmm");
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(ms);
						String timestamp = format.format(calendar.getTime());

						Log.d("body", body);
						String originalBody;
						try {
							byte[] encryptedBody = Base64.decode(body, Base64.DEFAULT);

							// Decripts with private
							Cipher cipher = Cipher.getInstance("RSA");
							cipher.init(Cipher.DECRYPT_MODE, PKManager.get());
							byte[] decryptedWithPrivate = cipher.doFinal(encryptedBody);

							// Decripts with the source's public key
							PublicKey pubKey;
							// verifies if the public key has already been fetched
							if(publicKeys.containsKey(srcNumber))
								pubKey = publicKeys.get(srcNumber);
							else {
								pubKey = new LoadKey(this).execute(srcNumber).get();
								publicKeys.put(srcNumber, pubKey);
							}
							
							cipher.init(Cipher.DECRYPT_MODE, pubKey);
							byte[] decryptedBody = cipher.doFinal(decryptedWithPrivate);

							byte[] messageBytes = new byte[decryptedBody.length - 4];
							byte[] hash = new byte[4];

							System.arraycopy(decryptedBody, 0, 
									messageBytes, 0, decryptedBody.length - 4);
							System.arraycopy(decryptedBody, decryptedBody.length - 4, 
									hash, 0,4);

							originalBody = new String(messageBytes);
							String stringToHash = srcNumber + originalBody + timestamp;
							//Verify integrity with HMac
							Mac hmac = Mac.getInstance("HmacSHA256");
							hmac.init(new SecretKeySpec(PKManager.getHmackey(), "HmacSHA256"));

							byte[] signature = hmac.doFinal(stringToHash.getBytes());
							byte[] lastBlock = new byte[4];
							lastBlock[0] = signature[signature.length - 4];
							lastBlock[1] = signature[signature.length - 3];
							lastBlock[2] = signature[signature.length - 2];
							lastBlock[3] = signature[signature.length - 1];

							if(Base64.encodeToString(hash, Base64.DEFAULT).equals(
									Base64.encodeToString(lastBlock, Base64.DEFAULT)))
								Log.d("Integrity check","Yup it was not modified" + " = " +
										Base64.encodeToString(lastBlock, Base64.DEFAULT));
							else {Log.d("Integrity check",Base64.encodeToString(hash, Base64.DEFAULT) + "!=" + 
									Base64.encodeToString(lastBlock, Base64.DEFAULT) + 
									" Hmm isto ja nao esta de acordo com o que devia ser");
							originalBody = "[Warning: This message could have been changed]" + originalBody;
							}
							//

							Log.d("OriginalBody",originalBody + Base64.encodeToString(hash, Base64.DEFAULT) +
									Base64.encodeToString(lastBlock, Base64.DEFAULT));
						} catch(Exception e) {
							originalBody = "Could not decipher the message...";
						}
						sms.setBody(originalBody);
					}
					else sms.setBody(body);
					
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
