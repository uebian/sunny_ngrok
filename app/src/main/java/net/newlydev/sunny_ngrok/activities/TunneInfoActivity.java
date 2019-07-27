package net.newlydev.sunny_ngrok.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import net.newlydev.sunny_ngrok.R;
import net.newlydev.sunny_ngrok.Utils;
import net.newlydev.sunny_ngrok.ngrok_core.ProxyConnectMessageHandler;
import net.newlydev.sunny_ngrok.ngrok_core.Tunnel;

import androidx.appcompat.widget.Toolbar;

public class TunneInfoActivity extends AppCompatActivity {
    Button btn_start, btn_stop;
    Tunnel tunnel;
    TextView tv_status, tv_downloaddata, tv_uploaddata, tv_speed, tv_count;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunneinfo);
        final AdView adview = new AdView(this);
        ((LinearLayout) findViewById(R.id.adLayout)).addView(adview);
        adview.setAdUnitId("ca-app-pub-4267459436057308/1136230739");
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notusesmartad", false)) {
            adview.setAdSize(AdSize.BANNER);
        } else {
            adview.setAdSize(AdSize.SMART_BANNER);
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);
        pd = new ProgressDialog(this);
        pd.setMessage("请稍候...");
        pd.setCancelable(false);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_normal));
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                tunnel.open();
                pd.show();
            }
        });
        btn_stop = findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                tunnel.close();
                final ProgressDialog pd=new ProgressDialog(TunneInfoActivity.this);
                pd.setMessage("请稍候...");
                pd.setCancelable(false);
                pd.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                },3500);
            }
        });
        tv_status = findViewById(R.id.tv_status);
        tv_status.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                if (tunnel.getStatus() == 2) {
                    String error = tunnel.geterrmsg();
                    new AlertDialog.Builder(TunneInfoActivity.this).setTitle("错误详情").setMessage(error).setPositiveButton("确定", null).show();
                }
            }
        });
        tv_downloaddata = findViewById(R.id.tv_downloaddata);
        tv_uploaddata = findViewById(R.id.tv_uploaddata);
        tv_speed = findViewById(R.id.tv_speed);
        tv_count = findViewById(R.id.tv_count);
        tv_count.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tunnel.getStatus() == 0) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(TunneInfoActivity.this);
                    ab.setTitle("连线的客户端");
                    StringBuilder sb = new StringBuilder();
                    for (String clientAddr : tunnel.getControlConnectMessageHandler().getClientList()) {
                        sb.append(clientAddr + "\n");
                    }
                    ab.setMessage(sb.toString());
                    ab.show();
                }
            }
        });
        tunnel = MainActivity.service.tunns.get((int) getIntent().getExtras().get("tuncount"));
        setTitle("隧道详细:" + tunnel.getLocalIP() + ":" + tunnel.getLocalPort());
        MainActivity.setTunneInfoActivity(this);
        update();
    }

    public void update() {
        if (tunnel.getSpeed() == -1) {
            tv_speed.setText("未知");
        } else {
            tv_speed.setText(tunnel.getSpeed() + "ms");
        }
        if (tunnel.isOpen()) {
            pd.dismiss();
            if (tunnel.getStatus() == 0) {
                tv_status.setTextColor(Color.GREEN);
                tv_status.setText("连接成功");
            } else if (tunnel.getStatus() == 2) {
                tv_status.setTextColor(Color.RED);
                tv_status.setText("出现错误，将在3秒后重连(点击此处查看错误详情)");
            } else {
                tv_status.setTextColor(Color.YELLOW);
                tv_status.setText("正在连接");
            }
            btn_start.setEnabled(false);
            btn_stop.setEnabled(true);
        } else {
            tv_status.setTextColor(Color.YELLOW);
            tv_status.setText("关闭");
            btn_stop.setEnabled(false);
            btn_start.setEnabled(true);
        }
        tv_downloaddata.setText(Utils.getFormatSize(tunnel.getDownloadData()));
        tv_uploaddata.setText(Utils.getFormatSize(tunnel.getUploadData()));
        if (tunnel.getControlConnectMessageHandler() != null) {
            tv_count.setText("" + tunnel.getControlConnectMessageHandler().getClientList().size());
        } else {
            tv_count.setText("未知");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.setTunneInfoActivity(null);
    }

}
