package net.dammy.android.fotogear;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.dammy.android.fotogear.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class FotogearNotificationService extends Service {
	
	private Timer timer = new Timer();
	private static long UPDATE_INTERVAL = 60 * 60 * 1000; // 60 seconds
	private static long DELAY_INTERVAL = 0;
	private static final int NEW_PHOTOS = 1;
	DbAdapter db;
	
	private NotificationManager _notificationManager;
	private Notification _notification;

	@Override
	public void onCreate() {
		super.onCreate();
		this.startTask();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.killTask();
	}
	
	public void startTask(){
		timer.scheduleAtFixedRate(
				new TimerTask(){
					@Override
					public void run() {
						doTask();
					}
				}, DELAY_INTERVAL, UPDATE_INTERVAL);
	}
	
	public void doTask(){
		try {
			db = new DbAdapter(this);
			db.open();
			List<FeedItem> feedItems = Fotogear.getFacebookTimeLine(this, 
			db.getLastFeedItemDate(), false);
			db.close();
			int c = feedItems.size();
			if (c > 0){
				notifyUser(c);
			}
		} catch (Exception e){ }
	}
	
	public void killTask(){
		if( timer != null){
			timer.cancel();
		}
	}
	
	public void notifyUser(int photoCount){
		// Notify user
		String ns = Context.NOTIFICATION_SERVICE;
		_notificationManager = (NotificationManager) 
									this.getSystemService(ns);
		_notification = new Notification(R.drawable.ic_notification,
				"New facebook photos", 0);
		
		Context context = this.getApplicationContext();
		CharSequence contentTitle = "New facebook photos";
		CharSequence contentText = photoCount + " new photos have been uploaded";
		Intent notificationIntent = new Intent(this, Home.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		_notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		// pass to notification manager
		_notificationManager.notify(NEW_PHOTOS, _notification);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
