package dt.bluebox;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import dt.bluebox.background.LogRotateManager;
import dt.bluebox.background.PortListener;

/**
 * Created by Daniel on 2/25/17.
 */

public class Utils
{
	private static final String tag = "Utils";

	public static void initRotatePending(Context context)
	{
		if(Vars.pendingRotate == null || Vars.rotateLog == null)
		{
			Vars.rotateLog = new Intent(context, LogRotateManager.class);
			Vars.rotateLog.setAction(Const.ACTION_ROTATE);
			Vars.pendingRotate = PendingIntent.getBroadcast(context, Const.ACTION_ROTATE_ID, Vars.rotateLog, PendingIntent.FLAG_UPDATE_CURRENT);
		}
	}

	public static boolean isPortListenerRunning(Context context)
	{
		ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
		for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE))
		{
			if(PortListener.class.getName().equals(service.service.getClassName()))
			{
				return true;
			}
		}

		return false;
	}

	//creates a new log file with the timestamp as the name
	public static void createNewLog()
	{
		synchronized (Vars.currentLogLock)
		{
			String logName = Environment.getExternalStorageDirectory() + "/" + Const.FOLDER_NAME + "/log_" + Vars.simpleDateFormat.format(new Date());
			Vars.currentLog = new File(logName);
			try
			{
				Vars.currentLog.createNewFile();
			}
			catch (IOException i)
			{
				Log.e(tag, i.getMessage());
			}
		}
	}

	public static void showOk(Context context, String message)
	{
		AlertDialog.Builder mkdialog = new AlertDialog.Builder(context);
		mkdialog.setMessage(message)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.cancel();
					}
				});
		AlertDialog showOkAlert = mkdialog.create();
		showOkAlert.show();
	}
}
