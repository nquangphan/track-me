package com.phannhatquang.trackme;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LifecycleObserver;
import androidx.room.Room;

import com.phannhatquang.trackme.data.localdatabase.AppDatabase;

import org.androidannotations.annotations.EApplication;

@EApplication
public class TrackMeApplication extends Application implements LifecycleObserver {
    private static TrackMeApplication sInstance;
   public AppDatabase appDatabase;

    public static synchronized TrackMeApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").fallbackToDestructiveMigration().build();

    }



    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
