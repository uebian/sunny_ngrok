package net.newlydev.sunny_ngrok.activities;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import net.newlydev.sunny_ngrok.R;

public class SettingActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar_normal));
	}
	
}
