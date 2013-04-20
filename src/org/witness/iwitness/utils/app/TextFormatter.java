package org.witness.iwitness.utils.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.Utils;

import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.Log;

public class TextFormatter {
	public final static String LOG = Utils.LOG;
	
	public static class BoldGrey extends CharacterStyle {
		public static final String token = "@@@";
		static final int color = R.drawable.grey;

		@Override
		public void updateDrawState(TextPaint tp) {
			tp.setColor(color);

		}
	}

	public static class BoldBlack extends CharacterStyle {
		public static final String token = "###";
		static final int color = R.drawable.black;
		
		@Override
		public void updateDrawState(TextPaint tp) {
			tp.setColor(color);

		}
	}

	public static class BoldBrick extends CharacterStyle {
		public static final String token = "$$$";
		static final int color = R.drawable.eyewitness_blue;

		@Override
		public void updateDrawState(TextPaint tp) {
			tp.setColor(color);
			

		}
	}

	public static Map<String, CharacterStyle> STYLE_MAP;
	static {
		Map<String, CharacterStyle> StyleMap = new HashMap<String, CharacterStyle>();
		StyleMap.put(BoldBlack.token, new BoldBlack());
		StyleMap.put(BoldGrey.token, new BoldGrey());
		StyleMap.put(BoldBrick.token, new BoldBrick());

		STYLE_MAP = Collections.unmodifiableMap(StyleMap);
	}

	public static CharSequence formatSpan(CharSequence text) {
		
		Iterator<String> i = STYLE_MAP.keySet().iterator();
		while(i.hasNext()) {
			String token = i.next();
			Log.d(LOG, "token: " + token);
			
			if(String.valueOf(text).indexOf(token) >= 0) {
				int start = String.valueOf(text).indexOf(token) + token.length();
				int end = String.valueOf(text).lastIndexOf(token);
				Log.d(LOG, "start: " + start + " end: " + end);
				
				String sub = String.valueOf(text).substring(start, end);
				Log.d(LOG, sub);
				text = String.valueOf(text).replace(token, ""); 
				
				CharacterStyle cs = STYLE_MAP.get(token);
				SpannableStringBuilder ssb = new SpannableStringBuilder(sub);
				ssb.setSpan(cs, 0, sub.length(), 0);
			
				text = String.valueOf(text).replace(sub, ssb);
			}
		}

		return text;

	}

	public static CharSequence wrap(CharSequence text, String wrapper) {
		return (wrapper + text + wrapper);
	}
}
