package dt.bluebox;

/**
 * Created by Daniel on 2/25/17.
 */

public class Const
{
	public static final String self = "dt.bluebox";

	//preference file stuff
	public static final String PREFS_FILE = "bluebox_prefs";
	public static final String PREF_ROTATE = "rotate_time";
	public static final String PREF_PORT = "port";

	//action rotate pending intent
	public static final String ACTION_ROTATE = "dt.bluebox.action_rotate";
	public static final int ACTION_ROTATE_ID = 194222; //time this was created (see created by date)


	public static final String FOLDER_NAME = "bluebox";
	public static final int BUFFER_SIZE = 1000;

	//save instance, restore instance stuff for vars
	public static final String SAVE_ISRUNNING = "is_port_listener_running";
	public static final String SAVE_PERM = "storage_permission";
	public static final String SAVE_LOGNAME = "log_file_name";
}
