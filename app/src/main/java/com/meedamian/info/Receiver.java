package com.meedamian.info;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import androidx.annotation.NonNull;

public class Receiver extends BroadcastReceiver {
    public Receiver() {
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                setAlarm(context);
                break;
        }
    }

    public static void setAlarm(@NonNull Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_HALF_DAY,
                PendingIntent.getService(
                        context,
                        0,
                        new Intent(context, CheckerService.class),
                        0
                ));
    }
}
