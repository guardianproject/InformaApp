package org.witness.informacam.app.screens;

import info.guardianproject.odkparser.FormWrapper.ODKFormListener;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.witness.informacam.app.EditorActivity;
import org.witness.informacam.app.R;
import org.witness.informacam.app.screens.editors.FullScreenVideoViewFragment;
import org.witness.informacam.app.screens.popups.PopupClickListener;
import org.witness.informacam.app.utils.Constants.App;
import org.witness.informacam.app.utils.Constants.EditorActivityListener;
import org.witness.informacam.app.views.ChevronRegionView;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.models.media.IRegionBounds;
import org.witness.informacam.ui.editors.IRegionDisplay;
import org.witness.informacam.utils.Constants.IRegionDisplayListener;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;

import android.app.Activity;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class FullScreenViewFragment extends Fragment implements OnClickListener, OnTouchListener, IRegionDisplayListener, ODKFormListener
{
	public enum Mode
	{
		Normal, Edit, AddTags
	}

	protected Mode currentMode = Mode.Normal;
	protected View rootView;

	protected RelativeLayout mediaHolder;

	protected int[] dims;

	protected IRegion currentRegion = null;

	public int DEFAULT_REGION_WIDTH, DEFAULT_REGION_HEIGHT;

	protected Handler h = new Handler();

	protected Activity a;

	// For moving tags
	private float mStartDragX;
	private float mStartDragY;
	private float mStartDragTagX;
	private float mStartDragTagY;
	private boolean movingTag = false;

	protected final static String LOG = App.Editor.LOG;

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);

		this.a = a;
	}

	// @Override
	// public void onDestroy() {
	// if(isEditingForm) {
	// ((TagFormFragment) tagFormFragment).saveTagFormData(currentRegion);
	// isEditingForm = false;
	// }
	//
	//
	// super.onDestroy();
	// }

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		dims = new int[] { display.getWidth(), display.getHeight() };

		rootView = li.inflate(R.layout.fragment_editor_media_view, null);

		DEFAULT_REGION_WIDTH = (int) (dims[0] * 0.15);
		DEFAULT_REGION_HEIGHT = (int) (dims[1] * 0.1);

		mediaHolder = (RelativeLayout) rootView.findViewById(R.id.media_holder);
		mediaHolder.setOnTouchListener(this);

		initLayout();

		return rootView;
	}

	protected void initLayout()
	{
	}

	protected void deleteTag()
	{
		((EditorActivityListener) a).media().removeRegion(currentRegion);
		mediaHolder.removeView(getTagViewByRegion(currentRegion));
		
//		for(int v=0; v<mediaHolder.getChildCount(); v++) {
//			View v_ = mediaHolder.getChildAt(v);
//			if(v_ instanceof IRegionDisplay) {
//				for(IRegion r : ((EditorActivityListener) a).media().associatedRegions) {
//					if(r.getRegionDisplay().equals(v_)) {
//						r.getRegionDisplay().indexOnScreen = v;
//					}
//				}
//			}
//		}
		
		currentRegion = null;
	}

	protected IRegionDisplay getTagViewByRegion(IRegion region)
	{
		for (int i = 0; i < mediaHolder.getChildCount(); i++)
		{
			View child = mediaHolder.getChildAt(i);
			if (child instanceof IRegionDisplay && ((IRegionDisplay) child).parent == region)
			{
				return (IRegionDisplay) child;
			}
		}
		return null;
	}

	protected void initRegions()
	{
		mediaHolder.setOnTouchListener(this);
		// mediaHolder.setOnClickListener(this);
		// toggleControls.setOnClickListener(this);

		if (((EditorActivityListener) a).media().associatedRegions != null)
		{
			for (IRegion r : ((EditorActivityListener) a).media().associatedRegions)
			{
				Log.d(LOG, "setting old region: " + r.asJson().toString());
				if (r.bounds.displayWidth != 0 && r.bounds.displayHeight != 0)
				{
					r.init(getActivity(), r.bounds, false, this);
					IRegionDisplay regionDisplay = r.getRegionDisplay();
					regionDisplay.setOnTouchListener(this);					
					regionDisplay.setSoundEffectsEnabled(false);
					//regionDisplay.indexOnScreen = mediaHolder.getChildCount();

					View newView = new ChevronRegionView(getActivity(), r, this);
					newView.setOnTouchListener(this);
					newView.setSoundEffectsEnabled(false);
					mediaHolder.addView(newView);
					// mediaHolder.addView(r.getRegionDisplay());
				}
			}

			updateRegionDisplay();
		}

		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				((EditorActivityListener) getActivity()).waiter(false);
			}
		});
	}

	protected void updateRegionDisplay()
	{
		if (((EditorActivityListener) a).media().associatedRegions != null)
		{
			for (IRegion r : ((EditorActivityListener) a).media().associatedRegions)
			{
				if (!r.equals(currentRegion) || currentMode == Mode.Normal)
				{
					IRegionDisplay display = getTagViewByRegion(r);
					if (display != null)
						display.setStatus(false);
				}
			}
		}
	}

	protected void setCurrentRegion(IRegion region)
	{
		setCurrentRegion(region, false);
	}

	protected void setCurrentRegion(IRegion region, boolean isNew)
	{
		Log.d(LOG, "this region: " + region.asJson().toString());

		currentRegion = region;
		currentRegion.getRegionDisplay().setOnTouchListener(this);

		if (isNew) {
			View newView = new ChevronRegionView(getActivity(), currentRegion, this);
			newView.setSoundEffectsEnabled(false);
			newView.setOnTouchListener(this);
			mediaHolder.addView(newView);
			// mediaHolder.addView(currentRegion.getRegionDisplay());
			currentRegion.getRegionDisplay().setSoundEffectsEnabled(false);
		}

		updateRegionDisplay();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				((EditorActivityListener) getActivity()).waiter(true);
			}
		});

	}

	@Override
	public void onClick(View v)
	{
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// Log.d(LOG, "on touch called at " + event.getX() + "," + event.getY()
		// + "\n on view: " + v.getClass().getName() + " (id " + v.getId() +
		// ")\naction: " + event.getAction());

		if (v instanceof IRegionDisplay && currentMode != Mode.Normal)
		{
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
			{
				mStartDragX = event.getX() + v.getLeft();
				mStartDragY = event.getY() + v.getTop();
				IRegionBounds bounds = ((IRegionDisplay) v).bounds;
				mStartDragTagX = bounds.displayLeft;
				mStartDragTagY = bounds.displayTop;
				v.performClick();
				((IRegionDisplay) v).update();
				v.postInvalidate();
				if (currentMode == Mode.Edit)
				{
					// Bring up the context menu
					IRegionDisplay regionDisplay = (IRegionDisplay) v;
					this.setCurrentRegion(regionDisplay.parent);
					showTagContextMenu(regionDisplay);
			}
				break;
			}
			case MotionEvent.ACTION_MOVE:

				final float x = event.getX() + v.getLeft();
				final float y = event.getY() + v.getTop();

				if (currentMode == Mode.AddTags && (movingTag || Math.abs(x - mStartDragX) > 10 || Math.abs(y - mStartDragY) > 10))
				{
					if (!movingTag)
					{
						v.cancelLongPress();
						movingTag = true;
						v.getParent().requestDisallowInterceptTouchEvent(true);
					}
					IRegionBounds bounds = ((IRegionDisplay) v).bounds;
					bounds.displayLeft = (int) (mStartDragTagX + (x - mStartDragX));
					bounds.displayTop = (int) (mStartDragTagY + (y - mStartDragY));
					constrainBoundsToImage(bounds);
					((IRegionDisplay) v).update();
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (movingTag)
				{
					((IRegionDisplay) v).parent.update(getActivity());
					movingTag = false;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				movingTag = false;
				break;
			}
		}
		else if (currentMode == Mode.AddTags)
		{
			currentRegion = null;
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				v.getParent().requestDisallowInterceptTouchEvent(true);
			}
			else if (event.getAction() == MotionEvent.ACTION_UP)
			{
				try
				{

					if (((EditorActivityListener) a).media().dcimEntry.mediaType.equals(MimeType.IMAGE))
					{

						IRegion region = ((EditorActivityListener) a).media().addRegion(getActivity(), (int) event.getY() - (DEFAULT_REGION_HEIGHT / 2),
								(int) event.getX() - (DEFAULT_REGION_WIDTH / 2), DEFAULT_REGION_WIDTH, DEFAULT_REGION_HEIGHT, this);
						constrainBoundsToImage(region.bounds);
						setCurrentRegion(region, true);
					}
					else if (((EditorActivityListener) a).media().dcimEntry.mediaType.equals(MimeType.VIDEO))
					{

						IRegion region = ((EditorActivityListener) a).media().addRegion(getActivity(), (int) event.getY() - (DEFAULT_REGION_HEIGHT / 2),
								(int) event.getX() - (DEFAULT_REGION_WIDTH / 2), DEFAULT_REGION_WIDTH, DEFAULT_REGION_HEIGHT,
								((FullScreenVideoViewFragment) this).getCurrentPosition(), ((FullScreenVideoViewFragment) this).getDuration(), this);
						constrainBoundsToImage(region.bounds);
						setCurrentRegion(region, true);
					}

				}
				catch (JSONException e)
				{
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}
			}
			return true;
		}
		else if (currentMode == Mode.Edit)
		{
			// Clicking outside of a tag in edit mode means "select none"
			//
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
			{
				currentRegion = null;
				updateRegionDisplay();
			}
			break;
			}
		}
		return false;
	}

	@Override
	public void onSelected(IRegionDisplay regionDisplay)
	{
		setCurrentRegion(regionDisplay.parent);
	}

	@Override
	public boolean saveForm()
	{

		return true;
	}

	@Override
	public int[] getSpecs()
	{

		if (((EditorActivityListener) a).media() != null)
		{
			List<Integer> specs = new ArrayList<Integer>();

			specs.add(((EditorActivityListener) a).media().width);
			specs.add(((EditorActivityListener) a).media().height);
			return ArrayUtils.toPrimitive(specs.toArray(new Integer[specs.size()]));
		}
		else
		{
			return null;
		}
	}

	public void setCurrentMode(Mode mode)
	{
		currentMode = mode;
		if (currentMode == Mode.Normal)
		{
			// clear current selection
			currentRegion = null;
			updateRegionDisplay();
		}
	}

	private void showTagContextMenu(IRegionDisplay view)
	{
		try
		{
			LayoutInflater inflater = LayoutInflater.from(getActivity());

			View content = inflater.inflate(R.layout.popup_tag_context_menu, mediaHolder, false);
			content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			PopupWindow mMenuPopup = new PopupWindow(content, content.getMeasuredWidth(), content.getMeasuredHeight(), true);

			// Delete
			//
			View btnDelete = content.findViewById(R.id.btnDeleteTag);
			btnDelete.setOnClickListener(new PopupClickListener(mMenuPopup)
			{
				@Override
				protected void onSelected()
				{
					deleteTag();
				}
			});

			// Form
			//
			View btnForm = content.findViewById(R.id.btnForm);
			btnForm.setOnClickListener(new PopupClickListener(mMenuPopup)
			{
				@Override
				protected void onSelected()
				{
					showTagFormPopup(currentRegion);
				}
			});

			mMenuPopup.setOutsideTouchable(true);
			mMenuPopup.setBackgroundDrawable(new BitmapDrawable());
			mMenuPopup.showAsDropDown(view, view.getWidth(), -view.getHeight());

			mMenuPopup.getContentView().setOnClickListener(new PopupClickListener(mMenuPopup));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void showTagFormPopup(IRegion region)
	{
		try
		{
			((EditorActivity) getActivity()).showTagForm(region);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void constrainBoundsToImage(IRegionBounds bounds)
	{
		RectF imageBounds = getImageBounds();
		if (imageBounds != null)
		{
			bounds.displayLeft = (int) Math.max(bounds.displayLeft, imageBounds.left - bounds.displayWidth / 2);
			bounds.displayTop = (int) Math.max(bounds.displayTop, imageBounds.top - bounds.displayHeight / 2);
			bounds.displayLeft = (int) Math.min(bounds.displayLeft, imageBounds.right - bounds.displayWidth / 2);
			bounds.displayTop = (int) Math.min(bounds.displayTop, imageBounds.bottom - bounds.displayHeight / 2);	
		}
	}
	
	public RectF getImageBounds()
	{
		return null;
	}

}
