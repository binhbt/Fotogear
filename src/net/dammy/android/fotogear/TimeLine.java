package net.dammy.android.fotogear;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import net.dammy.android.fotogear.R;

public class TimeLine extends ListActivity {
	
	private static final int SUCCESS = 0;
	private static final int FAIL = 1;
	private String fail_msg;
	private int refresh_count = 0;
	private int last_position = Fotogear.timeline.size() - 1;
	
	private static final int REFRESH = 3;
	private static final int SETTINGS = 4;
	private static final int LOAD_MORE = 5;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.timeline);
		
		this.setListAdapter(Fotogear.feedAdapter);
		Bundle extras = this.getIntent().getExtras();
		
		try{
			String msg = extras.getString("refresh_msg");
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		} catch (Exception e){ }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, REFRESH, 0, "Refresh").setIcon(R.drawable.ic_menu_refresh);
		menu.add(0, LOAD_MORE, 0, "Load More").setIcon(R.drawable.ic_menu_more);
		menu.add(0, SETTINGS, 0, "Settings").setIcon(R.drawable.ic_menu_settings);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// super.onListItemClick(l, v, position, id);
		viewPhoto(position);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case REFRESH:{
				this.refreshFeed();
				break;
			}
			
			case LOAD_MORE:{
				this.loadMore();
				break;
			}
			
			case SETTINGS:{
				Intent i = new Intent(this, FotogearSettings.class);
				this.startActivity(i);
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void viewPhoto(int position){
		Intent i = new Intent(this, ViewPhoto.class);
		i.putExtra("position", position);
		this.startActivity(i);
	}

	public void refreshFeed(){
			final ProgressDialog dialog = 
				ProgressDialog.show(this,"", "Refreshing Feed...");
			final Handler handler = new Handler(){
				@Override
				public void handleMessage(Message msg){
					switch(msg.what){
						case SUCCESS:{
							dialog.dismiss();
							String message = "There are no new photos";
							if ( refresh_count != 0 ){
								Fotogear.feedAdapter.notifyDataSetChanged();
								message = refresh_count + " new photos";
							}
							Toast.makeText(TimeLine.this, message,
									Toast.LENGTH_LONG).show();
							break;
						}
						
						case FAIL:{
							dialog.dismiss();
							Toast.makeText(TimeLine.this,fail_msg,
									Toast.LENGTH_LONG).show();
							break;
						}
					}
				}
			};
			
			new Thread(){
				@Override
				public void run(){
					try {
						refresh_count = Fotogear.refreshFeed(TimeLine.this);
						handler.sendEmptyMessage(SUCCESS);
					} catch (Exception e) {
						fail_msg = e.getMessage();
						handler.sendEmptyMessage(FAIL);
					}
				}
			}.start();
		}
	 
	public void loadMore(){
	 	final ProgressDialog dialog = 
		 		ProgressDialog.show(this, "", "Loading More Photos...");
	 	final Handler handler = new Handler(){
 			@Override
	 		public void handleMessage(Message msg){
	 			switch(msg.what){
	 				case SUCCESS:{
		 				dialog.dismiss();
		 				Fotogear.feedAdapter.notifyDataSetChanged();
		 				TimeLine.this.setSelection(last_position);
		 				last_position = Fotogear.timeline.size() - 1;
		 				break;
	 				}
	 				case FAIL:{
	 					dialog.dismiss();
						Toast.makeText(TimeLine.this,fail_msg,
								Toast.LENGTH_LONG).show();
						break;
	 				}
	 			}
	 		}
	 	};
	 	
	 	new Thread(){
	 		@Override
	 		public void run(){
	 			try {
					Fotogear.loadOlderFeed(TimeLine.this);
					handler.sendEmptyMessage(SUCCESS);
				} catch (Exception e) {
					fail_msg = e.getMessage();
					handler.sendEmptyMessage(FAIL);
				}
	 		}
	 	}.start();
	 }
}