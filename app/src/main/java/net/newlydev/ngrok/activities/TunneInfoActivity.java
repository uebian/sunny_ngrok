package net.newlydev.ngrok.activities;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import net.newlydev.ngrok.R;
import net.newlydev.ngrok.ngrok_core.Tunnel;
import androidx.appcompat.widget.Toolbar;

public class TunneInfoActivity extends AppCompatActivity
{
	Button btn_start,btn_stop;
	Tunnel tunnel;
	TextView tv_status,tv_downloaddata,tv_uploaddata,tv_speed;
	ProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tunneinfo);
		final AdView adview=new AdView(this);
		((LinearLayout)findViewById(R.id.adLayout)).addView(adview);
		adview.setAdUnitId("ca-app-pub-4267459436057308/1136230739");
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notusesmartad",false))
		{
			adview.setAdSize(AdSize.BANNER);
		}else{
			adview.setAdSize(AdSize.SMART_BANNER);
		}
		AdRequest adRequest=new AdRequest.Builder().build();
        adview.loadAd(adRequest);
		pd=new ProgressDialog(this);
		pd.setMessage("请稍候...");
		pd.setCancelable(false);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_start.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					tunnel.open();
					pd.show();
					// TODO: Implement this method
				}
			});
		btn_stop = (Button) findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					tunnel.close();
					// TODO: Implement this method
				}
			});
		tv_status = (TextView) findViewById(R.id.tv_status);
		tv_status.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if(tunnel.getStatus()==2)
					{
						String error=tunnel.geterrmsg();
						new AlertDialog.Builder(TunneInfoActivity.this).setTitle("错误详情").setMessage(error).setPositiveButton("确定",null).show();
					}
					// TODO: Implement this method
				}
			});
		tv_downloaddata=(TextView) findViewById(R.id.tv_downloaddata);
		tv_uploaddata=(TextView) findViewById(R.id.tv_uploaddata);
		tv_speed=(TextView) findViewById(R.id.tv_speed);
		tunnel = ViewTunneActivity.service.tunns.get(getIntent().getExtras().get("tuncount"));
		setTitle("隧道详细:" + tunnel.getLocalIP() + ":" + tunnel.getLocalPort());
		ViewTunneActivity.setTunneInfoActivity(this);
		update();
	}
	public void update()
	{
		if(tunnel.getSpeed()==-1)
		{
			tv_speed.setText("未知");
		}else{
			tv_speed.setText(tunnel.getSpeed()+"ms");
		}
		if (tunnel.isOpen())
		{
			pd.dismiss();
			if (tunnel.getStatus()==0)
			{
				tv_status.setTextColor(Color.GREEN);
				tv_status.setText("连接成功");
			}
			else if (tunnel.getStatus()==2)
			{
				tv_status.setTextColor(Color.RED);
				tv_status.setText("出现错误，将在3秒后重连(点击此处查看错误详情)");
			}else{
				tv_status.setTextColor(Color.YELLOW);
				tv_status.setText("正在连接");
			}
			btn_start.setEnabled(false);
			btn_stop.setEnabled(true);
		}
		else
		{
			tv_status.setTextColor(Color.YELLOW);
			tv_status.setText("关闭");
			btn_stop.setEnabled(false);
			btn_start.setEnabled(true);
		}
		tv_downloaddata.setText(tunnel.getDownloadData()+"B");
		tv_uploaddata.setText(tunnel.getUploadData()+"B");
	}
	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		ViewTunneActivity.setTunneInfoActivity(null);
	}

}
