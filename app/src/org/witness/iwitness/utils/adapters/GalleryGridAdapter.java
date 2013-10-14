package org.witness.iwitness.utils.adapters;

import info.guardianproject.odkparser.utils.QD;

import java.util.List;

import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.App;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class GalleryGridAdapter extends BaseAdapter {
	
	private List<? super IMedia> media;
	LayoutInflater li;
	Activity a;
	boolean mInSelectionMode;
	private int mNumLoading;

	private final static String LOG = App.LOG;

	public GalleryGridAdapter(Activity a, List<? super IMedia> media)
			throws NullPointerException {
		this.media = media;
		this.a = a;
		li = LayoutInflater.from(a);
	}

	public void setNumLoading(int loading)
	{
		mNumLoading = loading;
		notifyDataSetChanged();
	}
	
	public void update(List<IMedia> newMedia) {
		media = newMedia;
		notifyDataSetChanged();
	}

	public void update(IMedia newMedia) {
		media.add(newMedia);
		notifyDataSetChanged();
	}

	public void setInSelectionMode(boolean inSelectionMode) {
		mInSelectionMode = inSelectionMode;
	}

	@Override
	public int getCount() {
		int ret = 0;
		if (media != null)
			ret = media.size();
		ret += mNumLoading;
		return ret;
	}

	@Override
	public Object getItem(int position) {
		if (position < mNumLoading)
			return null;
		return media.get(position - mNumLoading);
	}

	@Override
	public long getItemId(int position) {
		Object item = getItem(position);
		if (item != null)
			return item.hashCode();
		return position;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		View view = null;
		
		if (position < mNumLoading)
		{
			view = li.inflate(R.layout.adapter_gallery_grid_placeholder, parent, false);
			
			ProgressBar pgLoading = (ProgressBar) view.findViewById(R.id.pgLoading);
		}
		else
		{
			IMedia m = (IMedia) media.get(position - mNumLoading);
			view = li.inflate(R.layout.adapter_gallery_grid, null);

			// Show or hide the selection layer
			view.findViewById(R.id.chkSelect).setVisibility(
				mInSelectionMode ? View.VISIBLE : View.GONE);

			ImageView iv = (ImageView) view.findViewById(R.id.gallery_thumb);

			View iv_holder = view.findViewById(R.id.gallery_thumb_holder);
			if (m.isNew) {
				iv_holder.setBackgroundDrawable(a.getResources().getDrawable(
					R.drawable.extras_is_new_background));
			}

			try {
				Bitmap bitmap = m.getThumbnail();
				iv.setImageBitmap(bitmap);
			} catch (NullPointerException e) {
				iv.setImageDrawable(a.getResources().getDrawable(
					R.drawable.ic_action_video));
			}

			boolean hasAudio = false;
			boolean hasNotes = false;
			boolean hasTags = false;
			for (IForm form : m.getForms(a))
			{
				if (form.namespace.equals(Forms.FreeAudio.TAG))
				{
					hasAudio = true;
				}
				else if (form.namespace.equals(Forms.FreeText.TAG))
				{
					QD question = form.getQuestionDefByTitleId(Forms.FreeText.PROMPT);
					if (question != null && question.hasInitialValue && !TextUtils.isEmpty(question.initialValue)) 
						hasNotes = true;
				}
			}
			
			hasTags = (m.getInnerLevelRegions().size() > 0);
			
			view.findViewById(R.id.llSymbols).setVisibility((hasAudio || hasNotes || hasTags) ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.ivAudioNote).setVisibility(hasAudio ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.ivNote).setVisibility(hasNotes ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.ivTag).setVisibility(hasTags ? View.VISIBLE : View.GONE);
		}
		return view;
	}

}
