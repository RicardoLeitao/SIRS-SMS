package ulisboa.tecnico.SIRSsms.networking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import ulisboa.tecnico.SIRSsms.HomeActivity;
import ulisboa.tecnico.SIRSsms.MainActivity;
import ulisboa.tecnico.SIRSsms.R;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.webkit.WebView.FindListener;
import android.widget.TextView;
import android.widget.Toast;

public class DBConnector extends AsyncTask<String, Void, String> {

	private Context context;
	private TextView statusField;

	public DBConnector(Context context,TextView statusField) {
			      this.context = context;
			      this.statusField = statusField;
			   }
	
	@Override
	protected String doInBackground(String... arg0) {
		try {
			String username = (String) arg0[0];
			String password = (String) arg0[1];
			String tag = (String) arg0[2];
			String link = "http://web.ist.utl.pt/ist169632/index.php";
			String data = URLEncoder.encode("tag", "UTF-8") + "="
					+ URLEncoder.encode(tag, "UTF-8");
			data += "&" + URLEncoder.encode("number", "UTF-8") + "="
					+ URLEncoder.encode(username, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "="
					+ URLEncoder.encode(password, "UTF-8");
			
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(data);
			wr.flush();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			// Read Server Response
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				break;
			}
			return sb.toString();
		} catch (Exception e) {
			return new String("Exception: " + e.getMessage());
		}
	}
	
	@Override
	   protected void onPostExecute(String result){ 
		JSONObject jObject;
		try {
			jObject = new JSONObject(result);
			String tag = jObject.getString("tag");
			Integer errorCode = jObject.getInt("error");
			if(errorCode == 0){
				Toast.makeText(context,tag + " success",Toast.LENGTH_SHORT).show();
		        Intent intent = new Intent(context, HomeActivity.class);
		        context.startActivity(intent);
			}else if(errorCode == 2){ 
				Toast.makeText(context,tag + " success. Public Key replaced!",Toast.LENGTH_SHORT).show();
		        Intent intent = new Intent(context, HomeActivity.class);
		        context.startActivity(intent);
			}else{
				String errorMsg = jObject.getString("error_msg");
				Toast.makeText(context,tag + " fail." + errorMsg,Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	   }
}
