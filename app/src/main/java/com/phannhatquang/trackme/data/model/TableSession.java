package com.phannhatquang.trackme.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TableSession {
    @PrimaryKey() @NonNull
    public String id;

    public double speed;

    public double distance;

    public long time;

    public long stateDate;
}
