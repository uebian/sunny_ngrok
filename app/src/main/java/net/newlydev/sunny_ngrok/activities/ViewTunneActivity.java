package net.newlydev.sunny_ngrok.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import net.newlydev.sunny_ngrok.LogManager;
import net.newlydev.sunny_ngrok.MainService;
import net.newlydev.sunny_ngrok.R;
import net.newlydev.sunny_ngrok.Utils;
import net.newlydev.sunny_ngrok.ngrok_core.Tunnel;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewTunneActivity extends AppCompatActivity
{
	JSONArray data;
	JSONObject info;
	public static MainService service;
	ListView lv;
	MyConnection conn;
	public static TunneInfoActivity tunneInfoActivity;
	public static void setTunneInfoActivity(TunneInfoActivity tunneInfoActivity)
	{
		ViewTunneActivity.tunneInfoActivity = tunneInfoActivity;
	}
	ArrayList<String> tunns=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewtunne);
		final AdView adview=new AdView(this);
		((LinearLayout)findViewById(R.id.adLayout)).addView(adview);
		adview.setAdUnitId("ca-app-pub-4267459436057308/1519374118");
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notusesmartad", false))
		{
			adview.setAdSize(AdSize.BANNER);
		}
		else
		{
			adview.setAdSize(AdSize.SMART_BANNER);
		}
		AdRequest adRequest=new AdRequest.Builder().build();
        adview.loadAd(adRequest);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		if (!Utils.isMainServiceRunning(this))
		{
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		else
		{
			conn = new MyConnection();
			lv = (ListView) findViewById(R.id.lv_checktunne);
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tunns);  
			adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);  
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
					{
						Intent i=new Intent(ViewTunneActivity.this, TunneInfoActivity.class);
						i.putExtra("tuncount", p3);
						startActivity(i);
					}
				});
			bindService(new Intent(this, MainService.class), conn, Context.BIND_AUTO_CREATE);
		}
		//Toast.makeText(this,data.toString(),Toast.LENGTH_LONG);
	}
	private class MyConnection implements ServiceConnection
	{

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder)
		{

			final MainService.mBinder service = (MainService.mBinder) binder;
			ViewTunneActivity.this.service = service.service;
			service.service.setUpdateListener(new updateListener(){
					@Override
					public void onUpdate()
					{
						runOnUiThread(new Runnable(){
								@Override
								public void run()
								{
									if (tunneInfoActivity != null)
									{
										tunneInfoActivity.update();
									}
									tunns.clear();
									for (final Tunnel tunnel:service.service.tunns)
									{
										switch (tunnel.getStatus())
										{
											case 0:
												tunns.add(tunnel.getRemoteUrl() + "->" + tunnel.getLocalIP() + ":" + tunnel.getLocalPort());
												break;
											case 1:
												tunns.add(tunnel.getLocalIP() + ":" + tunnel.getLocalPort() + "(连接中)");
												break;
											case 2:
												tunns.add(tunnel.getLocalIP() + ":" + tunnel.getLocalPort() + "(错误，准备重连)");
												break;
											case 3:
												tunns.add(tunnel.getLocalIP() + ":" + tunnel.getLocalPort() + "(已关闭)");
												break;
										}
									}
									adapter.notifyDataSetChanged();
								}
							});
					}
				});
			service.service.notice_tunnel_update();
			//service.service.t

		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{

		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (Utils.isMainServiceRunning(this))
		{
			unbindService(conn);
		}
	}

	public static interface updateListener
	{
		void onUpdate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("日志");
		menu.add("设置");
		menu.add("关于");
		menu.add("终止客户端");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getTitle().toString())
		{
			case "日志":
				if (LogManager.getisopenlog())
				{
					startActivity(new Intent(this, LogActivity.class));
				}
				else
				{
					Toast.makeText(this, "日志功能未开启，请在设置中打开日志功能并重启应用(终止客户端后再开)", Toast.LENGTH_SHORT).show();
				}
				break;
			case "设置":
				startActivity(new Intent(this, SettingActivity.class));
				break;
			case "关于":
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case "终止客户端":
				new AlertDialog.Builder(this).setTitle("警告").setMessage("这将会关闭所有的ngrok隧道，您确定继续吗？").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							unbindService(conn);
							stopService(new Intent(ViewTunneActivity.this, MainService.class));
							finish();
						}
					}).setNegativeButton("取消", null).show();

				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
