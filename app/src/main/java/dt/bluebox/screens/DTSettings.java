package dt.bluebox.screens;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import dt.bluebox.Const;
import dt.bluebox.R;
import dt.bluebox.Utils;
import dt.bluebox.Vars;

/**
 * Created by Daniel on 2/26/17.
 */

public class DTSettings extends AppCompatActivity
{
	private static Context applicationContext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		applicationContext = getApplicationContext();
		getFragmentManager().beginTransaction().replace(R.id.settings_placeholder, new SettingsFragment()).commit();
	}

	public static class SettingsFragment  extends PreferenceFragment implements Preference.OnPreferenceChangeListener
	{
		private Preference portPicker, rotationPicker;

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(Const.PREFS_FILE);;
			getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);;
			addPreferencesFromResource(R.xml.settings);

			portPicker = findPreference(Const.PREF_PORT);
			portPicker.setOnPreferenceChangeListener(this);
			rotationPicker = findPreference(Const.PREF_ROTATE);
			rotationPicker.setOnPreferenceChangeListener(this);
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{
			if(preference == portPicker)
			{
				try
				{
					int newPort = Integer.valueOf((String) newValue);
					if (newPort > 1 && newPort < 65536)
					{
						Vars.port = newPort;
						return true;
					}
					else
					{
						Utils.showOk(applicationContext, getString(R.string.err_port_range));
						return false;
					}
				}
				catch (NumberFormatException n)
				{
					return false;
				}
			}
			else
			{
				Vars.rotatePeriod = Integer.valueOf((String)newValue);
			}
			return true;
		}
	}
}
