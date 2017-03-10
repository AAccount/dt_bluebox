package dt.bluebox.background;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import dt.bluebox.Const;
import dt.bluebox.Utils;
import dt.bluebox.Vars;

/**
 * Created by Daniel on 2/25/17.
 */

public class PortListener extends IntentService
{
	private static final String tag = "PortListener";
	public PortListener()
	{
		super(tag);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{

		//setup the log file for the first time
		Utils.createNewLog();

		try
		{
			//establish the tcp listening socket
			ServerSocket serverSocket = new ServerSocket(Vars.port);

			//in an infinite loop, wait for new connections, read them, save it to the log file
			while(true)
			{
				try
				{
					Socket clientSocket = serverSocket.accept();
					InputStream clientIn = clientSocket.getInputStream();
					int readLength;
					byte[] rawBuffer = new byte[Const.BUFFER_SIZE];

					while(true) //keep reading from the socket until it's gone
					{
						readLength = clientIn.read(rawBuffer);
						if(readLength < 0)
						{//something went wrong, this log session is now over
							break;
						}
						synchronized (Vars.currentLogLock)
						{
							FileWriter logWrite = new FileWriter(Vars.currentLog, true);
							logWrite.write(new String(rawBuffer, 0, readLength));
							logWrite.close();
						}
					}
				}
				catch (Exception e)
				{
					//client socket is unreadable. nothing you can do, nothing worth saying
				}
			}
		}
		catch (IOException i)
		{
			Log.e(tag, i.getMessage());
		}
	}
}