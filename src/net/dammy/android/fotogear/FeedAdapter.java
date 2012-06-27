package net.dammy.android.fotogear;

import java.util.List;

import net.dammy.android.fotogear.FeedItem;
import net.dammy.android.fotogear.FeedView;
import net.dammy.android.fotogear.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class FeedAdapter extends BaseAdapter {
	private Context _context;
	private List<FeedItem> _feed;
	
	public FeedAdapter(Context c, List<FeedItem> feed){
		_context = c;
		_feed  = feed;
	}
	@Override
	public int getCount() {
		return _feed.size();
	}

	@Override
	public Object getItem(int position) {
		return _feed.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FeedView feedView;
		if(convertView == null){
			feedView = new FeedView(_context, _feed.get(position));
		}else{
			feedView = (FeedView) convertView;
			feedView.convertView(_context, _feed.get(position));
		}
        return feedView;
	}
}
