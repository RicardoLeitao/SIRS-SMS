package ulisboa.tecnico.SIRSsms.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import ulisboa.tecnico.SIRSsms.R;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class LoadKey extends AsyncTask<String, Void, PublicKey>{
	
	private Context context;
	
	

	public LoadKey(Context context) {
		super();
		this.context = context;
	}



	@Override
	protected PublicKey doInBackground(String... params) {
		PublicKey pKey = null;
		try{

			byte[] data = new byte[16384];
			String number = params[0];
			String host = "sigma.ist.utl.pt";
			String user = context.getResources().getString(R.string.user);
			String pass = context.getResources().getString(R.string.pass);
			String downloadPath = "/afs/ist.utl.pt/users/"+user.substring(7, 8)+"/"+user.substring(8, 9)+"/"+user+"/SIRSSMSKEYS";
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
			channelSftp.cd(downloadPath);
			InputStream is = channelSftp.get(number+".key");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int nread;
			while((nread = is.read(data, 0, data.length)) != -1)
				baos.write(data, 0, nread);
			baos.flush();
			X509EncodedKeySpec spec =
				      new X509EncodedKeySpec(baos.toByteArray());
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    pKey = kf.generatePublic(spec);
		} catch(Exception e){Log.e("FTP", e.getMessage());}
		return pKey;
	}

}
