package net.dammy.android.fotogear;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.dammy.android.fotogear.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader extends Thread {
	
	private static final int FINISH = 0;
	
	private String _url;
	private AsyncImageCallback _callback;
	private Context _context;
	private String _filename;
	private Bitmap _bitmap;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case FINISH:{
					if(_bitmap != null){
						_callback.onImageRecieved(_url, _bitmap);
					}else{
						_callback.onImageRecieved(_url, null);
					}
				}
			}
		}
	};
	
	interface AsyncImageCallback {
		void onImageRecieved(String url, Bitmap bm);
	}
	
	public AsyncImageLoader(String url, AsyncImageCallback callback, Context context){
		super();
		_url = url;
		_callback = callback;
		_context = context;
		_filename = url.replace("/", "-");
		
		this.start();
	}
	
	public void run(){
		try{
			FileInputStream fin = _context.openFileInput(_filename);
			_bitmap = BitmapFactory.decodeStream(fin);
			handler.sendEmptyMessage(FINISH);
			fin.close();
		}catch(Exception e){
			try{
				// image not found.. download image
				HttpURLConnection con = (HttpURLConnection) new URL(_url).openConnection();
				con.setDoInput(true);
				con.connect();
				
				_bitmap = BitmapFactory.decodeStream(con.getInputStream());
				FileOutputStream fout = _context.openFileOutput(_filename, Context.MODE_PRIVATE);
				_bitmap.compress(CompressFormat.JPEG, 100, fout);
				fout.close();
				
				//_callback.onImageRecieved(_url, _bitmap);
				handler.sendEmptyMessage(FINISH);
			}catch(Exception ex){
				_bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.cant_load);
				handler.sendEmptyMessage(FINISH);
			}
		}
		
	}
}
