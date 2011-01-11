package net.dammy.android.fotogear;

import java.util.ArrayList;
import java.util.List;
import net.dammy.android.fotogear.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple Fotgear Database. Defines CRUD Operations for the tables on the 
 * database.
 */
public class DbAdapter {

	// Name of tables and columns
    public static final String FB_ACCOUNT_ACCESS_TOKEN = "access_token";
    public static final String FB_ACCOUNT_ID = "_id";
    private static final String FB_ACCOUNT = "fb_account";
    private static final String TAG = "FotogearDbAdapter";
    
    // Used to cache fb names and id
    private static final String FB_NAMES = "fb_names";
    private static final String FB_NAMES_USERID = "fb_user_id";
    private static final String FB_NAMES_NAME = "fb_name";
    
    private static final String FB_NAMES_CREATE = 
    	"create table fb_names (_id integer primary key autoincrement, "
        + "fb_user_id text not null, fb_name text not null);";
    
    // Used to cache feedItems
    private static final String FEED = "feed";
    private static final String FEED_OWNER_NAME = "owner_name";
    private static final String FEED_OWNER_ID = "owner_id";
    private static final String FEED_SRC_SMALL = "src_small";
    private static final String FEED_SRC_BIG = "src_big";
    private static final String FEED_NETWORK = "network";
    private static final String FEED_OBJECT_ID = "object_id";
    private static final String FEED_DATE_CREATED = "date_created";
    
    private static final String FEED_CREATE = 
    	"create table feed (_id integer primary key autoincrement," +
    	"owner_name text not null, owner_id text not null," +
    	"src_small text not null, src_big text not null," +
    	"object_id text not null," +
    	"network text not null, date_created text not null);";
    
    // Store User options
    private static final String OPTIONS = "options";
    private static final String OPTIONS_KEY = "key";
    private static final String OPTIONS_VALUE = "value";
    
    private static final String OPTIONS_CREATE = 
    	"create table options (_id integer primary key autoincrement," +
    	"key text not null, value text not null);";
    
    private DatabaseHelper _dbHelper;
    private SQLiteDatabase _db;
    private final Context _context;

    // Table creation SQL statements
    private static final String FB_ACCOUNT_CREATE =
        "create table fb_account (_id integer primary key autoincrement, "
        + "access_token text not null);";

    private static final String DATABASE_NAME = "Fotogear";
    private static final int DATABASE_VERSION = 2;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FB_ACCOUNT_CREATE);
            db.execSQL(FB_NAMES_CREATE);
            db.execSQL(FEED_CREATE);
            db.execSQL(OPTIONS_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS fb_account");
            db.execSQL("DROP TABLE IF EXISTS fb_names");
            db.execSQL("DROP TABLE IF EXISTS feed");
            db.execSQL("DROP TABLE IF EXISTS options");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this._context = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        _dbHelper = new DatabaseHelper(_context);
        _db = _dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        _dbHelper.close();
    }

    public long addFacebookAccount(String access_token) {
    	// delete existing accounts
    	try{
    		_db.execSQL("delete from fb_account");
    	}catch(Exception e){ /* Do nothing */}
    	
    	// add new account
        ContentValues initialValues = new ContentValues();
        initialValues.put(FB_ACCOUNT_ACCESS_TOKEN, access_token);

        return _db.insert(FB_ACCOUNT, null, initialValues);
    }
    
    public String getFacebookAccount(){
    	try{
    		String sql = "select " + FB_ACCOUNT_ACCESS_TOKEN + " from " + FB_ACCOUNT ; 
	    	Cursor cursor = _db.rawQuery(sql, null);
	    	cursor.moveToFirst();
	    	String account = cursor.getString(cursor.getColumnIndex(FB_ACCOUNT_ACCESS_TOKEN));
	    	cursor.close();
	    	return account;
    	}catch(Exception e){
    		return "";
    	}
    }
    
    
    /*
     * Add a name and user Id to database
     */
    public long addFacebookName(String name, String fbId){
    	ContentValues args = new ContentValues();
    	args.put(FB_NAMES_NAME, name);
    	args.put(FB_NAMES_USERID, fbId);
    	return _db.insert(FB_NAMES, null, args);
    }
    
    /*
     * Get the name of facebook user or return empty string
     * if name is not cached
     */
    public String getFacebookName(String fbId){
    	try{
    		String sql = "select " + FB_NAMES_USERID + ", " + FB_NAMES_NAME +
    						" from " + FB_NAMES + " where " + FB_NAMES_USERID 
    						+ "=" + fbId;
	    	Cursor cursor = _db.rawQuery(sql, null);
	    	cursor.moveToFirst();
	    	String name =  cursor.getString(cursor.getColumnIndex(FB_NAMES_NAME));
	    	cursor.close();
	    	return name;
    	}catch(Exception e){
    		return "";
    	}
    }
   
    /*
     * Add feedItems to database
     */
    public void addFeedItems(List<FeedItem> items){
    	for(FeedItem i : items){
    		ContentValues args = new ContentValues();
    		args.put(FEED_OWNER_NAME, i.getOwnerName());
    		args.put(FEED_OWNER_ID, i.getOwnerId());
    		args.put(FEED_SRC_SMALL, i.getPhotoSmallSrc());
    		args.put(FEED_SRC_BIG, i.getPhotoBigSrc());
    		args.put(FEED_NETWORK, i.getNetwork());
    		args.put(FEED_OBJECT_ID, i.getObjectId());
    		long epoch = i.getDateCreated().getTime() / 1000;
    		args.put(FEED_DATE_CREATED, String.valueOf(epoch));
    		_db.insert(FEED, null, args);
    	}
    }
    
    /*
     * Get feed items that are stored in database
     * @params page The page to get from database
     */
    public List<FeedItem> getFeedItems(int page){
    	List<FeedItem> feedItems = new ArrayList<FeedItem>();
    	/*
    	Cursor cursor = _db.query(FEED, 
    			new String[] { FEED_OWNER_NAME, FEED_OWNER_ID, FEED_SRC_SMALL, 
    			FEED_SRC_BIG, FEED_NETWORK, FEED_DATE_CREATED, FEED_OBJECT_ID }, null, null, 
    			null, null, FEED_DATE_CREATED + " " + "DESC", String.valueOf(((page - 1) * 10)) + ",15");
    	*/
    	String sql = "Select * from " + FEED + " order by " + 
    				FEED_DATE_CREATED + " desc limit " + 
    				String.valueOf(((page - 1) * 10)) + ",15";
    	Cursor cursor = _db.rawQuery(sql, null);
    	cursor.moveToFirst();
    	while(cursor.isAfterLast() == false){
    		FeedItem f = new FeedItem();
    		f.setOwnerId(cursor.getString(cursor.getColumnIndex(FEED_OWNER_ID)));
    		f.setOwnerName(cursor.getString(cursor.getColumnIndex(FEED_OWNER_NAME)));
    		f.setPhotoBigSrc(cursor.getString(cursor.getColumnIndex(FEED_SRC_BIG)));
    		f.setPhotoSmallSrc(cursor.getString(cursor.getColumnIndex(FEED_SRC_SMALL)));
    		f.setNetwork(cursor.getString(cursor.getColumnIndex(FEED_NETWORK)));
    		f.setObjectId(cursor.getString(cursor.getColumnIndex(FEED_OBJECT_ID)));
    		f.setDateCreated(new java.util.Date(
    				Long.parseLong(cursor.getString(
    						cursor.getColumnIndex(FEED_DATE_CREATED)))*1000));
    		feedItems.add(f);
    		cursor.moveToNext();
    	}
    	cursor.close();
    	return feedItems;
    }
    
    /*
     * Gets feedItems that are older than the passed feedItem
     */
    public List<FeedItem> getOlderFeedItems(FeedItem item){
    	List<FeedItem> feedItems = new ArrayList<FeedItem>();
    	String lastDate = String.valueOf(item.getDateCreated().getTime() / 1000);
    	/* Cursor cursor = _db.query(FEED, 
    			new String[] { FEED_OWNER_NAME, FEED_OWNER_ID, FEED_SRC_SMALL, 
    			FEED_SRC_BIG, FEED_NETWORK, FEED_DATE_CREATED, FEED_OBJECT_ID }, FEED_DATE_CREATED + " < " + lastDate , null, 
    			null, null, FEED_DATE_CREATED + " " + "DESC", "0,15"); */
    	
    	String sql = "Select * from " + FEED + " where " + FEED_DATE_CREATED 
    				+ " < " + lastDate  + " order by " + 
					FEED_DATE_CREATED + " desc limit 0,15";
    	Cursor cursor = _db.rawQuery(sql, null);
    	cursor.moveToFirst();
    	while(cursor.isAfterLast() == false){
    		FeedItem f = new FeedItem();
    		f.setOwnerId(cursor.getString(cursor.getColumnIndex(FEED_OWNER_ID)));
    		f.setOwnerName(cursor.getString(cursor.getColumnIndex(FEED_OWNER_NAME)));
    		f.setPhotoBigSrc(cursor.getString(cursor.getColumnIndex(FEED_SRC_BIG)));
    		f.setPhotoSmallSrc(cursor.getString(cursor.getColumnIndex(FEED_SRC_SMALL)));
    		f.setNetwork(cursor.getString(cursor.getColumnIndex(FEED_NETWORK)));
    		f.setObjectId(cursor.getString(cursor.getColumnIndex(FEED_OBJECT_ID)));
    		f.setDateCreated(new java.util.Date(
    				Long.parseLong(cursor.getString(
    						cursor.getColumnIndex(FEED_DATE_CREATED)))*1000));
    		feedItems.add(f);
    		cursor.moveToNext();
    	}
    	cursor.close();
    	return feedItems;
    }
    
    public String getLastFeedItemDate(){
    	try{
    		/*
	    	Cursor cursor = _db.query(FEED, 
	    			new String[] { FEED_DATE_CREATED }, null, null, 
	    			null, null, FEED_DATE_CREATED + " " + "DESC","0,1");
	    			*/
    		String sql = "select " + FEED_DATE_CREATED + " from " + FEED + 
    					" order by " + FEED_DATE_CREATED + " desc limit 0,1";
    		Cursor cursor = _db.rawQuery(sql, null);
	    	cursor.moveToFirst();
	    	String lastDate = cursor.getString(cursor.getColumnIndex(FEED_DATE_CREATED));
	    	cursor.close();
	    	return lastDate;
    	}catch(Exception e){
    		return "0";
    	}
    }
    
    public String getNotificationOption(){
    	try{
    		String sql = "select " + OPTIONS_VALUE + " from " + OPTIONS +
    					" where " + OPTIONS_KEY + "='notification';";
    		Cursor cursor = _db.rawQuery(sql, null);
    		cursor.moveToFirst();
    		String notificationSetting = cursor.getString(cursor.getColumnIndex(OPTIONS_VALUE));
    		return notificationSetting;
    	}catch(Exception e){
    		return "0";
    	}
    }
    
    public void setNotificationOption(String setting){
    	// delete any existing notification setting
    	try{
    		_db.execSQL("delete from options where key='notification';");
    	}catch(Exception e){}
    	
    	ContentValues args = new ContentValues();
    	args.put("key", "notification");
    	args.put("value", setting);
    	_db.insert(OPTIONS, null, args);
    }
    
    public void deleteAccount(){
    	// delete existing accounts
    	try{
    		_db.execSQL("delete from fb_account;");
    		_db.execSQL("delete from fb_feed;");
    		_db.execSQL("delete from feed;");
    		_db.execSQL("delete from options");
    	}catch(Exception e){}
    }
    
    public List<FeedItem> getFeedItems(){
    	return getFeedItems(1);
    }
}