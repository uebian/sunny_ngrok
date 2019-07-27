package net.newlydev.sunny_ngrok;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import net.newlydev.sunny_ngrok.R;

public class mSettingFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(Bundle p1, String p2)
	{
		setPreferencesFromResource(R.xml.setting_preference,p2);
		EditTextPreference bufSize=findPreference("bufSize");
		EditTextPreference timeOut=findPreference("timeOut");
		bufSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference p1, Object p2)
			{
				boolean ok=true;
				try{
					int i=Integer.parseInt(p2.toString());
					if(i<=0)
					{
						ok=false;
					}
				}catch(NumberFormatException e)
				{
					ok=false;
				}
				if(!ok)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
					ab.setTitle("更改未保存");
					ab.setCancelable(false);
					ab.setPositiveButton("确定",null);
					ab.setMessage("您的输入有误");
					ab.show();
					return false;
				}
				return true;
			}
		});
		timeOut.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference p1, Object p2)
			{
				boolean ok=true;
				try{
					int i=Integer.parseInt(p2.toString());
					if(i<0)
					{
						ok=false;
					}
				}catch(NumberFormatException e)
				{
					ok=false;
				}
				if(!ok)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
					ab.setTitle("更改未保存");
					ab.setCancelable(false);
					ab.setPositiveButton("确定",null);
					ab.setMessage("您的输入有误");
					ab.show();
					return false;
				}
				return true;
			}
		});
	}
	
}
