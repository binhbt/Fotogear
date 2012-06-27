package net.dammy.android.fotogear;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.dammy.android.fotogear.R;
import net.dammy.android.fotogear.AsyncImageLoader.AsyncImageCallback;

public class FeedView extends LinearLayout implements AsyncImageCallback {
	
	private FeedItem _item;
	private ImageView _img;
	
	public FeedView(Context context, FeedItem item){
		super(context);
		this._item = item;
		
		LayoutInflater inflater = (LayoutInflater) 
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.feed_view, this);
		
		((TextView) this.findViewById(R.id.feedItemNetwork)).setText("via " + _item.getNetwork());
		((TextView) this.findViewById(R.id.feedItemUsername)).setText(_item.getOwnerName());
		String date = new java.text.SimpleDateFormat("MMMMM dd',' yyyy 'at' hh'.'mm aa").format(_item.getDateCreated());
		((TextView) this.findViewById(R.id.feedItemTime)).setText(date);
		

		_img = (ImageView) this.findViewById(R.id.feedItemImg);
		_img.setImageBitmap(null);
		
		new AsyncImageLoader(_item.getPhotoSmallSrc(), FeedView.this, context);
	}

	@Override
	public void onImageRecieved(String url, Bitmap bm) {
		_img = (ImageView) this.findViewById(R.id.feedItemImg);
		_img.setImageBitmap(bm);
	}
	
	public void convertView(Context context, FeedItem item){
		this._item = item;
		((TextView) this.findViewById(R.id.feedItemNetwork)).setText("via " + _item.getNetwork());
		((TextView) this.findViewById(R.id.feedItemUsername)).setText(_item.getOwnerName());

		String date = new java.text.SimpleDateFormat("MMMMM dd',' yyyy 'at' hh'.'mm aa").format(_item.getDateCreated());
		((TextView) this.findViewById(R.id.feedItemTime)).setText(date);
		
		_img = (ImageView) this.findViewById(R.id.feedItemImg);
		_img.setImageBitmap(null);
		
		new AsyncImageLoader(_item.getPhotoSmallSrc(), FeedView.this, context);
	}
	
}
