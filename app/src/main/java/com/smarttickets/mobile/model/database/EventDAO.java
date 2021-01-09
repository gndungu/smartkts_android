package com.smarttickets.mobile.model.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface EventDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(Event event);

    @Update
    public void update(Event event);

    @Delete
    public void delete(Event event);

    @Query("Select * from event order by create_date desc")
    public List<Event> getEvents();
}
