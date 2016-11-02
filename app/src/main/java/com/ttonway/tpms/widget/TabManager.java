package com.ttonway.tpms.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;

import java.util.HashMap;

/**
 * This is a helper class that implements a generic mechanism for
 * associating fragments with the tabs in a tab host.  It relies on a
 * trick.  Normally a tab host has a simple API for supplying a View or
 * Intent that each tab will show.  This is not sufficient for switching
 * between fragments.  So instead we make the content part of the tab host
 * 0dp high (it is not shown) and the TabManager supplies its own dummy
 * view to show as the tab content.  It listens to changes in tabs, and takes
 * care of switch to the correct fragment shown in a separate content area
 * whenever the selected tab changes.
 */
public class TabManager implements TabHost.OnTabChangeListener {
    private final Activity mActivity;
    private final FragmentManager mFragmentManager;
    private final TabHost mTabHost;
    private final int mContainerId;
    private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
    TabInfo mLastTab;

    private int enterAnim = 0, exitAnim = 0;

    static final class TabInfo {
        private final String tag;
        private final Class<?> clss;
        private final Bundle args;
        private Fragment fragment;

        TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    public TabManager(Activity activity, FragmentManager fm, TabHost tabHost, int containerId) {
        mActivity = activity;
        mFragmentManager = fm;
        mTabHost = tabHost;
        mContainerId = containerId;
        mTabHost.setOnTabChangedListener(this);
    }

    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mActivity));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, clss, args);

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        info.fragment = mFragmentManager.findFragmentByTag(tag);
        if (info.fragment != null && !info.fragment.isDetached()) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.detach(info.fragment);
            ft.commit();//ft.commitAllowingStateLoss();
        }

        mTabs.put(tag, info);
        mTabHost.addTab(tabSpec);
    }

    @Override
    public void onTabChanged(String tabId) {
        TabInfo newTab = mTabs.get(tabId);
        if (mLastTab != newTab) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            //if (enterAnim != 0 && exitAnim != 0)
            ft.setCustomAnimations(enterAnim, exitAnim);

            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    ft.detach(mLastTab.fragment);
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(mActivity,
                            newTab.clss.getName(), newTab.args);
                    ft.add(mContainerId, newTab.fragment, newTab.tag);
                } else {
                    ft.attach(newTab.fragment);
                }
            }

            mLastTab = newTab;
            ft.commit();//ft.commitAllowingStateLoss();
            mFragmentManager.executePendingTransactions();
        }
    }

    public void setCustomAnimations(int enter, int exit) {
        this.enterAnim = enter;
        this.exitAnim = exit;
    }

    public Fragment findFragmentByTag(String tag) {
        TabInfo info = mTabs.get(tag);
        return info == null ? null : info.fragment;
    }
}

