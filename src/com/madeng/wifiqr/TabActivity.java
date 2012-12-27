package com.madeng.wifiqr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.actionbarsherlock.widget.ShareActionProvider.OnShareTargetSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Displays "Generate" and "Saved" tabs
 * 
 * @author Amandeep Grewal
 * 
 */
public class TabActivity extends SherlockFragmentActivity {

	private static final String TAG = "TabActivity";

	private ViewPager mViewPager;
	public TabsAdapter mTabsAdapter;

	@SuppressWarnings("rawtypes")
	/**
	 * Stores fragments attached to window
	 */
	public final HashMap<Class, Fragment> mFrags = new HashMap<Class, Fragment>(2);

	@Override
	public void onAttachFragment(Fragment f) {
		// Listens for attached fragments and stores reference to them, using
		// their class as identifying key
		mFrags.put(f.getClass(), f);
		Log.d(TAG, "frag = " + f);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.actionbar_tabs_pager);

    if (findViewById(R.id.pager) != null) {
      // Use tabs for navigation
      getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

      // Add tabs
      ActionBar.Tab tab1 = getSupportActionBar().newTab().setText(R.string.generate);
      ActionBar.Tab tab2 = getSupportActionBar().newTab().setText(R.string.saved);

      // Ties swiping action (View Pager) with tabs display (adapter)
      mViewPager = (ViewPager) findViewById(R.id.pager);
      mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
      mTabsAdapter.addTab(tab1, GenQRFragment.class);
      mTabsAdapter.addTab(tab2, SavedFragment.class);

      // Restores current tab
      if (savedInstanceState != null) {
        getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("index"));
      }
    } else {

    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Saves current tab
		outState.putInt("index", getSupportActionBar().getSelectedNavigationIndex());
		
		Log.d(TAG, "onSaveInstanceState");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		// If before Honeycomb, just display icon, instead of text as well
		// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
		// menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// }

		ShareActionProvider shareProv = (ShareActionProvider) menu.findItem(R.id.share_qr_code).getActionProvider();
		shareProv.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		shareProv.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {

			@Override
			public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
				Log.d(TAG, "onShareTargetSelected");
				((GenQRFragment) mFrags.get(GenQRFragment.class)).onShare(intent);
				return true;
			}
		});

		Intent share = new Intent(Intent.ACTION_SEND);
		share.putExtra(Intent.EXTRA_STREAM, (Parcelable) null).setType("image/jpeg");
		shareProv.setShareIntent(share);

		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTabsAdapter.clear();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * Finds active GenQRFragment fragment
	 * 
	 * @param f
	 *            finds required fragment in this fragment's activity
	 * @return active GenQRFragment fragment
	 */
	public static GenQRFragment getQRFragment(Fragment f) {
		return (GenQRFragment) getTabActivity(f).mFrags.get(GenQRFragment.class);
	}

	/**
	 * Finds active SavedFragment fragment
	 * 
	 * @param f
	 *            finds required fragment in this fragment's activity
	 * @return active SavedFragment fragment
	 */
	public static SavedFragment getSavedFragment(Fragment f) {
		return (SavedFragment) getTabActivity(f).mFrags.get(SavedFragment.class);
	}

	/**
	 * Finds active TabActivity
	 * 
	 * @param f
	 *            finds required activity at this fragment's activity
	 * @return active TabActivity
	 */
	public static TabActivity getTabActivity(Fragment f) {
		return (TabActivity) f.getActivity();
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.TabListener {
		private final Context mContext;
		public final ActionBar mActionBar;
		public final ViewPager mViewPager;
		private final ArrayList<String> mTabs = new ArrayList<String>();
		private final HashMap<Integer, Fragment> mFrags = new HashMap<Integer, Fragment>(2);

		public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = actionBar;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void clear() {
			mFrags.clear();
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss) {
			mTabs.add(clss.getName());
			mActionBar.addTab(tab.setTabListener(this));
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			// Looks for fragment in map, otherwise create it and put it into
			// map
			Fragment frag = mFrags.get(position);
			if (frag != null)
				return frag;
			frag = Fragment.instantiate(mContext, mTabs.get(position), null);
			mFrags.put(position, frag);
			return frag;
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

}
