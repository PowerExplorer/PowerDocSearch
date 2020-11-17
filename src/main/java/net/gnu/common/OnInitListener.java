package net.gnu.common;

import java.util.Locale;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class OnInitListener implements TextToSpeech.OnInitListener {
	private TTSActivity ttsFrag;
	private String lang;
	public OnInitListener(TTSActivity ttsFrag, String lang) {
		this.ttsFrag = ttsFrag;
		this.lang = lang;
	}
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (lang.length() > 0) {
				String[] split = lang.split("[- _/]");
				//if (split.length > 1) {
				int result = TTSActivity.tts.setLanguage(new Locale(split[0], split[1]));
				if (result == TextToSpeech. LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
					Activity activity = ttsFrag;
					if (activity != null) {
						Toast.makeText(activity, lang + " is not supported", Toast.LENGTH_SHORT).show();
					}
					Log.e ("error", lang + " is not supported");
				}
				//}
			}

		} else {
			Activity activity = ttsFrag;
			if (activity != null) {
				Toast.makeText(activity, "Initialization TTS Failed!", Toast.LENGTH_SHORT).show();
			}
			Log.e ("error", "Initialization Failed!");
		}
	}
}

