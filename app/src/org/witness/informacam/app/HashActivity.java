package org.witness.informacam.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.embed.VideoConstructor;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.MediaHasher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class HashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

	    if (type != null) {
	        if (type.startsWith("image/") || type.startsWith("video/")) {
	        	 Uri uriMedia = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	        	 
	        	 try
	        	 {
	        		 String mediaHash = generateMediaHash(type, uriMedia);
	        		 showHashInfo(uriMedia,mediaHash, type);
	        	 }
	        	 catch (Exception e)
	        	 {
	        		 Log.d("Hasher","error generating hash",e);
	        	 }
	        	 
	        }
	    } 
	}

	public void showHashInfo (Uri uriMedia, String hash, String mimeType) throws IOException
	{

		String hashType = null;
		String fileType = "file";
			
		if (mimeType.startsWith("image"))
		{
			hashType = "SHA-1";
			fileType = "image";
		}
		else if (mimeType.startsWith("video"))
		{
			hashType = "MD5";
			fileType = "video";
		}
		else
			hashType = "";
		
		String filePath = getRealPathFromURI(uriMedia);
		
		if (filePath == null || (!new File(filePath).exists()))
			filePath = new File(uriMedia.getPath()).getCanonicalPath();
		
		File fileMedia = new File(filePath);
		
		final String searchLink = "https://j3m.info/submissions/?hashes=" + hash;
		
		final String hashMessage = "The " + fileType + " '" + fileMedia.getName() + " has a pixelhash(" + hashType + ") of " + hash;
		final String hashTag = "\n\nSearch for this file on our public notary: " + searchLink + " #informacam";
		
		new AlertDialog.Builder(this)
	    .setTitle("InformaCam PixelHash")
	    .setMessage(hashMessage)
	    .setPositiveButton("Share Hash", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	Intent sendIntent = new Intent();
		    	sendIntent.setAction(Intent.ACTION_SEND);
		    	sendIntent.putExtra(Intent.EXTRA_TEXT, hashMessage + hashTag);
		    	sendIntent.setType("text/plain");
		    	startActivity(sendIntent);
		    	HashActivity.this.finish();
	            
	        }
	     })
	     .setNeutralButton("Search Notary", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	Intent sendIntent = new Intent();
		    	sendIntent.setAction(Intent.ACTION_VIEW);
		    	sendIntent.setData(Uri.parse(searchLink));
		    	startActivity(sendIntent);
		    	HashActivity.this.finish();
	            
	        }
	     })
	    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            HashActivity.this.finish();
	            
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_info)
	     .show();
	}
	
	public String generateMediaHash (String mimeType, Uri uriMedia) throws NoSuchAlgorithmException, IOException
	{
		String result = null;
		
		if (mimeType.startsWith("image"))
		{
			InputStream in = getContentResolver().openInputStream(uriMedia); 

			result = MediaHasher.getJpegHash(in);
		
		}
		else if (mimeType.startsWith("video"))
		{
			InformaCam informaCam = InformaCam.getInstance();
			// 1. hash
			VideoConstructor vc = new VideoConstructor(informaCam);
			
			String filePath = getRealPathFromURI(uriMedia);
			
			if (filePath == null || (!new File(filePath).exists()))
				filePath = new File(uriMedia.getPath()).getCanonicalPath();
			
			String fileExt = filePath.substring(filePath.lastIndexOf('.')+1);
			
			result = vc.hashVideo(filePath, Type.FILE_SYSTEM, fileExt);
		}
		
		return result;
		
	}
	
	 public String getRealPathFromURI(Uri contentUri) {
		  String[] proj = { MediaStore.Images.Media.DATA };
		  
		  //This method was deprecated in API level 11
		  //Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		  
		  CursorLoader cursorLoader = new CursorLoader(
		            this, 
		            contentUri, proj, null, null, null);        
		  Cursor cursor = cursorLoader.loadInBackground();
		  
		  int column_index = 
		    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		  
		  if (cursor.isBeforeFirst())
		  {
			  cursor.moveToFirst();
			  return cursor.getString(column_index); 
		  }
		  else
			  return null;
		 }
}
