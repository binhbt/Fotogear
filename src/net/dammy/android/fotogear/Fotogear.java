package net.dammy.android.fotogear;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dammy.android.fotogear.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.facebook.android.Facebook;

public class Fotogear {
	
	public static String FB_APPLICATION_ID = "148225862414";
	public static Facebook facebook = new Facebook(FB_APPLICATION_ID);
	private static DbAdapter _db;
	
	private static final int SUCCESS = 1;
	private static final int FAIL = 2;
	
	private static String fail_msg;
	
	public static FeedAdapter feedAdapter;
	public static List<FeedItem> timeline = Collections.emptyList();
	
	/*
	 * Gets FeedItem {photos} from facebook servers
	 * @params context 
	 * @params createdFrom The last date from which to get new pictures
	 * @params Older If true get items created before the createdFrom parameter
	 */
	public static List<FeedItem> getFacebookTimeLine(Context context, String createdFrom, boolean Older)
		throws FileNotFoundException, MalformedURLException, IOException, JSONException {
		_db = new DbAdapter(context);
		List<FeedItem> feedItems = new ArrayList<FeedItem>();
		String query = "SELECT owner, object_id, src_big, src_small, created FROM photo WHERE aid IN ( SELECT aid " +
						"FROM album WHERE owner IN  (SELECT uid2 FROM friend " +
						"WHERE uid1 = me() )) ORDER BY created DESC LIMIT 0, 15";
		if (createdFrom != "0"){
			query = "SELECT owner, object_id, src_big, src_small, created FROM photo WHERE aid IN ( SELECT aid " +
			"FROM album WHERE owner IN  (SELECT uid2 FROM friend " +
			"WHERE uid1 = me() ) and created > " + createdFrom + ") ORDER BY created";
			if (Older == true){
				query = "SELECT owner, object_id, src_big, src_small, created FROM photo WHERE aid IN ( SELECT aid " +
				"FROM album WHERE owner IN  (SELECT uid2 FROM friend " +
				"WHERE uid1 = me() ) and created < " + createdFrom + ") ORDER BY created LIMIT 0, 15";
			}
		}
		String result = Fotogear.facebook.request_fql(query);
		
		JSONArray jsonArray = null;
		try{
			jsonArray = new JSONArray(result);
		}catch(JSONException e){ return feedItems; }
		
		for (int i = 0; i < jsonArray.length(); i++){
			JSONObject ob = jsonArray.getJSONObject(i);
			FeedItem f = new FeedItem();
			f.setDateCreated(new java.util.Date(ob.getLong("created")*1000));
			f.setNetwork("facebook");
			f.setOwnerId(ob.getString("owner"));
			f.setPhotoBigSrc(ob.getString("src_big"));
			f.setPhotoSmallSrc(ob.getString("src_small"));
			f.setObjectId(ob.getString("object_id"));
			
			_db.open();
			String name = _db.getFacebookName(f.getOwnerId());
			if(name == ""){
				// get owner's fullname
				query = "SELECT name from user WHERE uid = " + f.getOwnerId();
				name = new JSONArray(Fotogear.facebook.request_fql(query)).getJSONObject(0).getString("name");
				_db.addFacebookName(name, f.getOwnerId());
			}
			_db.close();
			f.setOwnerName(name);
			feedItems.add(f);
		}
		return feedItems;
	}
	
	/*
	 * Like a facebook feedItem
	 */
	public static void likeItem(final Context context, final FeedItem item){
		final ProgressDialog dialog = ProgressDialog.show(context, "", "Processing...");
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
					case SUCCESS:{
						Toast.makeText(context, "You like this item",
								Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
					case FAIL:{
						Toast.makeText(context, fail_msg,
								Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				}
			}
		};
		
		new Thread(){
			@Override
			public void run(){
				//String graphPath = item.getObjectId() + "/likes";
				try {
					Bundle params = new Bundle();
					params.putString("method", "stream.addLike");
					params.putString("post_id", item.getObjectId());
					handler.sendEmptyMessage(SUCCESS);
				}catch(Exception e){
					fail_msg = e.getMessage();
					handler.sendEmptyMessage(FAIL);
				}
			}
		}.start();
	}
	
	/*
	 * Gets recent feedItems{photos} from facebook
	 */
	public static List<FeedItem> getFacebookTimeLine(Context context) 
	throws NumberFormatException, FileNotFoundException,
	MalformedURLException, JSONException, IOException {
		return getFacebookTimeLine(context, "0", false);
	}
	
	public static int refreshFeed(final Context context) 
	throws FileNotFoundException, MalformedURLException, IOException, JSONException {
		
		DbAdapter db = new DbAdapter(context);
		db.open();
		Fotogear.timeline = db.getFeedItems();
		
		List<FeedItem> feedItems = getFacebookTimeLine(context, db.getLastFeedItemDate(), false);
		int c = feedItems.size();
		
		// dump in db if any new
		if ( feedItems.size() != 0) {
			try{
				final List<FeedItem> _feedItems = new ArrayList<FeedItem>();
				_feedItems.addAll(feedItems);
				
				new Thread(){
					@Override
					public void run(){
						DbAdapter _db = new DbAdapter(context);
						_db.open();
						_db.addFeedItems(_feedItems);
						_db.close();
					}
				}.start();

				feedItems.addAll(Fotogear.timeline);
				Fotogear.timeline.clear();
				Fotogear.timeline.addAll(feedItems.subList(0, 15));
			}catch(Exception e){}
		}
		
		db.close();
		return c;
	}
	
	public static List<FeedItem> loadOlderFeed(Context context) 
	throws FileNotFoundException, MalformedURLException, IOException, JSONException{
		FeedItem lastItem = Fotogear.timeline.get(Fotogear.timeline.size() - 1);
		DbAdapter db = new DbAdapter(context);
		db.open();
		// check if older items in db are up to 15
		List<FeedItem> feedItems = db.getOlderFeedItems(lastItem);
		if (feedItems.size() >= 15){
			Fotogear.timeline.addAll(feedItems);
		}else{
			feedItems.clear();
			String lastdate = String.valueOf(lastItem.getDateCreated().getTime() / 1000);
			feedItems = getFacebookTimeLine(context, lastdate, true);
			if ( feedItems.size() != 0) {
				try{
					db.addFeedItems(feedItems);
					Fotogear.timeline.addAll(db.getOlderFeedItems(lastItem));
				}catch(Exception e){}
			}
		}
		db.close();
		return feedItems;
	}
}
