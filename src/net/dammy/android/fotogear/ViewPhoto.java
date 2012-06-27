package net.dammy.android.fotogear;

import java.io.File;
import java.io.FileOutputStream;

import net.dammy.android.fotogear.R;
import net.dammy.android.fotogear.AsyncImageLoader.AsyncImageCallback;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;


public class ViewPhoto extends Activity implements AsyncImageCallback {
	
	private static final int SAVE = 1;
	private static final int PREVIOUS = 3;
	private static final int NEXT = 4;
	private static final int TIMELINE = 5;
	private static final int WEB_VIEW = 6;
	private int position;
	private FeedItem item;
	private Bitmap _bm;
	
	String state = Environment.getExternalStorageState();
	
	ProgressDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.view_photo);
		
		Bundle extras = this.getIntent().getExtras();
		position = extras.getInt("position");
		item = Fotogear.timeline.get(position);
		
		dialog = ProgressDialog.show(this, "", "Loading Image");
		new AsyncImageLoader(item.getPhotoBigSrc(), this, this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onImageRecieved(String url, Bitmap bm) {
		ImageView img = (ImageView) this.findViewById(R.id.viewPhotoImg);
		_bm = bm;
		img.setImageBitmap(_bm);
		dialog.dismiss();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, TIMELINE, 0, "Timeline").setIcon(R.drawable.ic_menu_timeline);
		menu.add(0, SAVE, 0, "Save Image").setIcon(R.drawable.ic_menu_save);
		menu.add(0, WEB_VIEW, 0, "Web View").setIcon(R.drawable.ic_web_view);
		menu.add(0, PREVIOUS, 0, "Previous").setIcon(R.drawable.ic_menu_arrow_left);
		menu.add(0, NEXT, 0, "Next").setIcon(R.drawable.ic_menu_arrow_right);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case TIMELINE:{
				Intent i = new Intent(this, TimeLine.class);
				this.startActivity(i);
				break;
			}
			
			case PREVIOUS:{
				if ((position - 1) == -1){
					Toast.makeText(this, "No more photos", Toast.LENGTH_LONG).show();
				}else{
					Intent i = new Intent(this, ViewPhoto.class);
					i.putExtra("position", position - 1);
					this.startActivity(i);
				}
				break;
			}
			
			case NEXT:{
				if ((position + 1) > (Fotogear.timeline.size() - 1)){
					Toast.makeText(this, "No more photos", Toast.LENGTH_LONG).show();
				}else{
					Intent i = new Intent(this, ViewPhoto.class);
					i.putExtra("position", position + 1);
					this.startActivity(i);
				}
				break;
			}
			
			case WEB_VIEW:{
				String url = "http://m.facebook.com/photo.php?fbid=" + this.item.getObjectId();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				this.startActivity(i);
				break;
			}
			
			case SAVE:{
				if (Environment.MEDIA_MOUNTED.equals(state)){
					String outputDirectory = Environment.getExternalStorageDirectory() + "/fotogear";
					File directory = new File(outputDirectory);
					FileOutputStream stream = null;
					String filename = outputDirectory + "/" + System.currentTimeMillis() + ".jpg";
					File file = new File(filename);
					try{
						if(!directory.isDirectory()){
							directory.mkdir();
						}
						stream = new FileOutputStream(file);
						_bm.compress(CompressFormat.JPEG, 100, stream);
						stream.flush();
						stream.close();
						Toast.makeText(this, "saved to gallery", Toast.LENGTH_LONG).show();
						
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
					} catch(Exception e){
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}else{
					Toast.makeText(this, "Unable to access memory card", Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
