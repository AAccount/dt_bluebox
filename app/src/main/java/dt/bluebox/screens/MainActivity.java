package dt.bluebox.screens;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.os.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import dt.bluebox.Const;
import dt.bluebox.background.PortListener;
import dt.bluebox.R;
import dt.bluebox.Utils;
import dt.bluebox.Vars;

public class MainActivity extends AppCompatActivity
{
	private TextView logview, currentLogName;
	private static final int MAXVIEW = 5, STORPERM = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		logview = (TextView)findViewById(R.id.main_logview);
		currentLogName = (TextView)findViewById(R.id.main_current_value);

		//get the settings
		SharedPreferences sharedPreferences = getSharedPreferences(Const.PREFS_FILE, MODE_PRIVATE);
		try
		{
			Vars.port = Integer.valueOf(sharedPreferences.getString(Const.PREF_PORT, ""));
			Vars.rotatePeriod = Integer.valueOf(sharedPreferences.getString(Const.PREF_ROTATE, ""));
		}
		catch (NumberFormatException n)
		{
			Vars.port = 0;
			Vars.rotatePeriod = 0;
		}

		//setup the intent and pending intent for log rotation. you'll need it sooner or late
		Utils.initRotatePending(getApplicationContext());
	}

	@Override
	public void onResume()
	{
		super.onResume();

		//if the settings are incomplete post a reminder
		if(Vars.port == 0 || Vars.rotatePeriod == 0)
		{
			Utils.showOk(this, getString(R.string.err_settings_incomplete));
		}

		//make sure there are storage permissions. otherwise nowhere to store the logs
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			//change the action bar
			Vars.storagePerm = false;

			android.app.AlertDialog.Builder mkdialog = new android.app.AlertDialog.Builder(this);
			mkdialog.setMessage(getString(R.string.main_activity_storage_perm))
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String[] perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
							ActivityCompat.requestPermissions(MainActivity.this, perms, STORPERM);
							dialog.cancel();
						}
					});
			android.app.AlertDialog showOkAlert = mkdialog.create();
			showOkAlert.show();
		}
		else
		{
			//for that weird case where you say no to the popup and then realize you needed to say yes so you
			//	do it by hand in the android settings --> app --> permissions
			Vars.storagePerm = true;
		}

		invalidateOptionsMenu();
	}

	@Override
	protected void onSaveInstanceState(Bundle save)
	{
		save.putInt(Const.PREF_PORT, Vars.port);
		save.putInt(Const.PREF_ROTATE, Vars.rotatePeriod);
		save.putBoolean(Const.SAVE_ISRUNNING, Vars.isPortListening);
		save.putBoolean(Const.SAVE_PERM, Vars.storagePerm);

		if(Vars.currentLog != null)
		{
			save.putString(Const.SAVE_LOGNAME, Vars.currentLog.getAbsolutePath());
		}
	}

	@Override
	protected void  onRestoreInstanceState(Bundle restore)
	{
		Vars.port = restore.getInt(Const.PREF_PORT);
		Vars.rotatePeriod = restore.getInt(Const.PREF_ROTATE);
		Vars.isPortListening = restore.getBoolean(Const.SAVE_ISRUNNING);
		Vars.storagePerm = restore.getBoolean(Const.SAVE_PERM);
		String oldLog = restore.getString(Const.SAVE_LOGNAME);

		if(Vars.currentLog == null && oldLog != null);
		{
			synchronized (Vars.currentLogLock)
			{
				Vars.currentLog = new File(restore.getString(Const.SAVE_LOGNAME));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_home, menu);

		//populate the options menu with the last 5 logs for quick viewing
		String path = Environment.getExternalStorageDirectory().toString() + "/" + Const.FOLDER_NAME;
		File folder = new File(path);

		//first check if the folder for storing logs exists
		boolean success = true; //default true in case it already exists
		if(!folder.exists())
		{
			success = folder.mkdir();
		}

		//populate the overflow menu with the 5 most recent logs for viewing
		if(success)
		{
			File savedLogs[] = folder.listFiles();
			if(savedLogs != null) //when there are no previous logs
			{
				//sort the files by newest to oldest
				//https://stackoverflow.com/questions/203030/best-way-to-list-files-in-java-sorted-by-date-modified
				Arrays.sort(savedLogs, new Comparator<File>()
				{
					@Override
					public int compare(File f1, File f2)
					{
						return (int)(f2.lastModified() - f1.lastModified());
					}
				});

				int max = Math.min(MAXVIEW, savedLogs.length);
				for (int i = 0; i < max; i++)
				{
					menu.add(savedLogs[i].getName());
				}
			}
		}
		else
		{
			logview.setText(getString(R.string.err_cant_mkdir));
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(Vars.rotatePeriod == 0 || Vars.port == 0 || !Vars.storagePerm)
		{
			menu.findItem(R.id.menu_home_start).setVisible(false);
			menu.findItem(R.id.menu_home_stop).setVisible(false);
			super.onPrepareOptionsMenu(menu);
			return true;
		}

		//show start and stop appropriately
		if(Vars.isPortListening)
		{
			menu.findItem(R.id.menu_home_start).setVisible(false);
			menu.findItem(R.id.menu_home_stop).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.menu_home_stop).setVisible(false);
			menu.findItem(R.id.menu_home_start).setVisible(true);
		}
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//for one of the preset menu items do the appropriate actions
		switch(item.getItemId())
		{
			case R.id.menu_home_start:
				if(!Vars.isPortListening) //make sure only 1 instance of port listener is running
				{
					startService(new Intent(this, PortListener.class));

					//setup the log rotation
					int rotateInMillis = Vars.rotatePeriod *60*60*1000;
					AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
					manager.cancel(Vars.pendingRotate);
					manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+rotateInMillis, Vars.pendingRotate);

					invalidateOptionsMenu(); //refresh the menu with start disabled
				}
				return true;
			case R.id.menu_home_stop:
				//cancel pending rotates
				AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
				manager.cancel(Vars.pendingRotate);

				//stop port listener
				stopService(new Intent(this, PortListener.class));

				invalidateOptionsMenu(); //enable start
				return true;
			case R.id.menu_home_settings:
				startActivity(new Intent(this, DTSettings.class));
				return true;
			case R.id.menu_home_refresh:
				synchronized (Vars.currentLogLock)
				{
					if (Vars.currentLog != null)
					{
						currentLogName.setText(Vars.currentLog.getName());
						dumpLog(Vars.currentLog.getAbsolutePath());
					}
				}
				return true;
		}

		//for one of the existing log files, dump its contents to the screen
		String logPath = Environment.getExternalStorageDirectory().toString() + "/" + Const.FOLDER_NAME + "/" + item.getTitle();
		dumpLog(logPath);
		return true;
	}

	private void dumpLog(String path)
	{
		try
		{
			File log = new File(path);
			if (log.exists()) //figure out if the log exists
			{
				String dump = "", line;
				BufferedReader logReader = new BufferedReader(new FileReader(log));
				while((line = logReader.readLine()) != null) //read it line by line
				{
					dump = dump + line + "\n";
				}
				logview.setText(dump);
			}
		}
		catch (Exception e)
		{
			logview.setText(e.getMessage());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch(requestCode)
		{
			case STORPERM:
			{
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
				{
					//app looses all function if you can't save any logs.
					Vars.storagePerm = false;
					invalidateOptionsMenu();
				}
			}
		}

	}
}
