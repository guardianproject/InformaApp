package org.witness.iwitness.app;

import org.witness.informacam.InformaCam;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.UIHelpers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
	private final static String LOG = Constants.App.Login.LOG;
	private String packageName;

	View rootView;
	EditText password;
	Button commit;
	ProgressBar waiter;

	Handler h = new Handler();

	InformaCam informaCam = InformaCam.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = this.getPackageName();

		setContentView(R.layout.activity_login);
		rootView = findViewById(R.id.llRoot);

		password = (EditText) findViewById(R.id.login_password);

		commit = (Button) findViewById(R.id.login_commit);
		commit.setOnClickListener(this);

		waiter = (ProgressBar) findViewById(R.id.login_waiter);
	}

	private void toggleStatus(boolean showButton) {
		if(showButton) {
			waiter.setVisibility(View.GONE);
			commit.setVisibility(View.VISIBLE);
		} else {
			commit.setVisibility(View.GONE);
			waiter.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		if(v == commit && password.getText().length() > 0) {
			h.post(new Runnable() {
				@Override
				public void run() {
					toggleStatus(false);
				}
			});

			new Thread(new Runnable() {
				@Override
				public void run() {
					if(informaCam.attemptLogin(password.getText().toString())) {
						setResult(Activity.RESULT_OK);
						finish();
					} else {
						h.post(new Runnable() {
							@Override
							public void run() {
								password.setText("");
								toggleStatus(true);
								Toast.makeText(LoginActivity.this, getString(R.string.we_could_not_log), Toast.LENGTH_LONG).show();
							}
						});

					}
				}
			}).start();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		boolean handled = super.onTouchEvent(event);
		if (!handled && event.getAction() == MotionEvent.ACTION_UP)
		{
			rootView.requestFocus();
			UIHelpers.hideSoftKeyboard(this);
		}
		return handled;
	}
	
}
