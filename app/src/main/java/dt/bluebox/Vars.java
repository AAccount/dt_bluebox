package dt.bluebox;

import android.app.PendingIntent;
import android.content.Intent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Daniel on 2/25/17.
 */

public class Vars
{
	public static File currentLog = null;
	public static final Object currentLogLock = new Object();
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM_dd_yyyy_HH_mm_ss", Locale.US);


	public static int port = 0;
	public static int rotatePeriod = 0;
	public static boolean storagePerm = true;

	public static PendingIntent pendingRotate;
	public static Intent rotateLog;
}
