package com.doodlyz.vlove.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.doodlyz.vlove.VloveSettings;
import com.doodlyz.vlove.apis.VAPIS;
import com.doodlyz.vlove.ui.dialogs.Popup;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PopupNotificationService extends NotificationListenerService {

    Handler mHandler;

    @Override
    public void onCreate() {
        mHandler = new Handler(getApplicationContext().getMainLooper());
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (!VAPIS.isExpired(this)) {
            if (isFromVApp(sbn)) {
                Bundle extras = sbn.getNotification().extras;
                String title = getTitle(extras);

                if (bestMatch(title)) {
                    String text = getMessage(extras);
                    if (text == null) text = "";

                     //Validate channel with white & black list channel
                    if (isChannelValid(text)) {
                        PendingIntent intent = sbn.getNotification().contentIntent;

                        showPopup(title, text, intent);
                    }
                }
            }
        }
    }

    private void showPopup(final String title, final String text, final PendingIntent intent) {
        runOnUiThread(() -> Popup.with(this, Popup.ID_WATCH).make(title, text).setAction(intent).show());
    }

    private void runOnUiThread(Runnable runnable) {
        mHandler.postDelayed(runnable, 100);
    }

    private String getTitle(Bundle notificationExtras) {
        String title;
        title = notificationExtras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
        if (title.isEmpty()) {
            title = notificationExtras.getCharSequence(Notification.EXTRA_TITLE_BIG, "").toString();
            if (title.isEmpty()) {
                title = null;
            }
        }

        return title;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private String getMessage(Bundle notificationExtras) {
        String message;
        message = notificationExtras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
        if (message.isEmpty()) {
            message = notificationExtras.getCharSequence(Notification.EXTRA_SUB_TEXT, "").toString();
            if (message.isEmpty()) {
                message = notificationExtras.getCharSequence(Notification.EXTRA_INFO_TEXT, "").toString();
                if (message.isEmpty()) {
                    message = notificationExtras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT, "").toString();
                    if (message.isEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            message = notificationExtras.getCharSequence(Notification.EXTRA_BIG_TEXT, "").toString();
                        }
                        if (message.isEmpty()) {
                            message = null;
                        }
                    }
                }
            }
        }

        return message;
    }

    private boolean isFromVApp(StatusBarNotification mSBNotification) {
        return mSBNotification.getPackageName().equals("com.naver.vapp");
    }

    private boolean bestMatch(String line) {
        if (line == null) return false;
        String pattern = "(download|upload|sav)(ing|ed|e)?";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(line.toLowerCase(Locale.US));
        return !m.find();
//        return true;
    }

    private boolean isChannelValid(String message) {
        String[] component = message.split(":");
        String[] listChannel;
        String currentChannel;

        if (component.length > 2) {

            currentChannel = component[0].trim();

            listChannel = VloveSettings.getInstance(this).getWhitelistChannel();

            if (listChannel != null) {

                for (String channel : listChannel) {

                    if (currentChannel.equalsIgnoreCase(channel.trim()))
                        return true;

                }

            }

            listChannel = VloveSettings.getInstance(this).getBlacklistChannel();

            if (listChannel != null) {

                for (String channel : listChannel) {

                    if (currentChannel.equalsIgnoreCase(channel.trim()))
                        return false;
                }

            }

        }

        return true;
    }
}
