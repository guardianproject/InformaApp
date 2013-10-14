package org.witness.iwitness.utils.adapters;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationsListAdapter extends BaseAdapter {
	private List<INotification> notifications;	
	private static final String LOG = App.LOG;
	
	
	public NotificationsListAdapter(List<INotification> notifications) {
		this.notifications = notifications;
		if (this.notifications != null)
		{
			Collections.sort(this.notifications, new Comparator<INotification>() {

				@Override
				public int compare(INotification n1, INotification n2) {
					return n1.timestamp > n2.timestamp ? -1 : (n1==n2 ? 0 : 1);
				}
			
			});
		
			Log.d(LOG, "NUM NOTIFICATIONS: " + this.notifications.size());
		}
	}
	
	public void update(List<INotification> newNotifications, Activity a) {
		notifications = newNotifications;
		if (notifications != null)
		{
			Collections.sort(notifications, new Comparator<INotification>() {

				@Override
				public int compare(INotification n1, INotification n2) {
					return n1.timestamp > n2.timestamp ? -1 : (n1==n2 ? 0 : 1);
				}
			
			});
		}
		
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Logger.d(LOG, "NOTIFIY DATA SET CHANGED IN HANDLER");
				notifyDataSetChanged();
			}
		});
		
	}
	
	public void update(INotification newNotification, Activity a) {		
		notifications.add(newNotification);
		Collections.sort(notifications, new Comparator<INotification>() {

			@Override
			public int compare(INotification n1, INotification n2) {
				return n1.timestamp > n2.timestamp ? -1 : (n1==n2 ? 0 : 1);
			}
			
		});
		
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public int getCount() {
		if (notifications == null)
			return 0;
		return notifications.size();
	}

	@Override
	public Object getItem(int position) {
		return notifications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		INotification notification = notifications.get(position);
		
		convertView = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.adapter_notification_list_item, null);
		
		TextView metadata = (TextView) convertView.findViewById(R.id.notification_metadata);
		StringBuffer sb = new StringBuffer();
		sb.append(TimeUtility.millisecondsToDatestamp(notification.timestamp));
		if(notification.from != null) {
			sb.append(System.getProperty("line.separator")).append(notification.from);
		}
		metadata.setText(sb.toString());
		
		if(notification.label != null) {
			TextView label = (TextView) convertView.findViewById(R.id.notification_label);
			label.setText(notification.label);
		}
		
		if(notification.content != null) {
			TextView content = (TextView) convertView.findViewById(R.id.notification_content);
			content.setText(notification.content);
		}
		
		if(notification.icon != null) {
			byte[] iconBytes = null;
			if(notification.iconSource == Type.IOCIPHER) {
				iconBytes = InformaCam.getInstance().ioService.getBytes(notification.icon, Type.IOCIPHER);
			}
			
			if(iconBytes != null) {
				ImageView icon = (ImageView) convertView.findViewById(R.id.notification_icon);
				Bitmap b = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
				icon.setImageBitmap(b);
			}
		}
		
		if(notification.type == Models.INotification.Type.EXPORTED_MEDIA) {
			ImageView statusIcon = (ImageView) convertView.findViewById(R.id.notification_status);
			
			int d = R.drawable.ic_notification_waiting;
			if(notification.taskComplete) {
				d = R.drawable.ic_notification_accepted;
			} else if(notification.canRetry) {
				d = R.drawable.ic_notification_failed;
			}
			
			statusIcon.setImageDrawable(parent.getContext().getApplicationContext().getResources().getDrawable(d));
			statusIcon.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
}
