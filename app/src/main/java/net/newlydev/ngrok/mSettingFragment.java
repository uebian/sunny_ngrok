package net.newlydev.ngrok;
import android.support.v7.preference.*;
import android.os.*;

public class mSettingFragment extends PreferenceFragmentCompat
{

	@Override
	public void onCreatePreferences(Bundle p1, String p2)
	{
		setPreferencesFromResource(R.xml.setting_preference,p2);
		// TODO: Implement this method
	}
	
}
