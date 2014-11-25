package ulisboa.tecnico.SIRSsms.networking;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;

import ulisboa.tecnico.SIRSsms.R;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class StoreKey extends AsyncTask<PublicKey, String, Void> {
	private String number;
	private Context context;
	
    public StoreKey(Context context, String number){
    	this.number = number;
    	this.context = context;
    }
    

	@Override
	protected Void doInBackground(PublicKey... params) {
		
    	try{
    		String host = "sigma.ist.utl.pt";
    		String user = context.getResources().getString(R.string.user);
    		String pass = context.getResources().getString(R.string.pass);
    		String uploadPath = "/afs/ist.utl.pt/users/"+user.substring(7, 8)+"/"+user.substring(8, 9)+"/"+user+"/SIRSSMSKEYS";
    		JSch jsch = new JSch();
    		Session session = jsch.getSession(user, host);
    		session.setPassword(pass);
    		java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications",
					"publickey,keyboard-interactive,password");
			session.setConfig(config);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp) channel;
			channelSftp.cd(uploadPath);
			InputStream is = new ByteArrayInputStream(params[0].getEncoded());
			channelSftp.put(is, number+".key");
    	} catch(Exception e){Log.e("FTP", e.getMessage());}
		return null;
	}

}
