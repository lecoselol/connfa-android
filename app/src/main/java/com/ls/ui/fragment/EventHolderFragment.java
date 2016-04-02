package com.ls.ui.fragment;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.astuetz.PagerSlidingTabStrip;
import com.ls.drupalcon.R;
import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.UpdatesManager;
import com.ls.drupalcon.model.managers.BofsManager;
import com.ls.drupalcon.model.managers.FavoriteManager;
import com.ls.drupalcon.model.managers.ProgramManager;
import com.ls.drupalcon.model.managers.SocialManager;
import com.ls.ui.adapter.BaseEventDaysPagerAdapter;
import com.ls.ui.receiver.ReceiverManager;
import com.ls.utils.DateUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.ls.ui.drawer.DrawerManager.*;

public class EventHolderFragment extends Fragment {

    public static final String TAG = "ProjectsFragment";
    private static final String EXTRAS_ARG_MODE = "EXTRAS_ARG_MODE";

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerTabs;
    private BaseEventDaysPagerAdapter mAdapter;

    private EventMode mEventMode;
    private View mTxtNoEvents;
    private View mNoFavorites;

    private UpdatesManager.DataUpdatedListener updateReceiver = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<Integer> requestIds) {
            updateData(requestIds);
        }
    };

    private ReceiverManager favoriteReceiver = new ReceiverManager(new ReceiverManager.FavoriteUpdatedListener() {
        @Override
        public void onFavoriteUpdated(long eventId, boolean isFavorite) {
            updateFavorites();
        }
    });

    public static EventHolderFragment newInstance(int modePos) {
        EventHolderFragment fragment = new EventHolderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRAS_ARG_MODE, modePos);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_holder_event, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Model.instance().getUpdatesManager().registerUpdateListener(updateReceiver);
        favoriteReceiver.register(getActivity());

        initData();
        initView();
        new LoadData(this, mEventMode).execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateReceiver);
        favoriteReceiver.unregister(getActivity());
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            int eventPos = bundle.getInt(EXTRAS_ARG_MODE, EventMode.Program.ordinal());
            mEventMode = EventMode.values()[eventPos];
        }
    }

    private void initView() {
        View view = getView();
        if (view == null) {
            return;
        }

        mAdapter = new BaseEventDaysPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mAdapter);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        mPagerTabs = (PagerSlidingTabStrip) getView().findViewById(R.id.pager_tab_strip);
        mPagerTabs.setTypeface(typeface, 0);
        mPagerTabs.setViewPager(mViewPager);

        mTxtNoEvents = view.findViewById(R.id.txtNoEvents);
        mNoFavorites = view.findViewById(R.id.emptyIcon);

        if (mEventMode != EventMode.Program) {
            setHasOptionsMenu(false);
        } else {
            setHasOptionsMenu(true);
        }
    }

    static class LoadData extends AsyncTask<Void, Void, List<Long>> {

        private final WeakReference<EventHolderFragment> mHostFragmentRef;
        private final EventMode mEventMode;

        LoadData(EventHolderFragment hostFragment, EventMode eventMode) {
            mEventMode = eventMode;
            mHostFragmentRef = new WeakReference<>(hostFragment);
        }

        @Override
        protected List<Long> doInBackground(Void... params) {
            return getDayList();
        }

        @Override
        protected void onPostExecute(List<Long> result) {
            EventHolderFragment fragment = mHostFragmentRef.get();
            if (fragment == null) {
                return;
            }
            fragment.updateViews(result);
        }

        private List<Long> getDayList() {
            List<Long> dayList = new ArrayList<>();
            switch (mEventMode) {
                case Bofs:
                    BofsManager bofsManager = Model.instance().getBofsManager();
                    dayList.addAll(bofsManager.getBofsDays());
                    break;
                case Social:
                    SocialManager socialManager = Model.instance().getSocialManager();
                    dayList.addAll(socialManager.getSocialsDays());
                    break;
                case Favorites:
                    FavoriteManager favoriteManager = Model.instance().getFavoriteManager();
                    dayList.addAll(favoriteManager.getFavoriteEventDays());
                    break;
                default:
                    ProgramManager programManager = Model.instance().getProgramManager();
                    dayList.addAll(programManager.getProgramDays());
                    break;
            }
            return dayList;
        }

    }

    private void updateViews(List<Long> dayList) {
        if (dayList.isEmpty()) {
            mPagerTabs.setVisibility(View.GONE);
            if (mEventMode == EventMode.Favorites) {
                mNoFavorites.setVisibility(View.VISIBLE);
            } else {
                mTxtNoEvents.setVisibility(View.VISIBLE);
            }
        } else {
            mNoFavorites.setVisibility(View.GONE);
            mTxtNoEvents.setVisibility(View.GONE);
            mPagerTabs.setVisibility(View.VISIBLE);
        }

        mAdapter.setData(dayList, mEventMode);
        switchToCurrentDay(dayList);
    }

    private void switchToCurrentDay(List<Long> days) {
        int item = 0;
        for (Long millis : days) {
            if (DateUtils.getInstance().isToday(millis)) {
                mViewPager.setCurrentItem(item);
                break;
            }
            item++;
        }
    }

    private void updateData(List<Integer> requestIds) {
        for (int id : requestIds) {
            int eventModePos = UpdatesManager.convertEventIdToEventModePos(id);
            if (eventModePos == mEventMode.ordinal()) {
                new LoadData(this, mEventMode).execute();
                break;
            }
        }
    }

    private void updateFavorites() {
        if (getView() != null) {
            if (mEventMode == EventMode.Favorites) {
                new LoadData(this, mEventMode).execute();
            }
        }
    }
}
