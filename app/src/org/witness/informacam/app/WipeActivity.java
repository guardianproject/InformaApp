package org.witness.informacam.app;

import org.witness.informacam.app.utils.Constants;
import org.witness.informacam.app.utils.Constants.Preferences;
import org.witness.informacam.app.utils.UIHelpers;
import org.witness.informacam.app.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class WipeActivity extends Activity implements OnTouchListener {
	private final static String LOG = Constants.App.Wipe.LOG;

	private View mArrow;
	private ImageView mSymbol;
	private boolean mOnlyTesting;
	private boolean mWipeEntireApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setBackgroundDrawable(null);
		setContentView(R.layout.activity_wipe);

		mOnlyTesting = getIntent().getBooleanExtra("testing", false);

		mArrow = findViewById(R.id.arrowSymbolView);

		mSymbol = (ImageView) findViewById(R.id.radioactiveSymbolView);
		mSymbol.setOnTouchListener(this);

		View btnCancel = findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WipeActivity.this.finish();
			}
		});

		View btnSettings = findViewById(R.id.btnSettings);
		btnSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WipeActivity.this,
						PreferencesActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				WipeActivity.this.startActivity(intent);
				WipeActivity.this.finish();
			}
		});

		String action = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.Keys.PANIC_ACTION, "0");
		mWipeEntireApp = (Integer.parseInt(action) == 1);
		
		TextView textHint = (TextView) findViewById(R.id.textHint);
		if (mWipeEntireApp)
			textHint.setText(R.string.panic_hint);
		else
			textHint.setText(R.string.panic_hint_wipe_content);
	}

	public int yMaxTranslation;
	public int yTranslationArrow;
	public int yCurrentTranslation;
	public int yDelta;
	public int yOriginal;
	public Rect mArrowRect;
	public boolean mIsOverArrow = false;

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view == mSymbol) {
			final int X = (int) event.getRawX();
			final int Y = (int) event.getRawY();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view
						.getLayoutParams();
				yOriginal = lParams.topMargin;
				yDelta = Y - lParams.topMargin;
				mIsOverArrow = false;

				mArrowRect = new Rect();
				if (!mArrow.getGlobalVisibleRect(mArrowRect)) {
					mArrowRect = null;
				} else {
					Rect symbolRect = new Rect();
					if (mSymbol.getGlobalVisibleRect(symbolRect)) {
						yMaxTranslation = mArrowRect.bottom - symbolRect.bottom;
						yTranslationArrow = mArrowRect.top - symbolRect.bottom;
					}
				}
				break;
			case MotionEvent.ACTION_UP: {
				mSymbol.setColorFilter(null);
				if (mIsOverArrow) {
					UIHelpers.scale(mSymbol, 1.0f, 0, 200,
							new Runnable() {
								@Override
								public void run() {
									doWipe();
								}
							});
				} else {
					UIHelpers.translateY(mSymbol, yCurrentTranslation,
							0, 200);
				}
				mIsOverArrow = false;
				break;
			}

			case MotionEvent.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_POINTER_UP:

				break;
			case MotionEvent.ACTION_MOVE: {
				yCurrentTranslation = Math.max(0,
						Math.min(Y - yDelta, yMaxTranslation));
				UIHelpers.translateY(mSymbol, yCurrentTranslation,
						yCurrentTranslation, 0);

				if (yCurrentTranslation >= yTranslationArrow)
					mIsOverArrow = true;
				else
					mIsOverArrow = false;
				setSymbolColor(mIsOverArrow);
				break;
			}
			}
			view.invalidate();
			return true;
		}
		return false;
	}

	private void setSymbolColor(boolean isOverArrow) {
		if (isOverArrow)
			mSymbol.setColorFilter(0xffff0000);
		else
			mSymbol.setColorFilter(null);
	}

	private void doWipe() {
		if (mOnlyTesting) {
			Builder alert = new AlertDialog.Builder(this)
					.setTitle(R.string.app_name)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									WipeActivity.this.finish();
								}
							}).setMessage(R.string.panic_test_successful);
			alert.show();
		} else {
			this.setResult(Activity.RESULT_OK);
			WipeActivity.this.finish();
		}
	}
}
