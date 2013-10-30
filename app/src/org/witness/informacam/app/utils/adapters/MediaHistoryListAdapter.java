package org.witness.informacam.app.utils.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.UIHelpers;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.TimeUtility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaHistoryListAdapter extends BaseAdapter {
	private List<INotification> notifications;
	private Context mContext;	
	private static final String LOG = App.LOG;
	
	
	public MediaHistoryListAdapter(Context context, String mediaId, List<INotification> notifications) {
		this.mContext = context;
		
		if (notifications != null)
		{
			this.notifications = new ArrayList<INotification>();
			for (INotification n : notifications)
			{
				if (mediaId.equals(n.mediaId))
				{
					if ((n.type == Models.INotification.Type.SHARED_MEDIA || n.type == Models.INotification.Type.EXPORTED_MEDIA) &&
						n.taskComplete)
					{
						this.notifications.add(n);
					}
				}
			}
			
			Collections.sort(this.notifications, new Comparator<INotification>() {
				@Override
				public int compare(INotification n1, INotification n2) {
					return n1.timestamp > n2.timestamp ? -1 : (n1==n2 ? 0 : 1);
				}
			
			});
			Log.d(LOG, "NUM NOTIFICATIONS: " + this.notifications.size());
		}	
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
		
		if (convertView == null)
			convertView = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.adapter_media_history_list_item, parent, false);
		
		TextView destination = (TextView) convertView.findViewById(R.id.tvDestination);
		if (notification.type == Models.INotification.Type.EXPORTED_MEDIA)
			destination.setText(R.string.editor_history_exported);
		else
			destination.setText(R.string.editor_history_shared);
		
		TextView time = (TextView) convertView.findViewById(R.id.tvTime);
		
		Date date = new Date(notification.timestamp);
		time.setText(UIHelpers.dateDiffDisplayString(date, mContext, 
				R.string.shared_never, R.string.shared_recently,
				R.string.shared_minutes, R.string.shared_minute,
				R.string.shared_hours, R.string.shared_hour,
				R.string.shared_days, R.string.shared_day));
		
		return convertView;
	}
}
