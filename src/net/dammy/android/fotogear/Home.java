package net.dammy.android.fotogear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import net.dammy.android.fotogear.R;

public class Home extends Activity implements DialogListener {
	
	private static final int FINISH = 0;
	private static final int FAIL = 1;
	private String fail_msg = "";
	private int new_item_count = 0;
	
	private boolean _firstUsage = false;
	
	private DbAdapter _dbAdapter = new DbAdapter(this);
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        // Check if user already has a connected facebook account
        _dbAdapter.open();
        String access_token = _dbAdapter.getFacebookAccount();
        _dbAdapter.close();
        if( access_token != "" ){
        	Fotogear.facebook.setAccessToken(access_token);
        	Fotogear.facebook.setAccessExpires(0);
        	Fotogear.facebook.setAppId(Fotogear.FB_APPLICATION_ID);
        	loadFeed();
        }
        
        Button btnFacebook = (Button) this.findViewById(R.id.btnFacebook);
        btnFacebook.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String[] perms = {"user_photos","friends_photos","offline_access"};
				Fotogear.facebook.authorize(Home.this, perms , Home.this);
			}
        	
        });
    }
    
    /*
    public void doLoadFeed(boolean showProgress){
    	if (showProgress == true){
	    	loadFeed();
	    	dialog.dismiss();
    	}else{
    		loadFeed();
    	}
    }
    */
    
    /*
     * Loads photofeed using a non-ui thread
     */
    public void loadFeed(){

    	final ProgressDialog dialog = ProgressDialog.show(this,"", "Loading Feed...");
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
					case FINISH:{
						loadFeedSuccessUI();
						if (_firstUsage == true){
							// first usage 
							// Enable notifications by default
							
							Intent i = new Intent(Home.this, FotogearNotificationService.class);
							Home.this.startService(i);
							
							_dbAdapter.open();
							_dbAdapter.setNotificationOption("1");
							_dbAdapter.close();
						}
						dialog.dismiss();
						break;
					}
					case FAIL:{
						loadFailUI();
						dialog.dismiss();
						break;
					}
				}
			}
		};
		
		new Thread(){
			@Override
			public void run(){
				try{
					new_item_count = Fotogear.refreshFeed(Home.this);
					handler.sendEmptyMessage(FINISH);
				}catch(Exception e){
					fail_msg = e.getMessage();
					handler.sendEmptyMessage(FAIL);
				}
			}
		}.start();
	}
	
    /*
     * Simple method to load timeLine
     * after loadfeed has completed
     */
	private void loadFeedSuccessUI(){
		// Pass message with amount of new updates to new activity
    	Fotogear.feedAdapter = new FeedAdapter(this, Fotogear.timeline);
    	Intent i = new Intent(this, TimeLine.class);
    	if (!_firstUsage){
	    	if (new_item_count == 0){
	    		i.putExtra("refresh_msg", "There are no new Items");
	    	}else{
	    		i.putExtra("refresh_msg", new_item_count + " new uploads");
	    	}
    	}
    	this.startActivity(i);
    }
	
	private void loadFailUI(){
		if( Fotogear.timeline.size() == 0){
			// going no where
			Toast.makeText(Home.this,fail_msg,Toast.LENGTH_LONG).show();
		}else {
			// load with cached items 
			// send unable to update message
			Fotogear.feedAdapter = new FeedAdapter(this, Fotogear.timeline);
	    	Intent i = new Intent(this, TimeLine.class);
	    	i.putExtra("refresh_msg", "Unable to talk to facebook at this time");
	    	this.startActivity(i);
		}
	}
	
    /*
     * (non-Javadoc)
     * @see com.facebook.android.Facebook.DialogListener#onCancel()
     * User clicks cancel on facebook dialog
     */
	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "You canceled facebook authorization", Toast.LENGTH_LONG).show();
	}

	/*
	 * (non-Javadoc)
	 * @see com.facebook.android.Facebook.DialogListener#onComplete(android.os.Bundle)
	 */
	@Override
	public void onComplete(Bundle values) {
		_dbAdapter.open();
		_dbAdapter.addFacebookAccount(Fotogear.facebook.getAccessToken());
		_dbAdapter.close();
		
		_firstUsage = true;
		
		loadFeed();
	}

	/*
	 * (non-Javadoc)
	 * @see com.facebook.android.Facebook.DialogListener#onError(com.facebook.android.DialogError)
	 */
	@Override
	public void onError(DialogError e) {
		// TODO Auto-generated method stub
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.facebook.android.Facebook.DialogListener#onFacebookError(com.facebook.android.FacebookError)
	 */
	@Override
	public void onFacebookError(FacebookError e) {
		// TODO Auto-generated method stub
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
	}
}