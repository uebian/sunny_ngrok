package net.newlydev.ngrok;
import android.app.*;
import com.google.android.gms.ads.*;
import android.support.v7.preference.*;

public class mApplication extends Application
{

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		MobileAds.initialize(this,"ca-app-pub-4267459436057308~1713254732");
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("uselog",false))
		{
			LogManager.openlog();
		}
	}
	
}
