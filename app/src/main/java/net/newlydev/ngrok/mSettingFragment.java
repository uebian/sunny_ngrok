package net.newlydev.ngrok;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class mSettingFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(Bundle p1, String p2)
	{
		setPreferencesFromResource(R.xml.setting_preference,p2);
	}
	
}
