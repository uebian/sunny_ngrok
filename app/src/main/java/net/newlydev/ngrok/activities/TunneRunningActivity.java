package net.newlydev.ngrok.activities;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.util.*;
import net.newlydev.ngrok.*;
import net.newlydev.ngrok.ngrok_core.*;
import org.json.*;

import android.support.v7.widget.Toolbar;

public class TunneRunningActivity extends AppCompatActivity
{
	JSONArray data;
	JSONObject info;
	public static MainService service;
	ListView lv;
	MyConnection conn;
	public static TunneInfoActivity tunneInfoActivity;
	public static void setTunneInfoActivity(TunneInfoActivity tunneInfoActivity)
	{
		TunneRunningActivity.tunneInfoActivity = tunneInfoActivity;
	}
	ArrayList<String> tunns=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managetunne);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		if (!Utils.isMainServiceRunning(this))
		{
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
						Intent i=new Intent(TunneRunningActivity.this, TunneInfoActivity.class);
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
			TunneRunningActivity.this.service = service.service;
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
										if (tunnel.getStatus().startsWith("info:"))
										{
											tunns.add(tunnel.getLocalIP() + ":" + tunnel.getLocalPort() + tunnel.getStatus().substring(5));
										}
										else if (tunnel.getStatus().startsWith("err:"))
										{
											tunns.add(tunnel.getLocalIP() + ":" + tunnel.getLocalPort() + "(错误，准备重连)");
										}
										else if (tunnel.getStatus().startsWith("succ:"))
										{
											tunns.add(tunnel.getStatus().substring(5) + "->" + tunnel.getLocalIP() + ":" + tunnel.getLocalPort());
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
		menu.add("结束所有隧道");
		// TODO: Implement this method
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getTitle().toString())
		{
			case "结束所有隧道":
				unbindService(conn);
				stopService(new Intent(TunneRunningActivity.this, MainService.class));
				finish();
				break;
		}
		// TODO: Implement this method
		return super.onOptionsItemSelected(item);
	}
}
