package net.dammy.android.fotogear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import net.dammy.android.fotogear.R;

public class FotogearSettings extends Activity {

	DbAdapter db = new DbAdapter(this);
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.settings);
		
		this.doNotificationsButtonSetUp();
		
		Button btnDelete = (Button) this.findViewById(R.id.settingsAccount);
		btnDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				new AlertDialog.Builder(FotogearSettings.this)
					.setTitle("Account").setMessage("Delete Account?")
					.setNegativeButton("No", null)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DbAdapter _db = new DbAdapter(FotogearSettings.this);
							_db.open();
							_db.deleteAccount();
							_db.close();
							
							Intent i = new Intent(FotogearSettings.this, Home.class);
							FotogearSettings.this.startActivity(i);
						}}
					).show();
			}
			
		});
	}
	
	public void doNotificationsButtonSetUp(){
		final Button btnNotifications = (Button) this.findViewById(R.id.settingsNotifications);
		btnNotifications.setText("Enable Notifications");
		btnNotifications.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DbAdapter _db = new DbAdapter(FotogearSettings.this);
				_db.open();
				_db.setNotificationOption("1");
				_db.close();
				// Enable notification service
				Intent i = new Intent(FotogearSettings.this, FotogearNotificationService.class);
				FotogearSettings.this.startService(i);
				
				doNotificationsButtonSetUp();
				Toast.makeText(FotogearSettings.this, "Notifications have been enabled", Toast.LENGTH_LONG).show();
			}
			
		});
		
		db.open();
		String notificationOption = db.getNotificationOption();
		if (notificationOption.equals("1")){
			btnNotifications.setText("Disable Notifications");
			btnNotifications.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					DbAdapter _db = new DbAdapter(FotogearSettings.this);
					_db.open();
					_db.setNotificationOption("0");
					_db.close();
					
					Intent i = new Intent(FotogearSettings.this, FotogearNotificationService.class);
					FotogearSettings.this.stopService(i);
					
					doNotificationsButtonSetUp();
					Toast.makeText(FotogearSettings.this, "Notifications have been disabled", Toast.LENGTH_LONG).show();
				}
				
			});
		}
		db.close();
	}
}