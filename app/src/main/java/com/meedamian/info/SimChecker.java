package com.meedamian.info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

class SimChecker extends PermChecker {

    static final String PERMISSION = Manifest.permission.READ_PHONE_STATE;

    private static final int PHONE_CHANGED_NOTIFICATION_ID = 666;

    private Context c;

    SimChecker(Context c) {
        this.c = c;

        if (!isPermitted(c))
            return;

        // There's no SIM present - ignore
        String currentSubscriber = getCurrentSubscriberId(c);
        if (currentSubscriber == null)
            return;

        // That's the first read - just save
        String cachedSubscriber = getCachedSubscriberId();
        if (cachedSubscriber == null) {
            cacheNewSubscriber(currentSubscriber);
            return;
        }

        // SIM changed - notify
        if (!cachedSubscriber.equals(currentSubscriber)) {
            cacheNewSubscriber(currentSubscriber);
            showSimChangedNotification(c);
        }
    }

    private String getCachedSubscriberId() {
        return LocalData.getString(c, LocalData.SUBSCRIBER_ID);
    }

    private void cacheNewSubscriber(String newSubscriberId) {
        LocalData.cacheString(c, LocalData.SUBSCRIBER_ID, newSubscriberId);
    }

    @SuppressLint("HardwareIds")
    private String getCurrentSubscriberId(@NonNull Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    private void showSimChangedNotification(@NonNull Context c) {
        String phoneNo = LocalData.getString(c, RemoteData.PHONE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(c, "simPerm")
                        .setSmallIcon(R.drawable.ic_sim_card_black_24dp)
                        .setContentTitle("SIM card changed")
                        .setContentText(String.format("Is %s your current phone number?", phoneNo))
                        .addAction(R.drawable.ic_edit_black_24dp, "Change", getAppPendingIntent(c))
                        .setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setContentIntent(getAppPendingIntent(c));

        getNotificationManager(c).notify(PHONE_CHANGED_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    protected String getPermission() {
        return PERMISSION;
    }

    @Override
    protected int getSmallIcon() {
        return R.drawable.ic_sim_card_black_24dp;
    }

    @Override
    protected int getText() {
        return R.string.phone_permission_content_text;
    }

    @Override
    protected int getNotificationId() {
        return MISSING_PHONE_PERM;
    }
}
