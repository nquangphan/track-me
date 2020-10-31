package com.phannhatquang.trackme.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.phannhatquang.trackme.data.model.MyLocation;
import com.phannhatquang.trackme.data.model.TableSession;

import java.util.List;

@Dao
public interface SessionDAO {
    @Query("SELECT * FROM tablesession")
    List<TableSession> getAll();

    @Insert
    void insertAll(TableSession... sessions);

    @Delete
    void delete(TableSession session);


    @Query("SELECT * FROM tablesession WHERE id LIKE :sessionID")
    TableSession loadSessionByID(String sessionID);

}