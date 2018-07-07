package net.newlydev.ngrok;
import android.app.*;
import com.google.android.gms.ads.*;

public class mApplication extends Application
{

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		MobileAds.initialize(this,"ca-app-pub-4267459436057308~1713254732");
	}
	
}
