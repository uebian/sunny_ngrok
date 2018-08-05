package net.newlydev.ngrok.activities;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.google.android.gms.ads.*;
import java.util.*;
import net.newlydev.ngrok.*;
import net.newlydev.ngrok.ngrok_core.*;
import org.json.*;

import android.support.v7.widget.Toolbar;
import net.newlydev.ngrok.R;

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
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewtunne);
		AdView adview=(AdView) findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().addTestDevice("27E31343F422BD0D601A6F9D3D438A95").build();
		AdRequest adRequest=new AdRequest.Builder().build();
        adview.loadAd(adRequest);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		if (!Utils.isMainServiceRunning(this))
		{
			startActivity(new Intent(this,MainActivity.class));
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
						// TODO: Implement this method
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
										switch(tunnel.getStatus())
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
									// TODO: Implement this method
								}
							});

						// TODO: Implement this method
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
		// TODO: Implement this method
		super.onDestroy();
		if (Utils.isMainServiceRunning(this))
		{
			unbindService(conn);
		}
	}

	public static interface updateListener
	{
		void onUpdate()
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("日志");
		menu.add("设置");
		menu.add("关于");
		menu.add("终止客户端");
		// TODO: Implement this method
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getTitle().toString())
		{
			case "日志":
				startActivity(new Intent(this,LogActivity.class));
				break;
			case "设置":
				startActivity(new Intent(this,SettingActivity.class));
				break;
			case "关于":
				startActivity(new Intent(this,AboutActivity.class));
				break;
			case "终止客户端":
				new AlertDialog.Builder(this).setTitle("警告").setMessage("这将会关闭所有的ngrok隧道，您确定继续吗？").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							unbindService(conn);
							stopService(new Intent(ViewTunneActivity.this, MainService.class));
							finish();
							// TODO: Implement this method
						}
					}).setNegativeButton("取消",null).show();
				
				break;
		}
		// TODO: Implement this method
		return super.onOptionsItemSelected(item);
	}
}
