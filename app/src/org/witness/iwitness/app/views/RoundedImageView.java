package org.witness.iwitness.app.views;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.UIHelpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {

	private int mRadius;
	private Bitmap mBitmap;

	public RoundedImageView(Context context) {
		super(context);
		init(null);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	@SuppressLint("NewApi")
	private void init(AttributeSet attrs)
 {
		mRadius = UIHelpers.dpToPx(12, getContext());
		if (attrs != null)
		{
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
			mRadius = (int) a.getDimension(R.styleable.RoundedImageView_rounding_radius, UIHelpers.dpToPx(12, getContext()));
			a.recycle();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (oldw != w || oldh != h)
		{
			createRoundedBitmap();
		}
	}

	private void createRoundedBitmap()
	{
		if (mBitmap == null)
			return;
		
	    int targetWidth = getWidth();
	    int targetHeight = getHeight();
	    if (targetWidth == 0 || targetHeight == 0)
	    	return;
	    
	    Bitmap targetBitmap = Bitmap.createBitmap(
	        targetWidth,
	        targetHeight,
	        Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(targetBitmap);
	    
		// Adapted from
		// http://stackoverflow.com/questions/1705239/how-should-i-give-images-rounded-corners-in-android
		Bitmap rounder = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvasRounder = new Canvas(rounder);
		Paint xferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		xferPaint.setColor(Color.RED);
		canvasRounder.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight),
				mRadius, mRadius, xferPaint);
		xferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		// Create a private image view to do scaling stuff. Saves us a lot of tricky calculations
		// to achieve "CenterCrop" scale type... =)
		ImageView ivCopy = new ImageView(this.getContext());
		ivCopy.setScaleType(ScaleType.CENTER_CROP);		
		ivCopy.setImageBitmap(mBitmap);
		ivCopy.measure(MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY),
						MeasureSpec.makeMeasureSpec(targetHeight, MeasureSpec.EXACTLY));
		ivCopy.layout(0, 0, targetWidth, targetHeight);
		
		canvas.save();
		ivCopy.draw(canvas);
		canvas.restore();
		canvas.drawBitmap(rounder, 0, 0, xferPaint);

	    super.setImageBitmap(targetBitmap);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		mBitmap = bm;
		createRoundedBitmap();
	}

	@Override
	public void setImageResource(int resId) {
		Drawable d = this.getContext().getResources().getDrawable(resId);
		if (d instanceof BitmapDrawable) {
			mBitmap = ((BitmapDrawable) d).getBitmap();
			createRoundedBitmap();
		}
		else
		{
			super.setImageResource(resId);
		}
	}

}
