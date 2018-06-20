package com.ibra.chatappdemo.widget;

import android.app.WallpaperInfo;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.fragment.ChatFragment;
import com.ibra.chatappdemo.fragment.FreindFragment;
import com.ibra.chatappdemo.fragment.RequestFragment;
import com.ibra.chatappdemo.model.Friend;
import com.ibra.chatappdemo.model.Message;
import com.ibra.chatappdemo.model.User;
import com.ibra.chatappdemo.model.Widget;
import com.ibra.chatappdemo.ui.MainActivity;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

public class ChatWidgetRemoteFactory implements RemoteViewsService.RemoteViewsFactory {


    public static final String WIDGET_EXTRA = "POSITION_EXTRA";
    private Context context;
    private ArrayList<User> list;
    private String name;


    public ChatWidgetRemoteFactory(Context context) {
        Log.d("from widget Adapter","done");
        this.context = context;
        list = new ArrayList<>();





    }




    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Log.d("DataSetChanged","inside");
        list = getData();

    }



    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if(list != null && list.size() > 0){
            return list.size();
        }else return 0;
    }

    @Override
    public RemoteViews getViewAt(final int i) {

        Log.d("fromFactorygetViewAt","inside");
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        if(list != null){

        }else Log.d("fromFactorygetData","name is null");


        views.setTextViewText(R.id.user_name_widget,list.get(i).getuName());


        Intent fillInIntent = new Intent(context, MainActivity.class);
        fillInIntent.putExtra(WIDGET_EXTRA,"intent from widget");
        views.setOnClickFillInIntent(R.id.list_view_row, fillInIntent);

        return views;
    }


    private ArrayList<User> getData() {
        Log.d("WidgetProcess","inside getData");
        SharedPreferences sharedPreferences = context.getSharedPreferences(RequestFragment.WIDGET_PREF,Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json  = sharedPreferences.getString(RequestFragment.EDITOR_WIDGET_PREF,null);
        Log.d("WidgetProcess","json in getData is"+json);
        Type type = new TypeToken<ArrayList<User>>(){}.getType();
        ArrayList<User> arrayList = gson.fromJson(json,type);

        return arrayList;

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
