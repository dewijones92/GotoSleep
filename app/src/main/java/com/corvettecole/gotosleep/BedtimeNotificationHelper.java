package com.corvettecole.gotosleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.CURRENT_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.FIRST_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.ONE_DAY_MILLIS;
import static com.corvettecole.gotosleep.MainActivity.getBedtimeCal;
import static com.corvettecole.gotosleep.MainActivity.parseBedtime;
import static com.corvettecole.gotosleep.SettingsFragment.BEDTIME_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_ENABLE_KEY;

public class BedtimeNotificationHelper extends BroadcastReceiver {

    private Calendar bedtime;
    private final String TAG = "NotificationHelper";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Device booted, broadcast received, setting bedtime notification");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled = settings.getBoolean(NOTIF_ENABLE_KEY, true);

        if (notificationsEnabled) {
            bedtime = Calendar.getInstance();
            bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "22:00")));
            if (bedtime.getTimeInMillis() < System.currentTimeMillis()){
                bedtime.setTimeInMillis(bedtime.getTimeInMillis() + ONE_DAY_MILLIS);
            }
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
            setBedtimeNotification(context, bedtime);
        }
    }

    private void setBedtimeNotification(Context context, Calendar bedtime){
        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtime.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, bedtime.getTimeInMillis(), pendingIntent);
        }


    }

}
