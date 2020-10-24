package net.gnu.agrep;

import net.gnu.common.view.SlidingHorizontalScroll;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import net.gnu.agrep.R;

import android.content.res.*;
import android.os.*;
import java.io.*;

import android.content.Intent;
import android.util.Log;
import android.support.v4.app.FragmentTransaction;
import java.util.ArrayList;
import android.support.v4.app.*;
import net.gnu.agrep.PagerItem;
import android.app.Activity;

public class SlidingTabsFragment extends Fragment implements TabAction {

	private static final String TAG = "SlidingTabsFragment";

    private SlidingHorizontalScroll mSlidingHorizontalScroll;

    private ViewPager mViewPager;
	private FragmentManager childFragmentManager;
	private PagerAdapter pagerAdapter;
	private int pageSelected = 1;
	private boolean newIntent = false;
	private SettingsFragment newFrag = null;
	private PagerItem pagerItem = null;
    private List<PagerItem> mTabs = new ArrayList<PagerItem>();
	private AGrepActivity aGrepActivity;
    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView " + savedInstanceState);
		return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of
     * {@link SampleFragmentPagerAdapter}. The {@link SlidingTabLayout} is then given the
     * {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        Log.d(TAG, "onViewCreated savedInstanceState=" + savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        childFragmentManager = getChildFragmentManager();
		aGrepActivity = (AGrepActivity) getActivity();
		
		Log.d(TAG, "onViewCreated mTabs=" + mTabs);
		if (savedInstanceState == null) {
			pagerAdapter = new PagerAdapter(childFragmentManager);
			mViewPager.setAdapter(pagerAdapter);
			Log.d(TAG, "mViewPager " + mViewPager);
		} else {
			mTabs.clear();
			
			String tag;
			PagerItem pagerItem;
			SettingsFragment frag;
			String title;
			final List<Fragment> fragments = childFragmentManager.getFragments();
			final int size = fragments.size();
			for (int i = 0; i < size; i++) {
				tag = savedInstanceState.getString(i + "");
				frag = (SettingsFragment) childFragmentManager.findFragmentByTag(tag);
				if (frag != null) {
					title = savedInstanceState.getString(tag);
					pagerItem = new PagerItem(frag, title);
					frag.slidingTabsFragment = this;
					Log.d(TAG, "onViewCreated frag " + i + ", " + tag + ", " + frag.getTag() + ", " + pagerItem.title + ", " + frag);
					mTabs.add(pagerItem);
				}
			}
			
			final String firstTag = savedInstanceState.getString("fake0");
			final String lastTag = savedInstanceState.getString("fakeEnd");
			if (firstTag != null) {
				final PagerItem get0 = mTabs.get(0);
				get0.fakeFrag = (SettingsFragment) childFragmentManager.findFragmentByTag(firstTag);
				get0.fakeFrag.slidingTabsFragment = this;
				final PagerItem last = mTabs.get((mTabs.size() - 1));
				last.fakeFrag = (SettingsFragment) childFragmentManager.findFragmentByTag(lastTag);
				last.fakeFrag.slidingTabsFragment = this;
			}
			// Get the ViewPager and set it's PagerAdapter so that it can
			// display items
			pagerAdapter = new PagerAdapter(childFragmentManager);
			mViewPager.setAdapter(pagerAdapter);
			int pos1 = savedInstanceState.getInt("pos", 0);
			mViewPager.setCurrentItem(pos1);
		}
		mViewPager.setOffscreenPageLimit(8);
		
		// BEGIN_INCLUDE (setup_slidingtablayout)
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the
		// ViewPager has had
		// it's PagerAdapter set.
		mSlidingHorizontalScroll = (SlidingHorizontalScroll) view
			.findViewById(R.id.sliding_tabs);
		mSlidingHorizontalScroll.setTabAction(this);

		mSlidingHorizontalScroll.setViewPager(mViewPager);
		mSlidingHorizontalScroll.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(final int pageSelected, final float positionOffset,
										   final int positionOffsetPixel) {
//					Log.e("onPageScrolled", "pageSelected: " + pageSelected
//						+ ", positionOffset: " + positionOffset
//						+ ", positionOffsetPixel: " + positionOffsetPixel);
					if (positionOffset == 0 && positionOffsetPixel == 0) {
						final int size = mTabs.size();
						if (size > 1) {
							if (pageSelected == 0) {
								mViewPager.setCurrentItem(size, false);
							} else if (pageSelected == size + 1) {
								mViewPager.setCurrentItem(1, false);
							}
						}
					} 
				}

				@Override
				public void onPageSelected(final int position) {
					final int size = mTabs.size();
					Log.d(TAG, "onPageSelected: " + position + ", mTabs.size() " + size);
					pageSelected = position;
					aGrepActivity.main = getItem(position);
					if (size > 1) {
						if (position == 1 || position == size) {
							final int newpos = position == 1 ? (size - 1) : position == size ? 0 : (position - 1);
							final PagerItem pi = mTabs.get(newpos);
							Log.d(TAG, "onPageSelected: " + position + ", pi.frag " + pi.frag + ", pi.fakeFrag " + pi.fakeFrag);
							if (pi.fakeFrag != null) {
								pi.fakeFrag.clone(pi.frag);
							} else {
								pi.createFakeFragment();
							}
						}
					}
				}

				@Override
				public void onPageScrollStateChanged(final int state) {
					Log.d(TAG, "onPageScrollStateChanged1 state " + state + ", pageSelected " + pageSelected);
					if (state == 0) {
						final int size = mTabs.size();
						if (pageSelected == 0) {
							pageSelected = size;
							mViewPager.setCurrentItem(pageSelected, false);
						} else if (pageSelected == size + 1) {
							pageSelected = 1;
							mViewPager.setCurrentItem(pageSelected, false);
						}

					}
					Log.d(TAG, "onPageScrollStateChanged2 state " + state + ", pageSelected " + pageSelected);
				}
			});
		
		// BEGIN_INCLUDE (tab_colorizer)
		// Set a TabColorizer to customize the indicator and divider colors.
		// Here we just retrieve
		// the tab at the position, and return it's set color
		mSlidingHorizontalScroll
			.setCustomTabColorizer(new SlidingHorizontalScroll.TabColorizer() {
				@Override
				public int getIndicatorColor(int position) {
					return 0xff00ff00;
				}

				@Override
				public int getDividerColor(int position) {
					return 0xffdddddd;
				}
			});
		// END_INCLUDE (tab_colorizer)
		// END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)

	@Override
	public void onResume() {
		Log.d(TAG, "onResume pagerAdapter=" + pagerAdapter + ", mTabs=" + mTabs + ", newIntent " + newIntent);
		super.onResume();
		aGrepActivity = (AGrepActivity) getActivity();
		if (newIntent) {
			newIntent = false;
			addFrag(newFrag, pagerItem);
			newFrag = null;
			pagerItem = null;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState " + outState);
		super.onSaveInstanceState(outState);
		try {
			final int size = mTabs.size();
			if (mTabs != null && size > 0) {
				int i = 0;
				for (PagerItem pi : mTabs) {
					Log.d(TAG, "onSaveInstanceState pi " + pi);
					//childFragmentManager.putFragment(outState, "tabb" + i++, pi.frag);
					outState.putString(i++ + "", pi.frag.getTag());
					outState.putString(pi.frag.getTag(), pi.title);
				}
				if (size > 1) {
					//Log.d(TAG, "fakeStart 0 tag" + mTabs.get(0).fakeFrag.getTag());
					outState.putString("fake0", mTabs.get(0).fakeFrag.getTag());
					//Log.d(TAG, "fakeEnd tag  " + mTabs.get(mTabs.size()-1).fakeFrag.getTag());
		 			outState.putString("fakeEnd", mTabs.get(size - 1).fakeFrag.getTag());
				}
				outState.putInt("pos", mViewPager.getCurrentItem());
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void addFrag(final SettingsFragment frag, final PagerItem pagerItem) {
		final FragmentTransaction ft = childFragmentManager.beginTransaction();
		final ArrayList<PagerItem> mTabs2 = new ArrayList<PagerItem>(mTabs);
		final int size = mTabs.size();
		int currentItem = 0;
		if (size > 1) {
			currentItem = mViewPager.getCurrentItem();
			Log.d(TAG, "addFrag1 currentItem " + currentItem + ", dir=" + frag.getTitle() + ", mTabs=" + mTabs);

			PagerItem pi = mTabs.get(0);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;

			pi = mTabs.get(size - 1);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;

			for (int j = 0; j < size; j++) {
				ft.remove(mTabs.remove(0).frag);
			}
			pagerAdapter.notifyDataSetChanged();
			ft.commitNow();

			for (PagerItem pi2 : mTabs2) {
				mTabs.add(pi2);
			}
			mTabs.add(currentItem++, pagerItem);
			mViewPager.setAdapter(pagerAdapter);
			mViewPager.setCurrentItem(currentItem, false);
		} else {
			final PagerItem remove = mTabs.remove(0);
			ft.remove(remove.frag);
			pagerAdapter.notifyDataSetChanged();
			ft.commitNow();
			mTabs.add(remove);
			mTabs.add(pagerItem);
			pagerAdapter.notifyDataSetChanged();
			currentItem = 2;
			mViewPager.setCurrentItem(currentItem);
		}
		notifyTitleChange();

		Log.d(TAG, "addFrag2 " + ", CurrentItem " + mViewPager.getCurrentItem() + ", " + mTabs);
	}
	
	public void addInit(List<SettingsFragment> l) {
		for (SettingsFragment newFrag : l) {
			newFrag.slidingTabsFragment = this;
			pagerItem = new PagerItem(newFrag, newFrag.getTitle());
			mTabs.add(pagerItem);
		}
		pagerAdapter.notifyDataSetChanged();
		notifyTitleChange();
	}

	public void addTab(final Intent intent) {
		Log.d(TAG, "addTab1 pagerAdapter=" + pagerAdapter + ", intent=" + intent + ", mTabs=" + mTabs);
		newFrag = SettingsFragment.newInstance((AGrepActivity)getActivity());
		newFrag.slidingTabsFragment = this;
		pagerItem = new PagerItem(newFrag, newFrag.getTitle());

		if (mViewPager != null) {
			if (mTabs.size() == 0) {
				mTabs.add(pagerItem);
				pagerAdapter.notifyDataSetChanged();
				mViewPager.setCurrentItem(pagerAdapter.getCount() - 1);
				notifyTitleChange();
			} else if (intent == null) {
				addFrag(newFrag, pagerItem);
			} else {
				newIntent = true;
			}
		} else {
			mTabs.add(pagerItem);
		}
		Log.d(TAG, "addTab2 " + mTabs);
	}

	public void closeTab(SettingsFragment m) {
		int i = 0;
		final ArrayList<PagerItem> mTabs2 = new ArrayList<PagerItem>(mTabs);
		for (PagerItem pi : mTabs) {
			if (pi.frag == m) {
				break;
			}
			i++;
		}
		Log.i(TAG, "closeTab " + i + ", " + m + ", " + mTabs);
		final FragmentTransaction ft = childFragmentManager.beginTransaction();
		PagerItem pi;
		if (mTabs.size() > 1) {
			pi = mTabs.get(0);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;
			pi = mTabs.get(mTabs.size() - 1);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;
		}
		for (int j = mTabs2.size() - 1; j >= i; j--) {
			ft.remove(mTabs.remove(j).frag);
		}
		if (mTabs.size() == 1 && mTabs2.size() == 2) {
			pi = mTabs.remove(0);
			ft.remove(pi.frag);
			pi.fakeFrag = null;
		}
		//mTabs.clear();
		pagerAdapter.notifyDataSetChanged();
		ft.commitNow();

		mTabs2.remove(i);

		if (mTabs.size() == 0 && i > 0) {
			mTabs.add(mTabs2.get(0));
		}
		for (int j = i; j < mTabs2.size(); j++) {
			mTabs.add(mTabs2.get(j));
		}
		pagerAdapter.notifyDataSetChanged();
		mTabs2.clear();
		notifyTitleChange();
		final int currentItem = i <= mTabs.size() - 1 && mTabs.size() > 1 ? i + 1: mTabs.size() == 1 ? 0 : i;
		mViewPager.setCurrentItem(currentItem);
	}

	public void closeCurTab() {
		final SettingsFragment main = getCurFrag();
		final AGrepActivity activity = (AGrepActivity)getActivity();
		activity.saved = 0;
		activity.tabCount = mTabs.size();
		Log.d(TAG, "closeCurTab " + main);
		if (activity.tabCount == 1) {
			activity.finish();
		} else {
			closeTab(main);
		}
	}

	public void closeOtherTabs() {
		final SettingsFragment cur = getCurFrag();
		final AGrepActivity activity = (AGrepActivity) getActivity();
		activity.saved = 0;
		final int size = mTabs.size();
		activity.tabCount = size;
		Log.d(TAG, "closeOtherTabs " + cur);
		for (int i = size - 1; i >= 0; i--) {
			final SettingsFragment m = mTabs.get(i).frag; //pagerAdapter.getItem(i);
			if (m != cur) {
				closeTab(m);
				Log.d(TAG, "closeOtherTabs2 " + m);
			}
		}
	}
	
	public int getSize() {
		return mTabs.size();
	}

	public void updateTitle(SettingsFragment m, String name) {
		Log.d(TAG, "updateTitle " + name + ", " + mTabs);
		for (PagerItem pi : mTabs) {
			if (pi.frag == m) {
				pi.title = name;
			}
		}
		notifyTitleChange();
	}

    public void notifyTitleChange() {
		mSlidingHorizontalScroll.setViewPager(mViewPager);
	}

	public SettingsFragment getCurFrag() {
		final int currentItem = mViewPager.getCurrentItem();
		Log.d(TAG, "getCurFrag()=" + currentItem + ", " + this);
		return pagerAdapter.getItem(currentItem);
	}

	public int getCurIndex() {
		return mViewPager.getCurrentItem();
	}

	public int getIndex(final SettingsFragment frag) {
		int i = 0;
		for (PagerItem sf : mTabs) {
			if (frag == sf.frag) {
				return i;
			}
			i++;
		}
		return 1;
	}
	
	public int getCount() {
		return pagerAdapter.getCount();
	}
	
	public SettingsFragment getItem(int i) {
		return pagerAdapter.getItem(i);
	}
	
	public void setCurrentItem(int i) {
		mViewPager.setCurrentItem(i);
	}

    public class PagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "PagerAdapter";

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

		@Override
		public SettingsFragment getItem(final int position) {
			final int size = mTabs.size();
			Log.d(TAG, "getItem " + position + "/" + size);
			if (size > 1) {
				if (position == 0) {
					SettingsFragment createFakeFragment = mTabs.get(size - 1).createFakeFragment();
					createFakeFragment.slidingTabsFragment = SlidingTabsFragment.this;
					return createFakeFragment;
				} else if (position == size + 1) {
					SettingsFragment createFakeFragment = mTabs.get(0).createFakeFragment();
					createFakeFragment.slidingTabsFragment = SlidingTabsFragment.this;
					return createFakeFragment;
				} else {
					return mTabs.get(position - 1).frag;
				}
			} else {
				return mTabs.get(0).frag;
			}
		}

		@Override
		public int getCount() {
			final int size = mTabs.size();
			if (size > 1) {
				return size + 2;
			} else {
				return size;
			}
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			final int size = mTabs.size();
			if (size > 1) {
				if (position == 0 || position == size + 1) {
					return "";
				} else {
					return mTabs.get(position - 1).getTitle();
				}
			} else {
				return mTabs.get(position).getTitle();
			}
		}

		@Override
		public int getItemPosition(final Object object) {
			for (PagerItem pi : mTabs) {
				if (pi.frag == object) {
					Log.d(TAG, "getItemPosition POSITION_UNCHANGED" + ", " + object);
					return POSITION_UNCHANGED;
				}
			}
			Log.d(TAG, "getItemPosition POSITION_NONE" + ", " + object);
			return POSITION_NONE;
		}
    }
}

class PagerItem implements Parcelable, Serializable {

	private static final String TAG = "PagerItem";

	SettingsFragment frag;
	SettingsFragment fakeFrag;
	String title;

	public PagerItem(final SettingsFragment frag1, String title) {
		//Log.d(TAG, "tag=" + frag1.getTag() + ", " + frag1);
		this.frag = frag1;
		this.title = frag1.getTitle();
		if (title != null) {
			this.title = title;
		}
	}

	public PagerItem(Parcel in) {
		final PagerItem pi = (PagerItem) in.readSerializable();
		frag = pi.frag;
		fakeFrag = pi.fakeFrag;
		title = pi.title;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(this);
	}

	public static final Parcelable.Creator<PagerItem> CREATOR = new Parcelable.Creator<PagerItem>() {
		public PagerItem createFromParcel(Parcel in) {
			return new PagerItem(in);
		}

		public PagerItem[] newArray(int size) {
			return new PagerItem[size];
		}
	};

	public SettingsFragment createFakeFragment() {
		Log.d(TAG, "createFakeFragment() fakeFrag " + fakeFrag + ", frag " + frag);

		if (fakeFrag == null) {
			fakeFrag = frag.clone();
		} else if (fakeFrag != null && frag != null) {
			fakeFrag.clone(frag);
		}
		return fakeFrag;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "frag=" + frag + ", fakeFrag=" + fakeFrag;
	}
}
