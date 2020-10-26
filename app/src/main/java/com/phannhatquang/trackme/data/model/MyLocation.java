package com.phannhatquang.trackme.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MyLocation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String sessionID;

    public double latitude;

    public double longitude;
}
