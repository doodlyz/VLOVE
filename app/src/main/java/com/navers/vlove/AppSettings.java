package com.navers.vlove;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.view.ContextThemeWrapper;

import com.navers.vlove.services.DownloaderService;
import com.navers.vlove.services.PopupNotificationService;
import com.navers.vlove.services.SaverService;
import com.navers.vlove.ui.dialogs.Popup;
import com.navers.vlove.views.BoardScreenActivity;
import com.navers.vlove.views.LaterScreenActivity;
import com.navers.vlove.views.MenuScreenActivity;

import java.lang.ref.WeakReference;
import java.util.Set;

public class AppSettings {
    private static AppSettings mInstance;

    private static String KEY_POPUP_VIBRATE;
    private static String KEY_POPUP_WCHANNEL;
    private static String KEY_POPUP_BCHANNEL;
    private static String KEY_BOARD_INTERVAL;
    private static String KEY_BOARD_USERNAME;
    private static String KEY_SAVER_DPATH;

    private WeakReference<Context> mContext;
    private SharedPreferences mSharedPreferences;

    private AppSettings(Context context) {
        mContext = new WeakReference<>(context.getApplicationContext());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.get());

        setAllKey();
    }

    private Context getContext() {
        return mContext.get();
    }

    private void setAllKey() {
        KEY_POPUP_VIBRATE = getContext().getString(R.string.pref_key_is_vibrate);
        KEY_POPUP_WCHANNEL = getContext().getString(R.string.pref_key_whitelist_channel);
        KEY_POPUP_BCHANNEL = getContext().getString(R.string.pref_key_blacklist_channel);
        KEY_BOARD_INTERVAL = getContext().getString(R.string.pref_key_sync_post_interval);
        KEY_BOARD_USERNAME = getContext().getString(R.string.pref_key_user);
        KEY_SAVER_DPATH = getContext().getString(R.string.pref_key_download_path);
    }

    public static synchronized AppSettings getInstance(Context context) {
        boolean contextValid = checkContext(context);
        if (contextValid) {
            if (mInstance == null || mInstance.getContext() == null) {
                mInstance = new AppSettings(context);
            }
            return mInstance;
        }
        else {
            throw new RuntimeException("Context not valid!");
        }
    }

    public boolean isVLiveInstalled() {
        return isAppInstalled(getContext(), "com.naver.vapp");
    }

    public boolean isPopupUseVibrate() {
        return mSharedPreferences.getBoolean(KEY_POPUP_VIBRATE, false);
    }

    public boolean isPopupEnabled() {
        return isNotificationListenerEnabled(getContext(), getContext().getPackageName());
    }

    public String[] getWhitelistChannel() {
        return validateListChannel(mSharedPreferences.getString(KEY_POPUP_WCHANNEL, "").split(";"));
    }

    public String[] getBlacklistChannel() {
        return validateListChannel(mSharedPreferences.getString(KEY_POPUP_BCHANNEL, "").split(";"));
    }

    public long getBoardSyncInterval() {
        return Long.parseLong(mSharedPreferences.getString(KEY_BOARD_INTERVAL, "-1"));
    }

    public String getBoardUsername() {
        return mSharedPreferences.getString(KEY_BOARD_USERNAME, "");
    }

    public String getSaverDownloadPath() {
        return mSharedPreferences.getString(KEY_SAVER_DPATH, "1").equals("0") ? getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
    }

    private String[] validateListChannel(String[] listChannel) {
        return listChannel.length == 1 && listChannel[0].trim().isEmpty() ? null : listChannel;
    }

    private boolean isAppInstalled(Context context, String appPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(appPackage, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isNotificationListenerEnabled(Context context, String packageOrClassName) {
        Set<String> listPackageApp = NotificationManagerCompat.getEnabledListenerPackages(context);
        for (String packageApp : listPackageApp) {
            if (packageApp.contains(packageOrClassName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkContext(Context context) {
        return context instanceof MenuScreenActivity ||
                context instanceof PopupNotificationService ||
                context instanceof BoardScreenActivity ||
                context instanceof LaterScreenActivity ||
                context instanceof SaverService ||
                context instanceof SettingsActivity ||
                context instanceof DownloaderService ||
                context instanceof Popup ||
                isContextFromBroadcast(context);
    }

    private static boolean isContextFromBroadcast(Context context) {
        return !(context instanceof Activity) &&
                !(context instanceof Service) &&
                !(context instanceof Application) &&
                !(context instanceof ContextThemeWrapper) &&
                context instanceof ContextWrapper;
    }
}
