package com.ls.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ls.drupalcon.model.data.EventDetailsEvent;
import com.ls.drupalcon.model.data.Speaker;
import com.ls.receiver.NotifyReceiver;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class AlarmTask implements Runnable {

    public static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_DAY = "EXTRA_DAY";
    public static final String EXTRA_TEXT = "EXTRA_TEXT";
    public static final String EXTRA_ROOM = "EXTRA_ROOM";
    public static final String EXTRA_SPEAKERS = "EXTRA_SPEAKERS";
    private static final int FIVE_MINUTES = 5 * 60 * 1000;
    private final static String EMPTY_STRING = "";

    private final Calendar date;
    private final AlarmManager am;
    private final Context context;
    private final EventDetailsEvent event;
    private final List<Speaker> speakerList;
    private final long day;

    public AlarmTask(Context context, Calendar date, AlarmManager am, EventDetailsEvent event, List<Speaker> speakerList, long day) {
        this.context = context;
        this.am = am;
        this.date = date;
        this.event = event;
        this.speakerList = speakerList;
        this.day = day;
    }

    @Override
    public void run() {
        Intent intent = new Intent(context, NotifyReceiver.class);
        intent.putExtra(EXTRA_ID, event.getEventId());
        intent.putExtra(EXTRA_DAY, day);
        intent.putExtra(EXTRA_ROOM, event.getPlace());
        intent.putExtra(EXTRA_TEXT, event.getEventName());
        intent.putExtra(EXTRA_SPEAKERS, createSpeakersText());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) event.getEventId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        am.set(AlarmManager.RTC, date.getTimeInMillis() - FIVE_MINUTES, pendingIntent);
    }

    private String createSpeakersText() {
        if (speakerList == null || speakerList.isEmpty()) {
            return EMPTY_STRING;
        }
        String delimiter = ", ";

        StringBuilder builder = new StringBuilder();
        Iterator<?> it = speakerList.iterator();

        while (it.hasNext()) {
            Speaker speaker = (Speaker) it.next();
            builder.append(speaker.getFirstName())
                    .append(delimiter);
        }

        int length = builder.length();
        builder.delete(length - delimiter.length(), length);
        return builder.toString();
    }
}
