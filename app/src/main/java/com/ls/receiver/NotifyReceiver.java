package com.ls.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.ls.drupalcon.R;
import com.ls.ui.activity.EventDetailsActivity;
import com.ls.ui.activity.HomeActivity;
import com.ls.utils.AlarmTask;

public class NotifyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra(AlarmTask.EXTRA_ID, -1);
        long day = intent.getLongExtra(AlarmTask.EXTRA_DAY, -1);
        String text = intent.getStringExtra(AlarmTask.EXTRA_TEXT);
        String room = intent.getStringExtra(AlarmTask.EXTRA_ROOM);
        String speakers = intent.getStringExtra(AlarmTask.EXTRA_SPEAKERS);
        showNotification(context, eventId, day, text, room, speakers);
    }

    private void showNotification(Context context, long id, long day, String sessionTitle, String room, String speakers) {
        int icon = android.R.drawable.ic_dialog_info;

        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, id);
        intent.putExtra(EventDetailsActivity.EXTRA_DAY, day);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Resources resources = context.getResources();
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(resources.getString(R.string.session_about_to_start))
                .setContentTitle(sessionTitle)
                .setAutoCancel(true);

        NotificationCompat.BigTextStyle richNotification = createBigTextRichNotification(
                notifBuilder,
                speakers,
                room,
                sessionTitle,
                resources
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) id, richNotification.build());
    }

    private NotificationCompat.BigTextStyle createBigTextRichNotification(NotificationCompat.Builder notifBuilder,
                                                                          String speakers,
                                                                          String roomName,
                                                                          String sessionTitle,
                                                                          Resources resources) {
        StringBuilder bigTextBuilder = new StringBuilder()
                .append(sessionTitle)
                .append('\n');
        if (!TextUtils.isEmpty(speakers)) {
            bigTextBuilder.append(resources.getString(R.string.session_starting_by, speakers))
                    .append('\n');
        }
        bigTextBuilder.append(resources.getString(R.string.session_starting_in, roomName));

        return new NotificationCompat.BigTextStyle(
                notifBuilder)
                .setBigContentTitle(resources.getString(R.string.session_about_to_start))
                .bigText(bigTextBuilder.toString());
    }
}
