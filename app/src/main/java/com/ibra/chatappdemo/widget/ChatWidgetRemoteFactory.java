package com.ibra.chatappdemo.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.fragment.ChatFragment;
import com.ibra.chatappdemo.model.Friend;
import com.ibra.chatappdemo.model.User;
import com.ibra.chatappdemo.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatWidgetRemoteFactory implements RemoteViewsService.RemoteViewsFactory {


    public static final String POSITION_EXTRA = "POSITION_EXTRA";
    private Context context;
    Intent intent;
    int appWidgetId;




    public ChatWidgetRemoteFactory(Context context, Intent intent) {
        Log.d("from widget Adapter","done");
        this.context = context;
        this.intent = intent;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);





    }




    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {


    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        Log.d("fromFactory","getViewItem");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        SharedPreferences sharedPreferences = context.getSharedPreferences(ChatFragment.WIDGET_PREF,Context.MODE_PRIVATE);
        String message = sharedPreferences.getString(ChatFragment.MESSAGE_WIDGET_PREF,null);
        String name = sharedPreferences.getString(ChatFragment.NAME_WIDGET_PREF,null);
        String time = sharedPreferences.getString(ChatFragment.TIME_WIDGET_PREF,null);
        views.setTextViewText(R.id.user_message_widget,message);
        views.setTextViewText(R.id.user_name_widget,name);
        views.setTextViewText(R.id.message_time_widget,time);
        Log.d("fromfactory","name is "+name);
        Log.d("fromfactory","message is "+message);


        Intent fillInIntent = new Intent(context, MainActivity.class);
        fillInIntent.putExtra(POSITION_EXTRA,i+"");
        views.setOnClickFillInIntent(R.id.list_view_row, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
