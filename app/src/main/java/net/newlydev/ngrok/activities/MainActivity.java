package net.newlydev.ngrok.activities;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.google.android.gms.ads.*;
import java.util.*;
import net.newlydev.ngrok.*;
import net.newlydev.ngrok.ngrok_core.*;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View.OnClickListener;
import net.newlydev.ngrok.R;

public class MainActivity extends AppCompatActivity
{
	private EditText et_clientid;
	private Button btn_start;
	private Handler callback;
	private ArrayAdapter adapter;
	private Button btn_add;
	private ArrayList<String> clientidlist=new ArrayList<String>();
	private ListView lv_addtunne;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		AdView adview=(AdView) findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().addTestDevice("27E31343F422BD0D601A6F9D3D438A95").build();
		AdRequest adRequest=new AdRequest.Builder().build();
        adview.loadAd(adRequest);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		if(Utils.isMainServiceRunning(this))
		{
			finish();
			startActivity(new Intent(this,TunneRunningActivity.class));
		}
		et_clientid = (EditText) findViewById(R.id.et_clientid);
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_start.setEnabled(false);
		lv_addtunne = (ListView) findViewById(R.id.lv_addtunne);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, clientidlist);  
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);  
		lv_addtunne.setAdapter(adapter);
		btn_add = (Button) findViewById(R.id.btn_add);
		btn_add.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					boolean okfalg=true;
					String clientid=et_clientid.getText().toString();
					if(clientid.equals(""))
					{
						okfalg=false;
					}
					for (char a:clientid.toCharArray())
					{
						if (!((a >= '0' && a <= '9') || (a >= 'a' && a <= 'f')))
						{
							okfalg = false;
							new AlertDialog.Builder(MainActivity.this).setTitle("错误").setMessage("不能识别您的输入").setPositiveButton("确定", null).show();
							break;
						}
					}
					for(String yclientid:clientidlist)
					{
						if(yclientid.equals(clientid))
						{
							okfalg=false;
							new AlertDialog.Builder(MainActivity.this).setTitle("错误").setMessage("重复添加的项目").setPositiveButton("确定", null).show();
							break;
						}
					}
					if (okfalg)
					{
						et_clientid.setText("");
						clientidlist.add(clientid);
						adapter.notifyDataSetChanged();
						if(clientidlist.size()!=0)
						{
							btn_start.setEnabled(true);
						}else{
							btn_start.setEnabled(false);
						}
					}
					// TODO: Implement this method
				}
			});
		final ProgressDialog pd=new ProgressDialog(this);
		lv_addtunne.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					clientidlist.remove(p3);
					adapter.notifyDataSetChanged();
					if(clientidlist.size()!=0)
					{
						btn_start.setEnabled(true);
					}else{
						btn_start.setEnabled(false);
					}
					// TODO: Implement this method
				}
				
			});
		pd.setCancelable(false);
		callback = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				String errmsg="";
				int type=msg.getData().getInt("type");
				if (type == 0)
				{
					int n=0;
					pd.dismiss();
					final ArrayList<String> data=msg.getData().getStringArrayList("data");
					for (int i=0;i < data.size();i++)
					{
						if (data.get(i).startsWith("err:"))
						{
							errmsg = errmsg + "==========\n隧道" + clientidlist.get(i) + "没有通过验证\n错误信息:" + data.get(i).substring(4) + "\n\n";
							n++;
						}
					}if (errmsg.equals(""))
					{
						//final JSONObject authData=new JSONObject(msg.getData().getString("data"));
						Intent i=new Intent(MainActivity.this, MainService.class);
						i.putExtra("authdata", data);
						startService(i);
						finish();
						startActivity(new Intent(MainActivity.this, TunneRunningActivity.class));
					}
					else
					{
						new AlertDialog.Builder(MainActivity.this).setTitle("发生了" + n + "个错误").setMessage(errmsg).setCancelable(false).setPositiveButton("忽略这些错误，继续", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									ArrayList<String> okdata=new ArrayList<String>();
									for(String tmp:data)
									{
										if(!tmp.startsWith("err:"))
										{
											okdata.add(tmp);
										}
									}
									if(okdata.size()==0)
									{
										new AlertDialog.Builder(MainActivity.this).setTitle("错误").setMessage("没有通过验证的隧道").setPositiveButton("确定",null).show();
									}else{
										Intent i=new Intent(MainActivity.this, MainService.class);
										i.putExtra("authdata", okdata);
										startService(i);
										finish();
										startActivity(new Intent(MainActivity.this, TunneRunningActivity.class));
									}
									// TODO: Implement this method
								}
							}).setNegativeButton("重新输入", null).show();
					}
				}
				else if (type == 1)
				{
					pd.setMessage("正在核对信息...(第" + msg.getData().getInt("progress") + "条，共" + clientidlist.size() + "条");
				}
			}
		};
		btn_start.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					pd.setMessage("正在核对信息...");
					pd.show();
					final AlertDialog.Builder errordlg=new AlertDialog.Builder(MainActivity.this);
					errordlg.setCancelable(false);
					errordlg.setPositiveButton("确定", null);
					try
					{
						new AuthThread(clientidlist, callback).start();
						// TODO: Implement this method
					}
					catch (Exception e)
					{
						runOnUiThread(new Runnable(){

								@Override
								public void run()
								{
									pd.dismiss();
									errordlg.setMessage("您的输入似乎不正确，请检查后重试。");
									errordlg.show();
									// TODO: Implement this method
								}
							});
					}}
			});

    }
}
