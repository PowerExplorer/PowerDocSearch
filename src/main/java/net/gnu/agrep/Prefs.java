package net.gnu.agrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.Collection;


public class Prefs {
	
    private static final String TAG = "Prefs";

    public static final String KEY_IGNORE_CASE = "IgnoreCase";
    public static final String KEY_REGULAR_EXPRESSION = "RegularExpression";
    public static final String KEY_TARGET_EXTENSIONS_OLD = "TargetExtensions";
    public static final String KEY_TARGET_DIRECTORIES_OLD = "TargetDirectories";
    public static final String KEY_TARGET_EXTENSIONS_NEW = "TargetExtensionsNew";
    public static final String KEY_TARGET_DIRECTORIES_NEW = "TargetDirectoriesNew";
    public static final String KEY_FONTSIZE = "FontSize";
    public static final String KEY_HIGHLIGHTFG = "HighlightFg";
    public static final String KEY_HIGHLIGHTBG = "HighlightBg";
    public static final String KEY_ADD_LINENUMBER = "AddLineNumber";
	public static final String KEY_TAB_COUNT = "TabCount";

    private static final String PREF_RECENT= "recent";

    boolean mRegularExrpression = false;
    boolean mIgnoreCase = true;
    int mFontSize = 15;
    int mHighlightBg = 0xFF00FFFF;
    int mHighlightFg = 0xFF000000;
    boolean addLineNumber=false;
	int tabCount = 0;
    Collection<CheckedString> mDirList = new ArrayList<CheckedString>();
    ArrayList<CheckedString> mExtList = new ArrayList<CheckedString>();

	static public Prefs loadPrefes(final Context ctx) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        final Prefs prefs = new Prefs();

		prefs.tabCount = sp.getInt(KEY_TAB_COUNT, 1);

        // target directory
        prefs.mDirList	=  new ArrayList<CheckedString>();

        // target extensions
        prefs.mExtList	=  new ArrayList<CheckedString>();
        
        prefs.mFontSize = Integer.parseInt(sp.getString(KEY_FONTSIZE, "16"));
        prefs.mHighlightFg = sp.getInt(KEY_HIGHLIGHTFG, 0xFF000000);
        prefs.mHighlightBg = sp.getInt(KEY_HIGHLIGHTBG, 0xFF00FFFF);

        prefs.addLineNumber = sp.getBoolean(KEY_ADD_LINENUMBER, true);
        return prefs;
    }

    static public Prefs loadPrefes(final Context ctx, final int no) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        final Prefs prefs = new Prefs();

		prefs.tabCount = sp.getInt(KEY_TAB_COUNT, 1);

        // target directory
        String dirs = sp.getString(KEY_TARGET_DIRECTORIES_NEW + no, "");
        Log.d(TAG, "loadPrefes " + no + ", " + dirs);
		prefs.mDirList	=  new ArrayList<CheckedString>();
        if (dirs.length() > 0) {
            String[] dirsarr = dirs.split("\\|");
            int size = dirsarr.length;
            for (int i=0;i < size;i += 2) {
                boolean c = dirsarr[i].equals("true");
                String s = dirsarr[i + 1];
                prefs.mDirList.add(new CheckedString(c, s));
            }
        } else {
            dirs = sp.getString(KEY_TARGET_DIRECTORIES_OLD + no, "");
            if (dirs.length() > 0) {
                String[] dirsarr = dirs.split("\\|");
                int size = dirsarr.length;
                for (int i=0;i < size;i++) {
                    prefs.mDirList.add(new CheckedString(dirsarr[i]));
                }
            }
        }
        // target extensions
        String exts = sp.getString(KEY_TARGET_EXTENSIONS_NEW + no, "");
        Log.d(TAG, "loadPrefes " + no + ", "+ exts);
		prefs.mExtList	=  new ArrayList<CheckedString>();
        if (exts.length() > 0) {
            String[] arr = exts.split("\\|");
            int size = arr.length;
            for (int i=0;i < size;i += 2) {
                boolean c = arr[i].equals("true");
                String s = arr[i + 1];
                prefs.mExtList.add(new CheckedString(c, s));
            }
        } else {
            exts = sp.getString(KEY_TARGET_EXTENSIONS_OLD + no, "txt|html|htm|docx|doc|odt|pdf|zip|7z|rar");
            if (exts.length() > 0) {
                String[] arr = exts.split("\\|");
                int size = arr.length;
                for (int i=0;i < size;i++) {
                    prefs.mExtList.add(new CheckedString(arr[i]));
                }
            }
        }

        prefs.mRegularExrpression = sp.getBoolean(KEY_REGULAR_EXPRESSION + no, false);
        prefs.mIgnoreCase = sp.getBoolean(KEY_IGNORE_CASE + no, true);

        prefs.mFontSize = Integer.parseInt(sp.getString(KEY_FONTSIZE, "16"));
        prefs.mHighlightFg = sp.getInt(KEY_HIGHLIGHTFG, 0xFF000000);
        prefs.mHighlightBg = sp.getInt(KEY_HIGHLIGHTBG, 0xFF00FFFF);

        prefs.addLineNumber = sp.getBoolean(KEY_ADD_LINENUMBER, true);
        return prefs;
    }

    public void savePrefs(Context context, final int no) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		
        final Editor editor = sp.edit();

        // target directory
        final StringBuilder dirs = new StringBuilder();
        for (CheckedString t : mDirList) {
            dirs.append(t.checked);
            dirs.append('|');
            dirs.append(t.string);
            dirs.append('|');
        }
        if (dirs.length() > 0) {
            dirs.deleteCharAt(dirs.length() - 1);
        }
		Log.i(TAG, "savePrefs " + no + ", "+ dirs);
		
        // target extensions
        final StringBuilder exts = new StringBuilder();
        for (CheckedString t : mExtList) {
            exts.append(t.checked);
            exts.append('|');
            exts.append(t.string);
            exts.append('|');
        }
        if (exts.length() > 0) {
            exts.deleteCharAt(exts.length() - 1);
        }
		Log.i(TAG, "savePrefs " + no + ", "+ exts);
		
        editor.putString(KEY_TARGET_DIRECTORIES_NEW + no, dirs.toString());
        editor.putString(KEY_TARGET_EXTENSIONS_NEW + no, exts.toString());
        editor.remove(KEY_TARGET_DIRECTORIES_OLD + no);
        editor.remove(KEY_TARGET_EXTENSIONS_OLD + no);
        editor.putBoolean(KEY_REGULAR_EXPRESSION + no, mRegularExrpression);
        editor.putBoolean(KEY_IGNORE_CASE + no, mIgnoreCase);

        editor.apply();

    }

    public void addRecent(Context context, int index, String searchWord) {
        // 書き出し
        final SharedPreferences rsp = context.getSharedPreferences(PREF_RECENT+index, Context.MODE_PRIVATE);
        Editor reditor = rsp.edit();
        reditor.putLong(searchWord, System.currentTimeMillis());
        reditor.apply();
    }

    public void clearRecent(Context context, int index) {
        final SharedPreferences rsp = context.getSharedPreferences(PREF_RECENT+index, Context.MODE_PRIVATE);
        Editor reditor = rsp.edit();
        reditor.clear();
        reditor.apply();
    }

    public ArrayList<String> getRecent(Context context, int index) {
        // ロード
        final SharedPreferences rsp = context.getSharedPreferences(PREF_RECENT+index, Context.MODE_PRIVATE);
        final Map<String,?> all = rsp.getAll();

        // ソート
        final List<Entry<String,?>> entries = new ArrayList<Entry<String,?>>(all.entrySet());
        Collections.sort(entries, new Comparator<Entry<String,?>>(){
				public int compare(final Entry<String,?> e1, final Entry<String,?> e2) {
					return ((Long)e2.getValue()).compareTo((Long)e1.getValue());
				}
			});
        // 取り出し
        final ArrayList<String> result = new ArrayList<String>(30);
        for (Entry<String,?> entry : entries) {
            result.add(entry.getKey());
        }

        // 30個目以降は削除
        final int MAX = 30;
        final int size = result.size();
        if (size > MAX) {
            final Editor editor = rsp.edit();
            for (int i=size - 1 ; i >= MAX ; i--) {
                editor.remove(result.remove(i));
            }
            editor.apply();
        }
        return result;
    }
}
