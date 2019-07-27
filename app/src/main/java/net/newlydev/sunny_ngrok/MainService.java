package net.newlydev.sunny_ngrok;
import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;
import org.json.*;
import net.newlydev.sunny_ngrok.ngrok_core.*;
import net.newlydev.sunny_ngrok.activities.*;

public class MainService extends Service
{
	public ArrayList<Tunnel> tunns=new ArrayList<Tunnel>();
	private MainActivity.updateListener listener;
	private static boolean isServiceRunning=false;

	private Notification.Builder builder;
	public void setUpdateListener(MainActivity.updateListener listener)
	{
		this.listener = listener;
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
		builder = new Notification.Builder(this);
		builder.setContentTitle("Ngrok客户端服务运行中");
		builder.setContentText("点击管理");
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setOngoing(true);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
			builder.setChannelId("0");
		}

	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		isServiceRunning=false;
		new Thread()
		{
			@Override
			public void run()
			{
				for (Tunnel tunnel:tunns)
				{
					tunnel.close();
				}
			}
		}.start();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		//((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, builder.build());
		startForeground(1,builder.build());
		isServiceRunning=true;
		ArrayList<String> authdatastr=intent.getExtras().getStringArrayList("authdata");
		for (int i=0;i < authdatastr.size();i++)
		{
			try
			{
				JSONObject authData=new JSONObject(authdatastr.get(i));
				JSONObject real_data=(JSONObject) authData.getJSONArray("data").get(0);
				String tcporhttp=real_data.getJSONObject("proto").keys().next();
				Tunnel tunnel = new Tunnel(authData.getString("server").split(":")[0], Integer.parseInt(authData.getString("server").split(":")[1]), 
										   real_data.getJSONObject("proto").getString(tcporhttp).split(":")[0], Integer.parseInt(real_data.getJSONObject("proto").getString(tcporhttp).split(":")[1]), tcporhttp, real_data.getString("subdomain"), real_data.getString("hostname"), authData.getJSONArray("data").getJSONObject(0).getInt("remoteport"), real_data.getString("httpauth"), MainService.this,authData.getString("sunnyid"));
				tunns.add(tunnel);
				tunnel.open();
			}
			catch (JSONException e)
			{}
		}
		new Handler().postDelayed(new Runnable(){
				@Override
				public void run()
				{
					notice_tunnel_update();
				}
			}, 500);
		//{"status":200,"msg":"获取隧道成功","server":"free.ngrok.cc:4443","data":[{"remoteport":19420,"subdomain":"","hostname":"","httpauth":"","proto":{"tcp":"127.0.0.1:80"}}]}

		return super.onStartCommand(intent, flags, startId);
	}
	public void notice_tunnel_update()
	{
		if (listener != null)
		{
			listener.onUpdate();
		}
	}
	public static boolean checkIsServiceRunning()
	{
		return isServiceRunning;
	}
	@Override
	public IBinder onBind(Intent p1)
	{
		return new mBinder();
	}
	public class mBinder extends Binder
	{
		public MainService service=MainService.this;
	}
}
