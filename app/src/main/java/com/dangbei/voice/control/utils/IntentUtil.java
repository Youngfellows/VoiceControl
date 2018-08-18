package com.dangbei.voice.control.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;

/**
 * Created by Byron on 2018/8/18.
 */

public class IntentUtil {
    private static String TAG = "IntentUtil";

    /**
     * 将隐性调用变成显性调用
     *
     * @param context
     * @param implicitIntent
     * @return
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        Intent explicitIntent = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            List resolveInfo = packageManager.queryIntentServices(implicitIntent, 0);
            if (resolveInfo == null || resolveInfo.size() != 1) {
                Log.e(TAG, "resolveInfo:" + resolveInfo);
                return null;
            }

            ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
            String packageName = serviceInfo.serviceInfo.packageName;
            String className = serviceInfo.serviceInfo.name;
            ComponentName component = new ComponentName(packageName, className);
            explicitIntent = new Intent(implicitIntent);
            explicitIntent.setComponent(component);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return explicitIntent;
    }
}
