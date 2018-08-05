package net.newlydev.ngrok.activities;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.widget.*;
import com.google.android.gms.ads.*;
import java.util.*;
import net.newlydev.ngrok.*;

import android.support.v7.widget.Toolbar;
import net.newlydev.ngrok.R;
import android.view.View.*;
import android.view.*;

public class LogActivity extends AppCompatActivity
{
	private int page=1;
	private ArrayAdapter adapter;
	ArrayList<String> logs=new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		
		AdView adview=(AdView) findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().addTestDevice("27E31343F422BD0D601A6F9D3D438A95").build();
		AdRequest adRequest=new AdRequest.Builder().build();
        adview.loadAd(adRequest);
		ListView lv=(ListView) findViewById(R.id.lv_log);
		
		adapter = new ArrayAdapter<String>(this, R.layout.list_log,logs);  
		Button refresh=(Button) findViewById(R.id.btn_refresh);
		Button prepage=(Button) findViewById(R.id.btn_prepage);
		prepage.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					page--;
					update();
					// TODO: Implement this method
				}
			});
		Button nxtpage=(Button) findViewById(R.id.btn_nexpage);
		nxtpage.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					page++;
					update();
					// TODO: Implement this method
				}
			});
		refresh.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					update();
					// TODO: Implement this method
				}
			});
		//adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);  
		lv.setAdapter(adapter);
		update();
	}
	private void update()
	{
		logs.clear();
		int start=(page-1)*20;
		if(start>=LogManager.getLogs().size())
		{
			page--;
			update();
			return;
		}
		if(start<0)
		{
			page++;
			update();
			return;
		}
		for(int i=start;i<Math.min(start+20,LogManager.getLogs().size());i++)
		{
			LogManager.Log log=LogManager.getLogs().get(i);
			logs.add(String.format("[%s][%d-%d-%d %d:%d:%d.%d][%d][%s]%s",log.type,log.year,log.month,log.day,log.hour,log.minute,log.second,log.millisecoud,log.threadid,log.tunnelid,log.msg));
		}
		adapter.notifyDataSetChanged();
	}
}
