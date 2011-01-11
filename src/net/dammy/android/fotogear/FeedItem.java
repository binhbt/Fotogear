package net.dammy.android.fotogear;

import java.util.Date;
import net.dammy.android.fotogear.R;

public class FeedItem {
	private String _ownerName;
	private String _ownerId;
	private String _objectId;
	private String _photoSmallSrc;
	private String _photoBigSrc;
	private String _network;
	private Date _dateCreated;
	
	public void setOwnerName(String name){
		_ownerName = name;
	}
	
	public String getOwnerName(){
		return _ownerName;
	}
	
	public void setOwnerId(String ownerId){
		_ownerId = ownerId;
	}
	
	public String getOwnerId(){
		return _ownerId;
	}
	
	public void setObjectId(String objectId){
		_objectId = objectId;
	}
	
	public String getObjectId(){
		return _objectId;
	}
	
	public void setPhotoSmallSrc(String src){
		_photoSmallSrc = src;
	}
	
	public String getPhotoSmallSrc(){
		return _photoSmallSrc;
	}
	
	public void setPhotoBigSrc(String src){
		_photoBigSrc = src;
	}
	
	public String getPhotoBigSrc(){
		return _photoBigSrc;
	}
	
	public void setNetwork(String network){
		_network = network;
	}
	
	public String getNetwork(){
		return _network;
	}
	
	public void setDateCreated(Date dateCreated){
		_dateCreated = dateCreated;
	}
	
	public Date getDateCreated(){
		return _dateCreated;
	}
}
