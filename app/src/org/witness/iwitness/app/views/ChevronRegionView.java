package org.witness.iwitness.app.views;

import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.ui.editors.IRegionDisplay;
import org.witness.informacam.utils.Constants.IRegionDisplayListener;
import org.witness.iwitness.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RelativeLayout.LayoutParams;

public class ChevronRegionView extends IRegionDisplay
{
	private boolean mIsActive;
	private Paint mPaint;
	private Path mPath;
	private LinearGradient mShader;

	public ChevronRegionView(Context context, IRegion region, IRegionDisplayListener listener)
	{
		super(context, region, listener);
		this.setAdjustViewBounds(false);
		this.setScaleType(ScaleType.CENTER);
		update();
	}

	@Override
	public void setStatus(boolean isActive)
	{
		mIsActive = isActive;
		super.setStatus(isActive);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (this.bounds != null)
			this.setMeasuredDimension(bounds.displayWidth, bounds.displayHeight);
		else
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void update()
	{
		super.update();
		LayoutParams lp = (LayoutParams) getLayoutParams();
		lp.leftMargin = bounds.displayLeft;
		lp.topMargin = bounds.displayTop;
		setLayoutParams(lp);

		Log.d("Chevron", "new size:" + this.getWidth() + "," + this.getHeight());

		
		updatePath();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		updatePath();
	}

	private void updatePath()
	{
		float radius = getWidth() / 20;
		float arrowHeight = getHeight() / 4;

		Rect rect = new Rect(2, 2, this.getWidth() - 4, this.getHeight() - 4);

		mPath = new Path();
		mPath.moveTo(rect.left + radius, rect.top);
		mPath.lineTo(rect.right, rect.top);
		mPath.lineTo(rect.right, rect.bottom - arrowHeight);
		mPath.lineTo(rect.exactCenterX(), rect.bottom);
		mPath.lineTo(rect.left, rect.bottom - arrowHeight);
		mPath.lineTo(rect.left, rect.top);
		mPath.close();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeWidth(3);
		mPaint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you
												// want
		mPaint.setStrokeCap(Paint.Cap.ROUND); // set the paint cap to round too
		mPaint.setPathEffect(new CornerPathEffect(radius)); // set the path
															// effect when they

		int color = this.getContext().getResources().getColor(R.color.tag_unselected);
		if (mIsActive)
			color = this.getContext().getResources().getColor(R.color.tag_selected);
		mShader = new LinearGradient(0, 0, 0, getHeight(), color, color, Shader.TileMode.CLAMP);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		mPaint.setShader(mShader);
		mPaint.setStyle(Paint.Style.FILL);
		int sc = canvas.save();
		canvas.drawPath(mPath, mPaint);
		mPaint.setShader(null);
		mPaint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(mPath, mPaint);
		canvas.restoreToCount(sc);
		canvas.clipPath(mPath);
	}

	@Override
	public void setImageDrawable(Drawable drawable)
	{
	}
}
