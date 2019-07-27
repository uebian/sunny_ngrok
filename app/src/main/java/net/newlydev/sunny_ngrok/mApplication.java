package net.newlydev.sunny_ngrok;
import android.app.Application;
import androidx.preference.PreferenceManager;
import com.google.android.gms.ads.MobileAds;

public class mApplication extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		MobileAds.initialize(this,"ca-app-pub-4267459436057308~1713254732");
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("uselog",false))
		{
			LogManager.openlog();
		}
		Utils.init(getApplicationContext());
	}
	
}
