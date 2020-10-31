package com.phannhatquang.trackme.data.localdatabase;

import android.se.omapi.Session;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.phannhatquang.trackme.data.dao.LocatioinDAO;
import com.phannhatquang.trackme.data.dao.SessionDAO;
import com.phannhatquang.trackme.data.model.MyLocation;
import com.phannhatquang.trackme.data.model.TableSession;

@Database(entities = {MyLocation.class, TableSession.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocatioinDAO locationDao();
    public abstract SessionDAO sessionDAO();
}