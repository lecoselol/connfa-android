package com.ls.drupalcon.model;

import android.os.AsyncTask;
import com.ls.drupal.DrupalClient;
import com.ls.drupalcon.model.data.UpdateDate;
import com.ls.drupalcon.model.database.ILAPIDBFacade;
import com.ls.drupalcon.model.managers.SynchronousItemManager;
import com.ls.http.base.BaseRequest;
import com.ls.http.base.RequestConfig;
import com.ls.http.base.ResponseData;
import com.ls.ui.drawer.DrawerManager;
import com.ls.util.ObserverHolder;
import com.ls.utils.ApplicationConfig;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpdatesManager {

    public static final int SETTINGS_REQUEST_ID = 0;
    public static final int TYPES_REQUEST_ID = 1;
    public static final int LEVELS_REQUEST_ID = 2;
    public static final int TRACKS_REQUEST_ID = 3;
    public static final int SPEAKERS_REQUEST_ID = 4;
    public static final int LOCATIONS_REQUEST_ID = 5;
    public static final int HOUSE_PLANS_REQUEST_ID = 6;
    public static final int PROGRAMS_REQUEST_ID = 7;
    public static final int BOFS_REQUEST_ID = 8;
    public static final int SOCIALS_REQUEST_ID = 9;
    public static final int POIS_REQUEST_ID = 10;
    public static final int INFO_REQUEST_ID = 11;
    public static final int TWITTER_REQUEST_ID = 12;
    private static final long MINIMUM_STALE_DATA_AGE_MILLIS = TimeUnit.MINUTES.toMillis(15);

    private final DrupalClient mClient;
    private final ObserverHolder<DataUpdatedListener> mUpdateListeners;

    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";

    public static int convertEventIdToEventModePos(int eventModePos) {
        switch (eventModePos) {
            case PROGRAMS_REQUEST_ID:
                return DrawerManager.EventMode.Program.ordinal();
            case BOFS_REQUEST_ID:
                return DrawerManager.EventMode.Bofs.ordinal();
            case SOCIALS_REQUEST_ID:
                return DrawerManager.EventMode.Social.ordinal();
        }
        return 0;
    }

    public UpdatesManager(@NotNull DrupalClient client) {
        mUpdateListeners = new ObserverHolder<>();
        mClient = client;
    }

    public void startLoading(final UpdateCallback callback) {
        new AsyncTask<Void, Void, List<Integer>>() {

            @Override
            protected List<Integer> doInBackground(Void... params) {
                return doPerformLoading();
            }

            @Override
            protected void onPostExecute(final List<Integer> result) {
                if (result != null) {
                    mUpdateListeners.notifyAllObservers(new ObserverHolder.ObserverNotifier<DataUpdatedListener>() {
                        @Override
                        public void onNotify(DataUpdatedListener observer) {
                            observer.onDataUpdated(result);
                        }
                    });
                }

                if (result != null) {
                    if (callback != null) {
                        callback.onDownloadSuccess();
                    }
                    mUpdateListeners.notifyAllObservers(new ObserverHolder.ObserverNotifier<DataUpdatedListener>() {
                        @Override
                        public void onNotify(DataUpdatedListener observer) {
                            observer.onDataUpdated(result);
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onDownloadError();
                    }
                }
            }
        }.execute();
    }

    public void registerUpdateListener(DataUpdatedListener listener) {
        mUpdateListeners.registerObserver(listener);
    }

    public void unregisterUpdateListener(DataUpdatedListener listener) {
        mUpdateListeners.unregisterObserver(listener);
    }

    /**
     * @return return updated request id's list in case of success or null in case of failure
     */

    private List<Integer> doPerformLoading() {
        if (dataIsFresh()) {
            return null;    // No need to update
        }

        RequestConfig config = new RequestConfig();
        config.setResponseFormat(BaseRequest.ResponseFormat.JSON);
        config.setRequestFormat(BaseRequest.RequestFormat.JSON);
        config.setResponseClassSpecifier(UpdateDate.class);
        BaseRequest checkForUpdatesRequest =
            new BaseRequest(BaseRequest.RequestMethod.GET,
                            ApplicationConfig.BASE_URL + "checkUpdates.json",
                            config);

        ResponseData updatesData = mClient.performRequest(checkForUpdatesRequest, true);

        int statusCode = updatesData.getStatusCode();
        if (statusCode > 0 && statusCode < 400) {
            UpdateDate updateDate = (UpdateDate) updatesData.getData();
            if (updateDate == null) {
                return new LinkedList<>();
            }
            updateDate.setTime(updatesData.getHeaders().get(LAST_MODIFIED_HEADER));
            return loadData(updateDate);
        } else {
            return null;
        }
    }

    private boolean dataIsFresh() {
        String lastDate = PreferencesManager.getInstance().getLastUpdateDate();
        long lastDateMillis = Long.getLong(lastDate, 0);
        return System.currentTimeMillis() - lastDateMillis < MINIMUM_STALE_DATA_AGE_MILLIS;
    }

    private List<Integer> loadData(UpdateDate updateDate) {
        List<Integer> updateIds = updateDate.getIdsForUpdate();
        if (updateIds == null || updateIds.isEmpty()) {
            return new LinkedList<>();
        }

        ILAPIDBFacade facade = Model.instance().getFacade();
        try {
            facade.open();
            facade.beginTransactions();
            boolean success = true;
            for (Integer i : updateIds) {
                success = sendRequestById(i);
                if (!success) {
                    break;
                }
            }
            if (success) {
                facade.setTransactionSuccesfull();
                PreferencesManager.getInstance().saveLastUpdateDate(String.valueOf(System.currentTimeMillis()));
            }
            return success ? updateIds : null;
        }
        finally {
            facade.endTransactions();
            facade.close();
        }
    }

    private boolean sendRequestById(int id) {

        SynchronousItemManager manager;
        switch (id) {
            case SETTINGS_REQUEST_ID:
                manager = Model.instance().getSettingsManager();
                break;

            case TYPES_REQUEST_ID:
                manager = Model.instance().getTypesManager();
                break;

            case LEVELS_REQUEST_ID:
                manager = Model.instance().getLevelsManager();
                break;

            case TRACKS_REQUEST_ID:
                manager = Model.instance().getTracksManager();
                break;

            case SPEAKERS_REQUEST_ID:
                manager = Model.instance().getSpeakerManager();
                break;

            case LOCATIONS_REQUEST_ID:
                manager = Model.instance().getLocationManager();
                break;

            case PROGRAMS_REQUEST_ID:
                manager = Model.instance().getProgramManager();
                break;

            case BOFS_REQUEST_ID:
                manager = Model.instance().getBofsManager();
                break;

            case SOCIALS_REQUEST_ID:
                manager = Model.instance().getSocialManager();
                break;

            case POIS_REQUEST_ID:
                manager = Model.instance().getPoisManager();
                break;

            case INFO_REQUEST_ID:
                manager = Model.instance().getInfoManager();
                break;
            default:
                return true;
        }

        if (manager != null) {
            return manager.fetchData();
        }

        return false;
    }

    public interface DataUpdatedListener {

        void onDataUpdated(List<Integer> requestIds);
    }

    public void checkForDatabaseUpdate() {
        ILAPIDBFacade facade = Model.instance().getFacade();
        facade.open();
        facade.close();
    }
}
