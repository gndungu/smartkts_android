package com.smarttickets.mobile.model.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TicketCategoryDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(TicketCategory ticketCategory);

    @Update
    public void update(TicketCategory ticketCategory);

    @Delete
    public void delete(TicketCategory ticketCategory);

    @Query("Select * from ticket_category order by create_date desc")
    public List<TicketCategory> getTicketCategory();
}
