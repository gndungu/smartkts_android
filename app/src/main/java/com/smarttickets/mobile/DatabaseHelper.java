package com.smarttickets.mobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by gndungu on 9/9/2017.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOGCAT = null;
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 6;

    // Database Name
    private static final String DATABASE_NAME = "smartkts_android_db";

    //Database Tables

    public static final String TABLE_TICKETS = "smartkts_tickets";
    public static final String TABLE_SCANTICKET = "smartkts_scan_ticket";

    //Table Attributes
    public static final String KEY_ID = "id";
    public static final String PROVIDER_ID = "provider_id";
    public static final String INSTANCECODE = "instancecode";
    public static final String EVENT_NAME = "event_name";
    public static final String TICKETCODE = "ticketcode";
    public static final String PHONENUMBER = "phone_number";
    public static final String USAGELIMIT = "usage_limit";
    public static final String DATEBOUGHT = "date_bought";
    public static final String STATUS = "status";
    public static final String SCANCOUNT = "scan_count";
    public static final String USED = "used";
    public static final String ADMITS = "admits";
    public static final String SYNCDATE = "sync_date";
    public static final String SYNC_STATUS = "sync_status";
    public static final String SCANDATE = "scan_date";
    public static final String CREATEDATE = "create_date";
    public static final String PAYCLASS = "pay_class";
    public static final String TICKETNUMBER = "ticket_number";
    public static final String TICKETUSER = "ticket_user";
    public static final String TICKETREQUEST = "ticket_request";
    public static final String SESSIONOPTION = "session_option";

    public static final String SYNCSTATUS = "sync_status";
    public static final String TABLE_EVENTTABLE = "event_table";


    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //Log.d(LOGCAT,"Database Access ....");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //System.out.println("Creating Table");

        String query;
        String query2;
        String query3;

        query = "CREATE TABLE "+ TABLE_TICKETS +" (" +
                KEY_ID + " INTEGER PRIMARY KEY autoincrement, " +
                PROVIDER_ID + " TEXT, " +
                EVENT_NAME + " TEXT, " +
                INSTANCECODE + " TEXT," +
                TICKETCODE + " TEXT," +
                PHONENUMBER + " TEXT,"+
                USAGELIMIT + " TEXT,"+
                DATEBOUGHT+" TEXT,"+
                STATUS+" TEXT," +
                USED+" TEXT," +
                SCANCOUNT+" TEXT," +
                ADMITS + " TEXT, " +
                SCANDATE +" TEXT, " +
                SYNC_STATUS + " INTEGER, " +
                SYNCDATE +" TEXT, " +
                PAYCLASS +" TEXT, "+
                TICKETNUMBER + " TEXT, "+
                TICKETUSER + " TEXT, " +
                TICKETREQUEST + " TEXT)";

        query2 = "CREATE TABLE "+ TABLE_SCANTICKET +" (" +
                KEY_ID + " INTEGER PRIMARY KEY autoincrement, " +
                TICKETCODE + " TEXT," +
                INSTANCECODE + " TEXT," +
                STATUS + " TEXT," +
                SCANCOUNT + " TEXT," +
                SYNCSTATUS + " INTEGER," +
                SESSIONOPTION + " TEXT, "+
                SCANDATE + " TEXT)";

        query3 = "CREATE TABLE "+ TABLE_EVENTTABLE +" (" +
                KEY_ID + " INTEGER PRIMARY KEY autoincrement, " +
                INSTANCECODE + " TEXT," +
                PROVIDER_ID + " TEXT," +
                EVENT_NAME + " TEXT," +
                SESSIONOPTION + " TEXT, "+
                CREATEDATE + " TEXT)";

        try{
            db.execSQL(query);
            db.execSQL(query2);
            db.execSQL(query3);
            System.out.println("Tables Created .....");

        }catch (Exception e) {
            System.out.println("Created Table Error: "+ e);
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        String query2;
        String query3;
        try{
            query = "DROP TABLE IF EXISTS "+ TABLE_TICKETS;
            db.execSQL(query);
            Log.d(LOGCAT, TABLE_TICKETS + " Table Dropped...." + query);

            query2 = "DROP TABLE IF EXISTS "+ TABLE_SCANTICKET;
            db.execSQL(query2);
            Log.d(LOGCAT, TABLE_SCANTICKET + " Table Dropped...." + query2);

            query3 = "DROP TABLE IF EXISTS "+ TABLE_EVENTTABLE;
            db.execSQL(query3);
            Log.d(LOGCAT, TABLE_EVENTTABLE + " Table Dropped...." + query3);
        }catch (Exception e) {
            System.out.println("UPDATE Table Error: "+ e);
            e.printStackTrace();
        }
        onCreate(db);
    }

}

