package net.newlydev.sunny_ngrok.activities;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import net.newlydev.sunny_ngrok.LogManager;
import net.newlydev.sunny_ngrok.R;

public class LogActivity extends AppCompatActivity
{
	private int page=1;
	private ArrayAdapter adapter;
	ArrayList<String> logs=new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		
		final AdView adview=new AdView(this);
		((LinearLayout)findViewById(R.id.adLayout)).addView(adview);
		adview.setAdUnitId("ca-app-pub-4267459436057308/8504026771");
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notusesmartad",false))
		{
			adview.setAdSize(AdSize.BANNER);
		}else{
			adview.setAdSize(AdSize.SMART_BANNER);
		}
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
				}
			});
		Button nxtpage=(Button) findViewById(R.id.btn_nexpage);
		nxtpage.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					page++;
					update();
				}
			});
		refresh.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					update();
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
