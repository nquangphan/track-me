package com.phannhatquang.trackme.data.localdatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.phannhatquang.trackme.data.dao.LocatioinDAO;
import com.phannhatquang.trackme.data.model.MyLocation;

@Database(entities = {MyLocation.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocatioinDAO userDao();
}