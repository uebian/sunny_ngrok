package net.newlydev.ngrok;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.appbar.*;

public class mSettingFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(Bundle p1, String p2)
	{
		AppBarLayout abl;
		setPreferencesFromResource(R.xml.setting_preference,p2);
		// TODO: Implement this method
	}
	
}
