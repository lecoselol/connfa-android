package com.ls.drupalcon.app;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;
import com.ls.drupal.DrupalClient;
import com.ls.drupalcon.BuildConfig;
import com.ls.drupalcon.model.AppDatabaseInfo;
import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.PreferencesManager;
import com.ls.drupalcon.model.data.Event;
import com.ls.drupalcon.model.data.Speaker;
import com.ls.drupalcon.model.database.LAPIDBRegister;
import com.ls.http.base.BaseRequest;
import com.ls.ui.view.FontHelper;
import com.ls.util.image.DrupalImageView;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    private static Context mContext;
    private static final String PROPERTY_ID = "UA-267362-65";
    private static App instance;
    private Map<Long, List<Long>> speakersMap;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        mContext = getApplicationContext();
        instance = this;
        speakersMap = new HashMap<>();

        LAPIDBRegister.getInstance().register(mContext, new AppDatabaseInfo(mContext));
        PreferencesManager.initializeInstance(mContext);
        Model.instance(mContext);
        FontHelper.init(mContext);
        DrupalImageView.setupSharedClient(new DrupalClient(null, Model.instance().createNewQueue(getApplicationContext()), BaseRequest.RequestFormat.JSON, null));
    }

    public static Context getContext() {
        return mContext;
    }

    public synchronized Tracker getTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        return analytics.newTracker(PROPERTY_ID);
    }

    public static App getInstance() {
        return instance;
    }

    public void saveSpeakersFrom(Event event) {
        if (!speakersMap.containsKey(event.getId())) {
            speakersMap.put(event.getId(), event.getSpeakers());
        }
    }

    // TODO SpeakerManager could use a sorting so that we just extract the speakers from the list by using their IDs instead of cycling every time
    public List<Speaker> getSpeakersWithinSession(Long id) {
        List<Long> speakersIds = speakersMap.get(id);
        List<Speaker> speakers = new ArrayList<>(speakersIds.size());
        final List<Speaker> allTheSpeakers = Model.instance().getSpeakerManager().getSpeakers();

        for (Long speakerId : speakersIds) {
            for (Speaker speaker : allTheSpeakers) {
                if (speaker.getId().equals(speakerId)) {
                    speakers.add(speaker);
                }
            }
        }

        return speakers;
    }
}
