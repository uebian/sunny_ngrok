package net.newlydev.ngrok.activities;

import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import net.newlydev.ngrok.*;

import android.support.v7.widget.Toolbar;

public class AboutActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
		((Button)findViewById(R.id.btn_openoffwebsite)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Uri uri = Uri.parse("https://www.ngrok.cc");
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					// TODO: Implement this method
				}
			});
		((Button)findViewById(R.id.btn_sourcecode)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Uri uri = Uri.parse("https://github.com/uebian/sunny_ngrok");
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					// TODO: Implement this method
				}
			});
		try
		{
			((TextView)findViewById(R.id.tv_ver)).setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		}
		catch (PackageManager.NameNotFoundException e)
		{}
	}

}
