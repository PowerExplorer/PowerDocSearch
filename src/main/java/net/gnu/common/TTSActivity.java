package net.gnu.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import net.gnu.util.*;
import net.gnu.agrep.R;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app. FragmentTransaction;
import android.content.*;
import android.widget.*;
import java.io.*;
import net.gnu.androidutil.*;
import android.content.pm.*;
import android.app.*;
import net.gnu.agrep.*;
import android.graphics.*;
import java.util.*;

public class TTSActivity extends CommonActivity implements Serializable, SeekBar.OnSeekBarChangeListener, OnFragmentInteractionListener {
	
	private static transient final long serialVersionUID = 6809126859083008379L;
	private static transient final String TAG = "TTSActivity";
	
	private transient OnFragmentInteractionListener mListener = this;
    
	private static transient final int CHECK_TTS_AVAILABILITY = 101;
	private transient String NO_TTS_ANDROID_MARKET_REDIRECT = 
	"'SpeechSynthesis Data Installer' is not installed on your system, you are being redirected to" +
	" the installer package. You may also be able to install it my going to the 'Home Screen' then " +
	"(Menu -> Settings -> Voice Input & output -> Text-to-speech settings)";
    private transient String NO_TTS_AVAILABLE = 
	"'SpeechSynthesis Data Installer' is not available on your system, " +
	"you may have to install it manually yourself. You may also be able to install it my going to the 'Home Screen' " +
	"then (Menu -> Settings -> Voice Input & output -> Text-to-speech settings)";

	String text = "";
	String lang = "en_GB";
	String speed = Float.valueOf(0.7f).toString();
	String pitch = "1";
	String dot = "1000";
	String para = "1500";
	boolean speak = true;
	boolean saveText = false;
	String replace = "";//"ā\nī\nū\nṅ\nṭ\nñ\nḍ\nṇ\nḷ\nṃ\nṁ\nĀ\nĪ\nŪ\nṄ\nṬ\nÑ\nḌ\nṆ\nḶ\nṂ\nṀ";
	String by = "";//     "a\ni\nu\nn\nt\nnh\nd\nn\nl\nm\nm\nA\nI\nU\nN\nT\nNh\nD\nN\nL\nM\nM";
	boolean isRegex = false;
	boolean caseSensitive = false;
	boolean toWav = false;
	static String curFile = null;
	static int partNo = -1;
	static int offset = -1;
	int vol = 5;
	String volProgress = "5%";
	String prevFiles = "";
	String prevText = "";
	String prevTextF = "";
	ArrayList<String> langList = new ArrayList<String>();
	private FragmentManager supportFragmentManager;
	private RetainFrag retainFrag;
    
	transient static TextToSpeech tts;
	transient static TTSTask ttsTask;
	transient static boolean cancel = false;
	transient static String command = "";
	
	transient CheckBox toWavCB;
	transient CheckBox isRegexCB;
    transient CheckBox caseSensitiveCB;
	transient SeekBar volBar;
	
	transient TextView volProgressTV;
    transient EditText replaceET;
    transient EditText byET;
    transient CheckBox speakCB;
	transient CheckBox saveTextCB;
	
	transient EditText textET;
	transient Spinner langSpinner;
	transient EditText speedET;
	transient EditText pitchET;
	transient EditText dotET;
	transient EditText paraET;
	transient static TextView statusTV;
	transient ImageButton startBtn;
	//transient Button cancelBtn;
	transient ImageButton stopBtn;
	
	transient Button filesBtn;
    transient Button saveToBtn;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tts);
		
        supportFragmentManager = getSupportFragmentManager();
		retainFrag = (RetainFrag) supportFragmentManager.findFragmentByTag("retainFragTTS");
		if (retainFrag == null) {
            retainFrag = new RetainFrag();
			final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
			transaction.add(retainFrag, "retainFragTTS");
			transaction.commit();
        } 
		
		final View customView = getLayoutInflater().inflate(R.layout.filechoosertoolbar, null);
		((TextView)customView.findViewById(R.id.title)).setText("Text to Speech");
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(customView);
		startBtn=(ImageButton)customView.findViewById(R.id.okDir);
		stopBtn=(ImageButton)customView.findViewById(R.id.cancelDir);
		stopBtn.setImageResource(R.drawable.ic_action_stop);
		startBtn.setColorFilter(0xff0000ff, PorterDuff.Mode.SRC_IN);
		startBtn.setTag("Play");
		stopBtn.setColorFilter(0xff0000ff, PorterDuff.Mode.SRC_IN);
		
		fileET=(EditText)findViewById(R.id.files);
		saveToET =(EditText)findViewById(R.id.saveTo);
		
		filesBtn = (Button) findViewById(R.id.filesBtn);
        saveToBtn = (Button) findViewById(R.id.saveToBtn);
		
		textET=(EditText)findViewById(R.id.text);
		//cancelBtn=(Button)findViewById(R.id.cancelDir);
		langSpinner = (Spinner)findViewById(R.id.lang);
		speedET = (EditText)findViewById(R.id.speed);
		pitchET = (EditText)findViewById(R.id.pitch);
		dotET = (EditText)findViewById(R.id.dot);
		paraET = (EditText)findViewById(R.id.para);
		statusTV = (TextView)findViewById(R.id.statusLbl);
		speakCB = (CheckBox)findViewById(R.id.speak);
		saveTextCB = (CheckBox)findViewById(R.id.saveText);
		replaceET = (EditText)findViewById(R.id.replace);
		byET = (EditText)findViewById(R.id.by);
		isRegexCB = (CheckBox) findViewById(R.id.regex);
        caseSensitiveCB = (CheckBox) findViewById(R.id.caseSensitive);
		toWavCB = (CheckBox) findViewById(R.id.toWav);
		volProgressTV = (TextView) findViewById(R.id.progress);
        volBar = (SeekBar) findViewById(R.id.vol);
        volBar.setOnSeekBarChangeListener(this);
		//AudioManager am = (AudioManager) getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		//int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		//volBar.setMax(max);
		//volBar.setProgress(vol);
		//volBar.setSecondaryProgress(max/2);

		if (tts == null) {
			Object sel = langSpinner.getSelectedItem();
			tts = new TextToSpeech(getApplicationContext(), new OnInitListener(this, sel == null ? "" : sel.toString()));
		}
//		if (curFile == null) {
		startBtn.setImageResource(R.drawable.exo_controls_play);
		startBtn.setTag("Play");
//		} else {
//			startBtn.setImageResource(R.drawable.exo_controls_play);
//		}
		if (savedInstanceState == null) {
			restore();
		}
		Log.i(TAG, "onCreate vol" + vol + ", " + volProgress + ", " + curFile + ", " + partNo + ", " + offset);
		if (tts.isSpeaking() || (ttsTask != null && ttsTask.getStatus() == AsyncTask.Status.RUNNING)) {
			startBtn.setImageResource(R.drawable.exo_controls_pause);
			startBtn.setTag("Pause");
		}
		textET.postDelayed(new Runnable() {

				@Override
				public void run() {
					Log.i(TAG, "DefaultLanguage " + tts.getDefaultLanguage());
					Log.i(TAG, "DefaultEngine " + tts.getDefaultEngine());
					Log.i(TAG, "DefaultVoice " + tts.getDefaultVoice());
					Log.i(TAG, "Language " + tts.getLanguage());
					Log.i(TAG, "Voice " + tts.getVoice());
					Log.i(TAG, "MaxSpeechInputLength " + tts.getMaxSpeechInputLength());
					Log.i(TAG, Util.collectionToString(tts.getFeatures(new Locale("vi_VN")), false, "\n") + ".");
					
					if (langList.size() == 0) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							Set<Locale> locales = tts.getAvailableLanguages();
							if (locales != null) {
								for (Locale l : locales) {
									Log.i(TAG, l + " " + tts.isLanguageAvailable(l));
									if (tts.isLanguageAvailable(l) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
										langList.add(l.toString());
									}
								}
								Collections.sort(langList);
							} else {
								getLocales();
							}
						} else {
							getLocales();
						}
					}
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						textET.getContext(), android.R.layout.simple_spinner_item, langList);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					langSpinner.setAdapter(adapter);
					langSpinner.setOnItemSelectedListener(
						new OnItemSelectedListener() {
							public void onItemSelected(
								AdapterView<?> parent, View view, int position, long id) {
									lang = langSpinner.getSelectedItem() + "";
									Log.i(TAG, "on Lang " + lang);
								//((MainActivity)TTSFragment.this.getActivity()).showToast("Spinner1: position=" + position + " id=" + id);
							}

							public void onNothingSelected(AdapterView<?> parent) {
								TTSActivity.this.showToast("Language: unselected");
							}
						});
					int i = 0;
					Log.i(TAG, "langET.onCreateView " + langSpinner.getSelectedItemPosition() + ", " + lang);
					for (String st : langList) {
						if (st.equals(lang)) {
							langSpinner.setSelection(i);
						}
						i++;
					}
					Log.i(TAG, "langET.onCreateView " + langSpinner.getSelectedItemPosition() + ", " + lang);
					
				}

				private void getLocales() {
					Locale[] locales = Locale.getAvailableLocales();
					for (Locale l : locales) {
						String loc = l.toString();
						if (loc.length() <= 5) {
							int result = tts.isLanguageAvailable(l);

							if (//result == TextToSpeech.LANG_AVAILABLE ||
								result == TextToSpeech.LANG_COUNTRY_AVAILABLE //||
								//result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
								) {// && result != TextToSpeech. LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
								langList.add(loc);
								Log.d(TAG, "LanguageAvailable " + loc + " " + result);
							}
						}
					}
				}
			}, 1000 );

		Log.i(TAG, "TTSFragment files1" + files);
		if (files != null && files.length() > 0) {
			String[] sts = Util.stringToArray(fileET.getText().toString(), "|");//files.split("|");
			//Arrays.sort(sts);
			filesBtn.setOnClickListener(new GetFileListener(this, ALL_SUFFIX_TITLE, ALL_SUFFIX, sts, FILES_REQUEST_CODE, MULTI_FILES));
		} else {
			filesBtn.setOnClickListener(new GetFileListener(this, ALL_SUFFIX_TITLE, ALL_SUFFIX, "", FILES_REQUEST_CODE, MULTI_FILES));
		}
		
        startBtn.setOnClickListener(new OkBtnListener(this, mListener));

        //cancelBtn.setOnClickListener(new CancelBtnListener(this, mListener));

		saveToBtn.setOnClickListener(new GetFileListener(this, "Output Folder", "", saveToET.getText().toString(), SAVETO_REQUEST_CODE, !MULTI_FILES));

		stopBtn.setOnClickListener(new Button1Listener(this, mListener));
		
        //return view;
    }

	void save() {
		Log.i(TAG, "save, lang " + lang + Util.collectionToSlashString(langList));
		Log.i(TAG, "save1 vol " + vol + ", " + volProgress + ", " + curFile + ", " + partNo + ", " + offset);
		files = fileET.getText().toString();
		saveTo = saveToET.getText().toString();
		text = textET.getText().toString().trim();
		lang = langSpinner.getSelectedItem() + "";
		Log.i(TAG, "langET.save " + lang);
		speak = speakCB.isChecked();
		speed = speedET.getText().toString();
		pitch = pitchET.getText().toString();
		dot = dotET.getText().toString();
		para = paraET.getText().toString();
		saveText = saveTextCB.isChecked();
		isRegex = isRegexCB.isChecked();
		caseSensitive = caseSensitiveCB.isChecked();
		toWav = toWavCB.isChecked();
		replace = replaceET.getText().toString();
		by = byET.getText().toString();
		vol = volBar.getProgress();
		volProgress = volProgressTV.getText().toString();
		Log.i(TAG, "save2 vol" + vol + ", " + volProgress + ", " + curFile + ", " + partNo + ", " + offset);
	}
	
	void restore() {
		Log.i(TAG, "restore, project " + lang + Util.collectionToSlashString(langList));
		Log.i(TAG, "restore1 vol " + vol + ", " + volProgress + ", " + curFile + ", " + partNo + ", " + offset);
		fileET.setText(files);
		saveToET.setText(saveTo);
		textET.setText(text);
		int i = 0;
		for (String st : langList) {
			if (st.equals(lang)) {
				langSpinner.setSelection(i);
			}
			i++;
		}
		Log.i(TAG, "lang, restore " + langSpinner.getSelectedItemPosition() + ", " +  lang + "." + Util.collectionToSlashString(langList));
		speedET.setText(speed);
		pitchET.setText(pitch);
		dotET.setText(dot);
		paraET.setText(para);
		speakCB.setChecked(speak);
		saveTextCB.setChecked(saveText);
        replaceET.setText(replace);
		byET.setText(by);
		toWavCB.setChecked(toWav);
		isRegexCB.setChecked(isRegex);
		caseSensitiveCB.setChecked(caseSensitive);
		volBar.setProgress(vol);
		volProgressTV.setText(volProgress);
		Log.i(TAG, "restore2 vol " + vol + ", " + volProgress + ", " + curFile + ", " + partNo + ", " + offset);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			if (fileET != null) {
				fileET.setText(savedInstanceState.getString("Files"));
				saveToET.setText(savedInstanceState.getString("SaveTo"));
				textET.setText(savedInstanceState.getString("Text"));
				langSpinner.setSelection(savedInstanceState.getInt("Lang"));
				speedET.setText(savedInstanceState.getString("Speed"));
				pitchET.setText(savedInstanceState.getString("Pitch"));
				dotET.setText(savedInstanceState.getString("Dot"));
				paraET.setText(savedInstanceState.getString("Para"));
				speakCB.setChecked(savedInstanceState.getBoolean("speak"));
				saveTextCB.setChecked(savedInstanceState.getBoolean("saveText"));
				isRegexCB.setChecked(savedInstanceState.getBoolean("isRegex", false));
				caseSensitiveCB.setChecked(savedInstanceState.getBoolean("caseSensitive", false));
				toWavCB.setChecked(savedInstanceState.getBoolean("toWav", false));
				replaceET.setText(savedInstanceState.getString("replace"));
				byET.setText(savedInstanceState.getString("by"));
				volProgressTV.setText(savedInstanceState.getString("volProgressTV"));
				volBar.setProgress(savedInstanceState.getInt("volBar"));
			} 
			langList = savedInstanceState.getStringArrayList("projectList");
			lang = savedInstanceState.getString("langu");
			volProgress = savedInstanceState.getString("volProgressTV");
			vol = savedInstanceState.getInt("volBar");
			curFile = savedInstanceState.getString("curFile");
			partNo = savedInstanceState.getInt("partNo");
			offset = savedInstanceState.getInt("offset");
			prevFiles = savedInstanceState.getString("prevFiles");
			prevText = savedInstanceState.getString("prevText");
			prevTextF = savedInstanceState.getString("prevTextF");
        }
	}
	
	@Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
		super.onSaveInstanceState(saveInstanceState);
		Log.i(TAG, "onSaveInstanceState " + saveInstanceState);
        if (saveInstanceState == null) {
            return;
        }
        Log.i(TAG, "onSaveInstanceState vol " + vol + ", " + volProgress + ", " + curFile + ", " + partNo + ", " + offset);
		saveInstanceState.putString("Files", fileET.getText() + "");
		saveInstanceState.putString("SaveTo", saveToET.getText() + "");
		saveInstanceState.putString("Text", textET.getText() + "");
		saveInstanceState.putInt("Lang", langSpinner.getSelectedItemPosition());
		saveInstanceState.putString("langu", lang);
		Log.i(TAG, "onSaveInstanceState " + langSpinner.getSelectedItemPosition() + ", " + lang);
		saveInstanceState.putString("Speed", speedET.getText() + "");
		saveInstanceState.putString("Pitch", pitchET.getText() + "");
		saveInstanceState.putBoolean("speak", speakCB.isChecked());
		saveInstanceState.putBoolean("saveText", saveTextCB.isChecked());
		saveInstanceState.putBoolean("isRegex", isRegexCB.isChecked());
		saveInstanceState.putBoolean("caseSensitive", caseSensitiveCB.isChecked());
		saveInstanceState.putBoolean("toWav", toWavCB.isChecked());
		saveInstanceState.putString("replace", replaceET.getText() + "");
		saveInstanceState.putString("by", byET.getText() + "");
		saveInstanceState.putString("Dot", dotET.getText() + "");
		saveInstanceState.putString("Para", paraET.getText() + "");
		saveInstanceState.putStringArrayList("projectList", langList);
		saveInstanceState.putInt("volBar", volBar.getProgress());
		saveInstanceState.putString("volProgressTV", volProgressTV.getText() + "");
		saveInstanceState.putString("curFile", curFile);
		saveInstanceState.putInt("partNo", partNo);
		saveInstanceState.putInt("offset", offset);
		saveInstanceState.putString("prevFiles", prevFiles);
		saveInstanceState.putString("prevText", prevText);
		saveInstanceState.putString("prevTextF", prevTextF);
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
            //Log.d(TAG, "TTS Response: "+requestCode);
            if (requestCode == CHECK_TTS_AVAILABILITY) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // success, create the TTS instance
                    //this.mTts = new TextToSpeech(this, this);
                } else {
                    // missing data, install it
                    Toast.makeText(this, NO_TTS_ANDROID_MARKET_REDIRECT, Toast.LENGTH_LONG).show();

                    PackageManager pm = getPackageManager();
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    ResolveInfo resolveInfo = pm.resolveActivity( installIntent, PackageManager.MATCH_DEFAULT_ONLY );

                    if( resolveInfo == null ) {
						// Not able to find the activity which should be started for this intent
                        Toast.makeText(this, NO_TTS_AVAILABLE, Toast.LENGTH_LONG).show();
                    } else {
						startActivity( installIntent );
                    }
                    //finish();
                }
            } 
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            //finish();
        }
	}

//	@Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//		Log.i(TAG, "onAttach");
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//										 + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//		Log.i(TAG, "onDetach");
////        mListener = null;
//    }

//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		tts.shutdown();
//	}

	@Override
	public void onOk(FragmentActivity fra) {
		Log.i(TAG, "onOk " + startBtn.getTag());

		save();
		if ("Pause".equals(startBtn.getTag().toString())) {
			// pausing
			command = "pause";
			cancel = true;
			if (ttsTask != null) {
				ttsTask.cancel(true);
				ttsTask.publishProgress("");
			}
			tts.stop();
			startBtn.setImageResource(R.drawable.exo_controls_play);
			startBtn.setTag("Play");
			stopService(new Intent(this, ForegroundService.class));
			return;
		}
		if (speakCB.isChecked()) {
			startBtn.setImageResource(R.drawable.exo_controls_pause);
			startBtn.setTag("Pause");
		}
		if (speed.length() == 0 && pitch.length() == 0 || "0".equals(speed) && "0".equals(pitch)) {
			showToast("Invalid speed or pitch");
			return;
		}
		if (dot.length() == 0 && para.length() == 0 || "0".equals(dot) && "0".equals(para)) {
			showToast("Invalid dot pause or paragraph");
			return;
		}
		if (lang.length() <= 3) {
			showToast("Invalid language");
			return;
		}
		if (files.trim().length() == 0 && text.trim().length() == 0 || "0".equals(dot) && "0".equals(para)) {
			showToast("Invalid files or text");
			return;
		}
		String[] split = lang.split("[- _/]");
		if (split.length > 1) {
			int result = tts.setLanguage(new Locale(split[0], split[1]));
			if (result == TextToSpeech. LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, lang + " is not supported", Toast.LENGTH_SHORT).show();
				Log.e ("error", lang + " is not supported");
				return;
			}
		} else {
			showToast("Invalid language");
			return;
		}
		tts.setSpeechRate(Float.parseFloat(speedET.getText().length() > 0 ? speedET.getText().toString() : "1"));
		tts.setPitch(Float.parseFloat(pitchET.getText().length() > 0 ? pitchET.getText().toString() : "1"));

		if (!speak && !toWav) {
			showToast("Speak and/or Convert to Wav?");
			return;
		}
		String st = null;
		if (!prevFiles.equals(files) 
			|| !prevText.equalsIgnoreCase(text)) {
//				ttsFrag.curFile = null;
//				ttsFrag.partNo = -1;
//				ttsFrag.offset = -1;
			if (text.length() > 0
				&& !prevText.equalsIgnoreCase(text)) {
				try {
//					if (mNotificationManager == null) {
//						mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//					}
//					createNotification("Searcher", "Touch to Open", false, "Text to Speech");
					String fPath = SearcherAplication.PRIVATE_PATH + "/tts." + Util.dtf.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|\\\\]+", "_") + ".txt";
					FileUtil.writeFileAsCharset(fPath, text, "utf-8");
					new File(fPath).deleteOnExit();
					prevTextF = fPath;
					st = fPath + "|" + files;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (text.length() > 0
					   && prevText.equalsIgnoreCase(text)) {
				st = prevTextF + "|" + files;
			} else {
				st = fileET.getText().toString();
			}
			prevFiles = files;
			prevText = text;
		} else {
			st = prevTextF + "|" + files;
		}
		//ttsFrag.text = ttsFrag.textET.getText().toString().trim();
		Log.d(TAG, "tts st " + st);
		if (retainFrag.callBackTask != null) {
			retainFrag.callBackTask.cancel(true);
		}
		ttsTask = new TTSTask(this, retainFrag);
		retainFrag.callBackTask = ttsTask;
		retainFrag.oriList = Util.stringToList(st, "|");//mPrefs.mDirList;
		retainFrag.statusTV = statusTV;
		retainFrag.mTask = new ConvertTask(retainFrag, ttsTask);//SettingsFragment.this);
		retainFrag.mTask.execute();
//		curFrag.selectedFiles = Util.stringToArray(st, "|");
//		curFrag.requestTts = true;
//		curFrag.stopReadAndSearch();
//		curFrag.currentZipFileName = ""; // làm dấu để khỏi show web getSourceFile
//		curFrag.load = "Search";
//		curFrag.requestCompare = false;
//		curFrag.requestSearching = false;
//		curFrag.getSourceFileTask = new GetSourceFileTask(curFrag);
//		curFrag.getSourceFileTask.execute();
		return;

	}


//	@Override
//	public void onCancelChooser(FragmentActivity path) {
//		
//	}

	@Override
	public void onButton1(FragmentActivity path) {
		Log.i(TAG, "onButton1 " );
		command = "";
		cancel = true;
		if (ttsTask != null) {
			ttsTask.cancel(true);
			ttsTask.publishProgress("");
		}
		//save();
		curFile = null;
		partNo = -1;
		offset = -1;
		tts.stop();
		statusTV.setText("");
		startBtn.setImageResource(R.drawable.exo_controls_play);
		startBtn.setTag("Play");
		stopService(new Intent(this, ForegroundService.class));
		return;

	}

//	@Override
//	public void onButton2(FragmentActivity path) {
//		
//	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

//		AudioManager am = (AudioManager) getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		Log.i(TAG, "onProgressChanged vol" + vol + ", " + volProgress + ", " + progress + "/ " + max + ", " +curFile + ", " + partNo + ", " + offset);

		if (ttsTask != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				ttsTask.myBundleAlarm.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,
											   (float)progress / 100);
			} else {
				ttsTask.myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_VOLUME,
										String.valueOf((float)progress / 100));
			}
		}
		volProgress = progress + "%";
		volProgressTV.setText(volProgress);
		vol = progress;
		//am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
		Log.i(TAG, "onProgressChanged vol" + vol + ", " + volProgress + ", " + progress + "/ " + 100 + ", " + curFile + ", " + partNo + ", " + offset);
		//throw new RuntimeException(TAG);
    }

	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //mTrackingText.setText(getString(R.string.seekbar_tracking_on));
    }

	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //mTrackingText.setText(getString(R.string.seekbar_tracking_off));
    }
	
	
}
