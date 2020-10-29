package com.phannhatquang.trackme.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.phannhatquang.trackme.data.model.MyLocation;

import java.util.List;

@Dao
public interface LocatioinDAO {
    @Query("SELECT * FROM mylocation")
    List<MyLocation> getAll();

    @Query("SELECT * FROM mylocation WHERE sessionID LIKE :sessionID")
    List<MyLocation> loadAllBySessionIds(String sessionID);

    @Insert
    void insertAll(MyLocation... locations);

    @Delete
    void delete(MyLocation location);
}