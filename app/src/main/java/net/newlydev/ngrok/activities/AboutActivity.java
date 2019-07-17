package net.newlydev.ngrok.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import net.newlydev.ngrok.R;

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
