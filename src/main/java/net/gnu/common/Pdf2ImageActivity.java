package net.gnu.common;

import java.io.Serializable;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import android.widget.LinearLayout;
import com.jaredrummler.android.colorpicker.ColorPickerView;
import android.view.KeyEvent;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.SeekBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.app.Fragment;
import android.widget.ImageButton;
import net.gnu.agrep.R;
import android.support.v4.app.*;
import java.io.*;
import android.app.*;

/**
 * Activities that contain this fragment must implement the
 * {@link net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link net.rdrei.android.dirchooser.DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Pdf2ImageActivity extends CommonActivity 
implements  Serializable, ColorPickerView.OnColorChangedListener,
RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, OnFragmentInteractionListener {


	private static transient final long serialVersionUID = 7105871643003630949L;

	private static transient final String TAG = "Pdf2ImageActivity";
	
	private String zoom = "100";
	private boolean extractImages;
	
	private String left = "0";
	private String top = "0";
	private String right = "0";
	private String bottom = "0";
	private int background = 0;
	private int type = R.id.webp;
	private int rate = 95;
	
	private transient ImageButton mBtnConfirm;
	//private transient ImageButton mBtnCancel;
	private transient ImageButton filesBtn;
	private transient ImageButton saveToBtn;
	private transient EditText zoomET;
	private transient CheckBox extractImagesCB;
	
	private transient EditText leftET;
	private transient EditText topET;
	private transient EditText rightET;
	private transient EditText bottomET;
	private transient EditText backgroundET;
	private transient SeekBar rateSB;
	private transient RadioGroup typeRG;
	private transient View rateLl;
	private transient TextView volProgressTV;
    
	private transient ColorPickerView colorPickerView;
	private transient ColorPanelView newColorPanelView;
	
	private transient OnFragmentInteractionListener mListener = this;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e(TAG, "onSaveInstanceState");
		if (outState == null) {
			return;
		}
		
		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
		outState.putString("zoom", zoomET.getText() + "");
		outState.putBoolean("extractImages", extractImagesCB.isChecked());
		
		outState.putString("left", leftET.getText() + "");
		outState.putString("top", topET.getText() + "");
		outState.putString("right", rightET.getText() + "");
		outState.putString("bottom", bottomET.getText() + "");
		outState.putInt("background", colorPickerView.getColor());
		outState.putInt("type", typeRG.getCheckedRadioButtonId());
		outState.putInt("rate", rateSB.getProgress());
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
		super.onCreate(savedInstanceState);
//        if (this.getShowsDialog()) {
//            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
//        } else {
//            setHasOptionsMenu(true);
//        }
//    }
//	
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		Log.e(TAG, "onCreateView " + savedInstanceState);
//		super.onCreateView(inflater, container, savedInstanceState);
//		return inflater.inflate(R.layout.pdf2image, container, false);
//	}
//		
//	@Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        Log.e(TAG, "onViewCreated " + savedInstanceState);
//		super.onViewCreated(view, savedInstanceState);
		//getDialog().setTitle("PDFs To Images");
		setContentView(R.layout.pdf2image);
		
		final View customView = getLayoutInflater().inflate(R.layout.filechoosertoolbar, null);
		((TextView)customView.findViewById(R.id.title)).setText("PDFs to Images");
		customView.findViewById(R.id.cancelDir).setVisibility(View.INVISIBLE);
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(customView);
		
		
		mBtnConfirm = (ImageButton) findViewById(R.id.okDir);
		//mBtnCancel = (ImageButton) findViewById(R.id.cancelDir);
		fileET = (EditText) findViewById(R.id.files);
		saveToET = (EditText) findViewById(R.id.saveTo);
		backgroundET = (EditText) findViewById(R.id.background);
		
		leftET = (EditText) findViewById(R.id.left);
		topET = (EditText) findViewById(R.id.top);
		rightET = (EditText) findViewById(R.id.right);
		bottomET = (EditText) findViewById(R.id.bottom);

		zoomET = (EditText) findViewById(R.id.zoom);
		extractImagesCB = (CheckBox) findViewById(R.id.extractImages);

		filesBtn = (ImageButton) findViewById(R.id.filesBtn);
		saveToBtn = (ImageButton) findViewById(R.id.saveToBtn);
		
		rateLl = findViewById(R.id.rateLl);
		rateSB = (SeekBar) findViewById(R.id.rate);
		volProgressTV = (TextView) findViewById(R.id.progress);
        typeRG = (RadioGroup) findViewById(R.id.type);
		Log.d(TAG, "typeRG " + typeRG.getCheckedRadioButtonId());
		rateSB.setOnSeekBarChangeListener(this);
		typeRG.setOnCheckedChangeListener(this);
		volProgressTV.setText(rateSB.getProgress() + "");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int initialColor = prefs.getInt("color_3" , 0xFFFFFFFF);
		colorPickerView = (ColorPickerView) findViewById(R.id.cpv_color_picker_view);
		ColorPanelView colorPanelView = (ColorPanelView) findViewById(R.id.cpv_color_panel_old);
		newColorPanelView = (ColorPanelView) findViewById(R.id.cpv_color_panel_new);

		((LinearLayout) colorPanelView.getParent()).setPadding(colorPickerView.getPaddingLeft(), 0 ,
															   colorPickerView.getPaddingRight(), 0);
		colorPickerView.setOnColorChangedListener(this);
		colorPickerView.setColor(initialColor, true);
		colorPanelView.setColor(initialColor);

		//restore();
		if (savedInstanceState != null && fileET != null) {
			fileET.setText(savedInstanceState.getString("Files"));
			saveToET.setText(savedInstanceState.getString("SaveTo"));
			zoomET.setText(savedInstanceState.getString("zoom"));
			extractImagesCB.setChecked(savedInstanceState.getBoolean("extractImages"));
			typeRG.check(savedInstanceState.getInt("type"));
			rateSB.setProgress(savedInstanceState.getInt("rate"));
			volProgressTV.setText(rateSB.getProgress() + "");
			leftET.setText(savedInstanceState.getString("left"));
			topET.setText(savedInstanceState.getString("top"));
			rightET.setText(savedInstanceState.getString("right"));
			bottomET.setText(savedInstanceState.getString("bottom"));
			backgroundET.setText(Integer.toHexString(savedInstanceState.getInt("background")));
			colorPickerView.setColor(savedInstanceState.getInt("background"), false);
		}
		filesBtn.setOnClickListener(new GetFileListener(this, "Pdf files", ".pdf", fileET.getText().toString(), FILES_REQUEST_CODE, !MULTI_FILES));
		saveToBtn.setOnClickListener(new GetFileListener(this, "Save To", "", saveToET.getText().toString(), SAVETO_REQUEST_CODE, !MULTI_FILES));
		mBtnConfirm.setOnClickListener(new OkBtnListener(this, mListener));

		//mBtnCancel.setOnClickListener(new CancelBtnListener(this, mListener));
		backgroundET.addTextChangedListener(new TextWatcher() {
				public void beforeTextChanged(CharSequence s, int start, int end, int count) {
				}

				public void afterTextChanged(final Editable text) {
					if (text.length() > 0) {
						try {
							final int parseInt = Integer.parseInt(text.toString(), 16);
							colorPickerView.setColor(parseInt, false);
							newColorPanelView.setColor(parseInt);
							SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(Pdf2ImageActivity.this).edit();
							edit.putInt("color_3" , colorPickerView.getColor());
							edit.apply();
						} catch (NumberFormatException nfe) {
							Log.e(TAG, nfe.getMessage());
						}
					}
					
				}
				
				public void onTextChanged(CharSequence s, int start, int end, int count) {
				}
			});
	}
	
	@Override 
	public void onColorChanged (int newColor) {
		newColorPanelView.setColor(colorPickerView.getColor());
		SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		edit.putInt("color_3" , colorPickerView.getColor());
		backgroundET.setText(Integer.toHexString(colorPickerView.getColor()));
		edit.apply();
	}

	public void restore() {
		fileET.setText(files);
		saveToET.setText(saveTo);
		zoomET.setText(zoom);
		extractImagesCB.setChecked(extractImages);
		backgroundET.setText(Integer.toHexString(background));
		
		leftET.setText(left);
		topET.setText(top);
		rightET.setText(right);
		bottomET.setText(bottom);
		colorPickerView.setColor(background, false);
		
		typeRG.check(type);
		rateSB.setProgress(rate);
		volProgressTV.setText(rateSB.getProgress() + "");
	}

	public void save() {
		files = fileET.getText().toString();
		saveTo = saveToET.getText().toString();
		zoom = zoomET.getText().toString();
		extractImages = extractImagesCB.isChecked();
		
		left = leftET.getText().toString();
		top = topET.getText().toString();
		right = rightET.getText().toString();
		bottom = bottomET.getText().toString();
		background = colorPickerView.getColor();
		
		type = typeRG.getCheckedRadioButtonId();
		rate = rateSB.getProgress();
	}

	@Override
	public void onCheckedChanged(RadioGroup p1, int p2) {
		int t = p1.getCheckedRadioButtonId();
		if (t != R.id.png) {
			rateLl.setVisibility(View.VISIBLE);
		} else {
			rateLl.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		volProgressTV.setText(progress + "%");
		rate = progress;
    }

	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
	
	
//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		Log.e(TAG, "onAttach");
//		try {
//			mListener = (OnFragmentInteractionListener) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity.toString()
//										 + " must implement OnFragmentInteractionListener");
//		}
//	}
//
//	@Override
//	public void onDetach() {
//		super.onDetach();
//		Log.e(TAG, "onDetach");
//		mListener = null;
//	}
	
	@Override
	public void onOk(FragmentActivity path) {
		save();
		File oriF = new File(files);
		File modiF = new File(saveTo);
		if ((!oriF.exists() || files.length() == 0)) {
			showToast("Pdf file is not existed or empty");
			return;
		} else if (!modiF.exists()) {
			showToast("Target folder is not existed");
			return;
		} 
		new Pdf2ImageTask(this, files, saveTo, zoom, extractImages, left, top, right, bottom, background, type, rate).execute();
		
	}

//	@Override
//	public void onCancelChooser(DialogFragment fra) {
//		Log.i(TAG, "onCancelChooser ");
//		fra.dismiss();
//	}
	
	@Override
	public void onButton1(FragmentActivity fra) {
		
	}
	
//	@Override
//	public void onButton2(DialogFragment fra) {
//		Log.i(TAG, "onButton2 ");
//		fra.dismiss();
//		showToast("Nothing to do");
//	}
	
	
}
