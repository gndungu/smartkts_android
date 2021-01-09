package com.smarttickets.mobile.model.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TicketDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(Ticket ticket);

    @Update
    public void update(Ticket ticket);

    @Delete
    public void delete(Ticket ticket);

    @Query("Select * from ticket order by create_date desc")
    public List<Ticket> getTicket();
}
