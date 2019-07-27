package net.newlydev.sunny_ngrok;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

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
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
		{
			NotificationChannel notificationChannel=new NotificationChannel("0","状态", NotificationManager.IMPORTANCE_LOW);
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
		}
	}
	
}
