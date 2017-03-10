package dt.bluebox.background;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dt.bluebox.Const;
import dt.bluebox.Utils;
import dt.bluebox.Vars;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Daniel on 2/25/17.
 */

public class LogRotateManager extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if(action.equals(Const.ACTION_ROTATE))
		{
			//make the new log
			Utils.createNewLog();

			//set another rotation to go
			int rotateInMillis = Vars.rotatePeriod *60*60*1000;
			AlarmManager manager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
			manager.cancel(Vars.pendingRotate);
			manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+rotateInMillis, Vars.pendingRotate);
		}
	}
}
