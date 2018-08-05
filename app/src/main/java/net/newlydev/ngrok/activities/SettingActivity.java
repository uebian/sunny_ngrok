package net.newlydev.ngrok.activities;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.preference.*;
import android.support.v7.widget.*;

public class SettingActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		//PreferenceFragmentCompat pfc;
		
	}
	
}
