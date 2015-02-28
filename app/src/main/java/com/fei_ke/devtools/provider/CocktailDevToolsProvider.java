package com.fei_ke.devtools.provider;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.fei_ke.devtools.R;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;

/**
 * Created by 杨金阳 on 28/2/2015.
 */
public class CocktailDevToolsProvider extends SlookCocktailProvider {
    public static final String ACTION_CLICK = "com.fei_ke.devtools.action.btn_click";
    public static final String EXTRA_CLICKED_VIEW_ID = "clicked_view_id";

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailManager, int[] cocktailIds) {
        super.onUpdate(context, cocktailManager, cocktailIds);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.layout_devtools);

        setPendingIntent(context, R.id.btn_app_info, rv);
        setPendingIntent(context, R.id.btn_top_activity_info, rv);

        for (int i = 0; i < cocktailIds.length; i++) {
            cocktailManager.updateCocktail(cocktailIds[i], rv);
        }
    }


    private void setPendingIntent(Context context, int resId, RemoteViews rv) {
        Intent intent = new Intent(ACTION_CLICK);
        intent.putExtra(EXTRA_CLICKED_VIEW_ID, resId);
        PendingIntent itemClickPendingIntent = PendingIntent.getBroadcast(context, resId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(resId, itemClickPendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_CLICK.equals(action)) {
            int viewId = intent.getIntExtra(EXTRA_CLICKED_VIEW_ID, 0);
            onViewClick(context, viewId);
        }
    }

    private void onViewClick(Context context, int viewId) {
        switch (viewId) {
            case R.id.btn_app_info:
                showAppInfo(context);
                break;
            case R.id.btn_top_activity_info:
                showTopActivityInfo(context);
                break;
            default:
                break;
        }
    }

    private void showTopActivityInfo(Context context) {
        ComponentName cn = getTopComponent(context);
        Toast.makeText(context, cn.getClassName(), Toast.LENGTH_SHORT).show();
    }

    private void showAppInfo(Context context) {
        ComponentName cn = getTopComponent(context);
        String packageName = cn.getPackageName();

        Log.d("", "pkg:" + packageName);
        Log.d("", "cls:" + cn.getClassName());

        try {
            //Open the specific App Info page:
            Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:" + packageName));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            //Open the generic Apps page:
            Intent i = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    private ComponentName getTopComponent(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
        return am.getRunningTasks(1).get(0).topActivity;
    }
}
