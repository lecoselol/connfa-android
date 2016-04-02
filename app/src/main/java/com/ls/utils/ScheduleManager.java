package com.ls.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ls.drupalcon.model.data.EventDetailsEvent;
import com.ls.drupalcon.model.data.Speaker;
import com.ls.receiver.NotifyReceiver;

import java.util.Calendar;
import java.util.List;

public class ScheduleManager {
    private Context mContext;
    private final AlarmManager am;

    public ScheduleManager(Context context) {
        mContext = context;
        this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setAlarmForNotification(Calendar calendar, EventDetailsEvent event, List<Speaker> speakerList, long day) {
        new AlarmTask(mContext, calendar, am, event, speakerList, day).run();
    }

    public void cancelAlarm(long id) {
        Intent intent = new Intent(mContext, NotifyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pendingIntent);
    }

}
