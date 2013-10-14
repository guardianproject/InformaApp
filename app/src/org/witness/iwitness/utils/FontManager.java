package org.witness.iwitness.utils;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

public class FontManager
{
	private static HashMap<String, Typeface> gFonts = new HashMap<String, Typeface>();

	public static Typeface getFontByName(Context context, String name)
	{
		if (gFonts.containsKey(name))
			return gFonts.get(name);

		try
		{
			Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/" + name + ".ttf");
			if (font != null)
			{
				gFonts.put(name, font);
			}
			return font;
		}
		catch (Exception ex)
		{
			Log.e("FontManager", "Failed to get font: " + name);
		}
		return null;
	}
}
