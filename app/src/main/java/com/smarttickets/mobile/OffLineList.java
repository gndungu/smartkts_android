package com.smarttickets.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class OffLineList extends AppCompatActivity {

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    private String instance_code;
    private String provider_id;
    private String event_name;
    private String uUsername;
    private String uPassword;
    int uploadCount = 0;

    ListView listview;
    private static final String TAG = "OfflineList";

    MyOfflineAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line_list);
        setTitle("Offline Validation");

        listview = (ListView) findViewById(R.id.lvOfflineList);

        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        provider_id = getIntent().getStringExtra("PROVIDER_ID");

        System.out.println("ACTIVITY TO offline PROVIDER ID" + provider_id);

        String selectQuery = "SELECT " + dbHelper.INSTANCECODE + ", " + dbHelper.EVENT_NAME + " , " + dbHelper.PROVIDER_ID + " , MAX(" +
                dbHelper.SYNCDATE + ") FROM " + dbHelper.TABLE_TICKETS + " group by " + dbHelper.INSTANCECODE + ", " + dbHelper.EVENT_NAME;
        Cursor cursor      = db.rawQuery(selectQuery, null);
        //String[] data      = null;
        System.out.println("OFFLINE TICKET COUNT >>>>> "+ cursor.getCount() + " "+provider_id);

        List<String> data = new ArrayList<String>();
        ArrayList<OfflineItem> items = new ArrayList<OfflineItem>();
        String ic = "";

        if (cursor.moveToFirst()) {
            do {
                System.out.print("OFFLINE DB SELECT" + cursor.getString(0));
                byte[] ic_data = Base64.decode(cursor.getString(0), Base64.DEFAULT);
                try {
                    ic = new String(ic_data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Integer cnt = getDatabaseTicketCode(cursor.getString(0));
                items.add(new OfflineItem(cursor.getString(1) , cursor.getString(3), ""+cnt, cursor.getString(0)));
            } while (cursor.moveToNext());
            adapter = new MyOfflineAdapter(this, items);
            listview.setAdapter(adapter);
        }


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String  itemValue = "Data";//(String) listview.getItemAtPosition(position);

                final TextView catdisplay = (TextView) view.findViewById(R.id.txtOEvent);
                final TextView selected = (TextView) view.findViewById(R.id.txtOInstanceCode);
                //System.out.println(" Item "+selected.getText().toString());
                final String item = selected.getText().toString();
                final String cat = catdisplay.getText().toString();

                int stickets = scanned_tickets(item);
                if(stickets > 0){
                    AlertDialog alertDialog = new AlertDialog.Builder(OffLineList.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Please upload the offline scanned tickets for the other events first.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }

                //Intent view_intent = new Intent(OffLineList.this, FullScannerActivity.class);
                Intent view_intent = new Intent(OffLineList.this, ScanHomeActivity.class);
                view_intent.putExtra("INSTANCE_CODE", item);
                view_intent.putExtra("EVENT_NAME", cat);
                view_intent.putExtra("PROVIDER_ID", provider_id);
                view_intent.putExtra("SCAN_TYPE", "OFFLINE");

                SaveSharedPreference.setLoggedInUserInstanceCode(OffLineList.this, item);
                //SaveSharedPreference.setLoggedInUserProviderId(OffLineList.this, provider_id);
                startActivity(view_intent);
            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Offline Activities");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Toast.makeText(this, "Resuming....", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                // return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    private int scanned_tickets(String ic){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + dbHelper.TABLE_SCANTICKET + " where "+ dbHelper.INSTANCECODE + " != \"" + ic + "\" and "+ dbHelper.SYNCSTATUS+" = 0";

        Cursor cursor = db.rawQuery(selectQuery, null);
        uploadCount = cursor.getCount();
        Log.v("SyncData", selectQuery + " Result: "+ uploadCount);
        return uploadCount;
    }

    public Integer getDatabaseTicketCode(String instance_code){

        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where " + dbHelper.INSTANCECODE +" = \"" + instance_code + "\"";
        Log.v(TAG, selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor.getCount();

    }
}
