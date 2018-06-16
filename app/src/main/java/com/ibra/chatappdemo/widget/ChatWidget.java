package com.ibra.chatappdemo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class ChatWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chat_widget);


        Intent intent = new Intent(context,ChatWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list_view,intent);
        views.setEmptyView(R.id.widget_list_view,R.id.empty_widget_text_view);


        Intent startActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, startActivityPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

