package net.gnu.agrep;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import net.gnu.agrep.R;
import android.os.Parcelable;
import android.os.Parcel;
import java.io.Serializable;

public class GrepView extends ListView {

    static class Data implements Comparable<Data>, Comparator<Data>, Parcelable, Serializable {

        public File mFile ;
        public int mLinenumber ;
        public CharSequence mText;

        public Data() {
            this(null, 0, null);
        }

        public Data(File file, int linenumber, CharSequence text) {
            mFile = file;
            mLinenumber = linenumber;
            mText = text;
        }
		
		public Data(final Parcel im) {
			mFile = new File(im.readString());
			mText = im.readString();
			mLinenumber = im.readInt();
		}

		@Override
		public int describeContents() {
			return "Data".hashCode();
		}

		@Override
		public void writeToParcel(final Parcel p1, final int p2) {
			p1.writeString(mFile.getAbsolutePath());
			p1.writeString(mText+"");
			p1.writeInt(mLinenumber);
		}

		public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
			public Data createFromParcel(final Parcel in) {
				return new Data(in);
			}

			public Data[] newArray(final int size) {
				return new Data[size];
			}
		};
		
        @Override
        public int compare(final Data object1, final Data object2) {
            int ret = object1.mFile.getName().compareToIgnoreCase(object2.mFile.getName());
            if (ret == 0) {
                ret = object1.mLinenumber - object2.mLinenumber;
            }
            return ret;
        }

		@Override
		public int compareTo(final GrepView.Data object2) {
			int ret = mFile.getName().compareToIgnoreCase(object2.mFile.getName());
            if (ret == 0) {
                ret = mLinenumber - object2.mLinenumber;
            }
            return ret;
		}

	}

    interface Callback {
        void onGrepItemClicked(int position);
        boolean onGrepItemLongClicked(int position);
    }

    private Callback mCallback;

    private void init(Context context) {
        setSmoothScrollbarEnabled(true);
        setScrollingCacheEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setFastScrollEnabled(true);
        //setBackgroundColor(R.color.lightyellow);
        //setCacheColorHint(Color.WHITE);
        setDividerHeight(1);
        setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (mCallback != null) {
						mCallback.onGrepItemClicked(position);
					}
				}
			});
        setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					if (mCallback != null) {
						return mCallback.onGrepItemLongClicked(position);
					}
					return false;
				}
			});

    }

    public GrepView(Context context) {
        super(context);
        init(context);
    }

    public GrepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public GrepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        requestFocus();
        return super.onTouchEvent(ev);
    }


    static class GrepAdapter extends ArrayAdapter<Data> {

        private Pattern mPattern;
        private int mFgColor;
        private int mBgColor;
        private int mFontSize;

        static	class ViewHolder {
            TextView no;
            TextView path;
            TextView quote;
        }

        public GrepAdapter(final Context context, final int resource, final int textViewResourceId, final ArrayList<Data> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            final ViewHolder holder;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = inflate(getContext(), R.layout.list_row, null);

                holder = new ViewHolder();
				
                holder.no = (TextView)view.findViewById(R.id.no);
                holder.path = (TextView)view.findViewById(R.id.path);
                holder.quote = (TextView)view.findViewById(R.id.quote);

                holder.no.setTextColor(Color.BLACK);
				holder.path.setTextColor(Color.DKGRAY);
                holder.quote.setTextColor(Color.BLACK);
				
				holder.no.setTextSize(mFontSize);
                holder.path.setTextSize(mFontSize);
                holder.quote.setTextSize(mFontSize);

                view.setTag(holder);
            }
            final Data d = getItem(position);

            final String fname = d.mFile.getName() + "(" + d.mLinenumber + ")";
            holder.no.setText("" + (position + 1));
			holder.path.setText(fname);
            holder.quote.setText(SettingsFragment.highlightKeyword(d.mText, mPattern, mFgColor, mBgColor));

            return view;
        }

        public void setFormat(final Pattern pattern, final int fgcolor, final int bgcolor, final int size) {
            mPattern = pattern;
            mFgColor = fgcolor;
            mBgColor = bgcolor;
            mFontSize = size;

        }
    }
}
