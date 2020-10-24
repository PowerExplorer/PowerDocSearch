package net.gnu.agrep;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import net.gnu.agrep.R;
import android.util.Log;

public class OptionActivity extends PreferenceActivity {
    final public static int DefaultHighlightColor=0xFF00FFFF;

    private PreferenceScreen mPs = null;
    private PreferenceManager mPm;
    private Prefs  mPrefs;

    final private static int REQUEST_CODE_HIGHLIGHT = 0x1000;
    final private static int REQUEST_CODE_BACKGROUND = 0x1001;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPrefs = Prefs.loadPrefes(this);

        mPm = getPreferenceManager();
        mPs = mPm.createPreferenceScreen(this);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); {
            final Preference pr = new Preference(this);
            // set Version Name to title field
            try {
                pr.setTitle(getString(R.string.version, getPackageManager()
									  .getPackageInfo(getPackageName(), 0).versionName));
            } catch (NameNotFoundException e) {
            }
            pr.setSummary(R.string.link);
            pr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=net.gnu.agrep"));
						startActivity(intent);
						return true;
					}
				});
            mPs.addPreference(pr);
        } {
            // フォントサイズ
            final ListPreference pr = new ListPreference(this);
            pr.setKey(Prefs.KEY_FONTSIZE);
            pr.setSummary(sp.getString(pr.getKey(), ""));
            pr.setTitle(R.string.label_font_size);
            pr.setEntries(new String[] { "10","12", "14","15", "16", "18", "20",  });
            pr.setEntryValues(new String[] { "10","12", "14", "15","16", "18", "20",  });
            mPs.addPreference(pr);
        }
        createHighlightPreference(R.string.label_highlight_bg , REQUEST_CODE_HIGHLIGHT);
        createHighlightPreference(R.string.label_highlight_fg , REQUEST_CODE_BACKGROUND);
		{
            // Add Linenumber
            final CheckBoxPreference pr = new CheckBoxPreference(this);
            pr.setKey(Prefs.KEY_ADD_LINENUMBER);
            pr.setSummary(R.string.summary_add_linenumber);
            pr.setTitle(R.string.label_add_linenumber);
            mPs.addPreference(pr);
        }
//		{
//            final Preference pr = new Preference(this);
//            pr.setTitle(R.string.icondesign);
//            pr.setSummary(R.string.iconsite);
//            pr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//					@Override
//					public boolean onPreferenceClick(Preference preference) {
//						Intent intent = new Intent(Intent.ACTION_VIEW);
//						intent.setData(Uri.parse(getString(R.string.iconlink)));
//						startActivity(intent);
//						return true;
//					}
//				});
//            mPs.addPreference(pr);
//        }
        setPreferenceScreen(mPs);

    }


    private void createHighlightPreference(final int resid , final int reqCode) {
        final Preference pr = new Preference(this);
        pr.setTitle(resid);

        pr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(OptionActivity.this , ColorPickerActivity.class);
					intent.putExtra(ColorPickerActivity.EXTRA_TITLE, getString(resid));
					startActivityForResult(intent, reqCode);
					return true;
				}
			});

        mPs.addPreference(pr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            int color = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR, 0x00FFFF);
            if (requestCode == REQUEST_CODE_HIGHLIGHT) {
                mPrefs.mHighlightFg = color;
            } else if (requestCode == REQUEST_CODE_BACKGROUND) {
                mPrefs.mHighlightBg = color;
            }
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.edit()
				.putInt(Prefs.KEY_HIGHLIGHTFG, mPrefs.mHighlightFg)
				.putInt(Prefs.KEY_HIGHLIGHTBG, mPrefs.mHighlightBg)
				.apply();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
		Log.d("OptionActivity", "id " + id + ", home " + android.R.id.home);
        switch (id) {
            case android.R.id.home:
				setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
