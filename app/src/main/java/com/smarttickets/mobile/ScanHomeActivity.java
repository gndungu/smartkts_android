package com.smarttickets.mobile;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScanHomeActivity extends AppCompatActivity {

    private ProcessingTask mProcessingTask = null;
    private LoadProcessingTask mLoadProcessingTask = null;
    public static final String TAG = "Sync Activity";
    String instance_code;
    String event_name;
    String provider_id;
    String scan_type;

    RelativeLayout qrScan;
    RelativeLayout qrType;
    RelativeLayout syncData;
    RelativeLayout downloadData;
    TextView txtVsyncStatus;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    private String uUsername;
    private String uPassword;
    String system_url = Config.URL;
    int uploadCount = 0;
    int asyncCall = 0;

    ConnectionDetector cd;

    ProgressDialog pd;
    ProgressDialog pds;
    private Util util;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_home);

        provider_id = getIntent().getStringExtra("PROVIDER_ID");
        instance_code = getIntent().getStringExtra("INSTANCE_CODE");
        event_name = getIntent().getStringExtra("EVENT_NAME");
        scan_type = getIntent().getStringExtra("SCAN_TYPE");
        util = new Util(this, this);

        if(provider_id == null || provider_id == ""){
            provider_id = SaveSharedPreference.getLoggedInUserProviderId(this);
        }

        Log.d("SCANHOME", "PROVIDER ID HERE " + provider_id + " Instance Code" + instance_code);

        SaveSharedPreference.setLoggedInUserInstanceCode(this, instance_code);
        SaveSharedPreference.setLoggedInUserProviderId(this, provider_id);
        SaveSharedPreference.setEvent(this, event_name);
        SaveSharedPreference.setScanType(this, scan_type);

        int tc = ticket_count();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(event_name + " - "+ tc + " [" + scan_type + "]");

        if(SaveSharedPreference.getUserLoggedInStatus(this) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(this);
            uPassword = SaveSharedPreference.getLoggedInUserPassword(this);
        }

        cd = new ConnectionDetector(this);

        pd = new ProgressDialog(ScanHomeActivity.this);

        qrScan = (RelativeLayout) findViewById(R.id.qrscan);
        qrType = (RelativeLayout) findViewById(R.id.qrtype);
        syncData = (RelativeLayout) findViewById(R.id.rlSyncData);
        downloadData = (RelativeLayout) findViewById(R.id.rlDownload);
        txtVsyncStatus = (TextView) findViewById(R.id.txtVsyncStatus);

        Log.d("SCANHOME", "Ready to Scan Event " + event_name);

        int scanned = scanned_tickets();
        if(scanned > 0) {
            txtVsyncStatus.setText("Click to Upload");
            blink();
        }

        qrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = scanned_tickets();
                if(scan_type.equals("ONLINE") && c > 0){
                    AlertDialog alertDialog = new AlertDialog.Builder(ScanHomeActivity.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Please upload the offline scanned tickets.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }

                Intent i = new Intent(ScanHomeActivity.this, FullScannerActivity.class);
                i.putExtra("INSTANCE_CODE", instance_code);
                i.putExtra("EVENT_NAME", event_name);
                i.putExtra("PROVIDER_ID", provider_id);
                i.putExtra("SCAN_TYPE", scan_type);
                startActivity(i);
            }
        });

        qrType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = scanned_tickets();
                if(scan_type.equals("ONLINE") && c > 0){
                    AlertDialog alertDialog = new AlertDialog.Builder(ScanHomeActivity.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Please upload the offline scanned tickets.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                Intent i = new Intent(ScanHomeActivity.this, TypeCodeActivity.class);
                i.putExtra("INSTANCE_CODE", instance_code);
                i.putExtra("EVENT_NAME", event_name);
                i.putExtra("PROVIDER_ID", provider_id);
                i.putExtra("SCAN_TYPE", scan_type);
                startActivity(i);
            }
        });

        syncData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cd.isConnectingToInternet()){
                    Toast.makeText(ScanHomeActivity.this, "Internet Connections is Required!", Toast.LENGTH_LONG).show();
                    return;
                }
                pd.setMessage("Updating Ticket Information");
                pd.show();
                if(!cd.isConnectingToInternet()){
                    Toast.makeText(ScanHomeActivity.this, "Internet Connections is Required!", Toast.LENGTH_LONG).show();
                    //pd.dismiss();
                    return;
                }
                upload_scanned_tickets();

            }
        });

        downloadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject post_dict = new JSONObject();
                if(!cd.isConnectingToInternet()){
                    Toast.makeText(ScanHomeActivity.this, "Internet Connections is Required!", Toast.LENGTH_LONG).show();
                    return;
                }

                int stickets = scanned_tickets();
                if(stickets > 0){
                    AlertDialog alertDialog = new AlertDialog.Builder(ScanHomeActivity.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Please upload the ticket data.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }

                pds = new ProgressDialog(ScanHomeActivity.this);
                pds.setMessage("Downloading Ticket Information");
                pds.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pds.setCancelable(false);
                pds.show();
                try {
                    post_dict.put("username" , uUsername);
                    post_dict.put("password", uPassword);
                    post_dict.put("provider_id", provider_id);
                    //post_dict.put("instancecode", Base64.encodeToString(instance_code.getBytes(), Base64.DEFAULT));
                    post_dict.put("instancecode", instance_code);
                    post_dict.put("type", "ticket_list");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mLoadProcessingTask = new LoadProcessingTask();
                mLoadProcessingTask.execute(String.valueOf(post_dict));

                dbHelper = new DatabaseHelper(getBaseContext());
                db = dbHelper.getReadableDatabase();

                String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS;
                Cursor cursor = db.rawQuery(selectQuery, null);
                //String[] data      = null;
                String[] columnNames = cursor.getColumnNames();
                System.out.println("TTICKET COUNT >>>>> "+ cursor.getCount());
                cursor.close();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.home){
            // app icon in action bar clicked; goto parent activity.
            this.finish();
            return true;
            // return super.onOptionsItemSelected(item);
        }

        if (id == R.id.action_logout) {
            util.myLogoutDialog("");
            return true;
        }

        if(id == R.id.menuterms){
            Intent it = new Intent(this, WebContentActivity.class);
            it.putExtra("URL", "https://smartkts.com/terms-of-service/?api=1");
            it.putExtra("TITLE", "Terms and Conditions");
            startActivity(it);
            return true;
        }

        if(id == R.id.menuPrivacy){
            Intent it = new Intent(this, WebContentActivity.class);
            it.putExtra("URL", "https://smartkts.com/privacy-policy/?api=1");
            it.putExtra("TITLE", "Private Policy");
            startActivity(it);
            return true;
        }

        if(id == R.id.menuGetStarted){
            Intent it = new Intent(this, WebContentActivity.class);
            it.putExtra("URL", "https://smartkts.com/getting-started/?api=1");
            it.putExtra("TITLE", "Get Started");
            startActivity(it);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume(){
        super.onResume();
        int scanned = scanned_tickets();
        txtVsyncStatus.setText("");
        if(scanned > 0) {
            txtVsyncStatus.setText("Click to Upload");
            blink();
        }
    }

    private void blink(){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeToBlink = 1000;    //in milissegunds
                try{Thread.sleep(timeToBlink);}catch (Exception e) {}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(txtVsyncStatus.getVisibility() == View.VISIBLE){
                            txtVsyncStatus.setVisibility(View.INVISIBLE);
                        }else{
                            txtVsyncStatus.setVisibility(View.VISIBLE);
                        }
                        blink();
                    }
                });
            }
        }).start();
    }

    public int ticket_count(){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + dbHelper.TABLE_TICKETS + " where " +
                dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";
        Log.v(TAG, selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);
        System.out.println("OFFLINE TICKET COUNT >>>>> "+ cursor.getCount() + " "+provider_id);
        return cursor.getCount();
    }

    public int scanned_tickets(){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT t."+dbHelper.PROVIDER_ID+", t."+dbHelper.INSTANCECODE+", st."+dbHelper.SCANDATE+", st."+dbHelper.TICKETCODE +
                ", st."+dbHelper.SCANCOUNT+", st."+dbHelper.STATUS+", st."+dbHelper.SYNCSTATUS + ", t."+dbHelper.ADMITS+ ", t."+dbHelper.PAYCLASS +
                " FROM " + dbHelper.TABLE_TICKETS + " t inner join " + dbHelper.TABLE_SCANTICKET + " st on t."+
                dbHelper.TICKETCODE + " = st." + dbHelper.TICKETCODE + " where t."+ dbHelper.INSTANCECODE + " = \"" + instance_code + "\" and st."+
                dbHelper.SYNCSTATUS+" = 0";
        Log.v("SyncData", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        uploadCount = cursor.getCount();
        return uploadCount;
    }

    public void upload_scanned_tickets(){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();
        Integer scan_count;
        String[] params = new String[]{};

        //String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where "+
        //        dbHelper.PROVIDER_ID + " = \"" + provider_id + "\" and " +
        //       dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";

        String selectQuery = "SELECT t."+dbHelper.PROVIDER_ID+", t."+dbHelper.INSTANCECODE+", st."+dbHelper.SCANDATE+", st."+dbHelper.TICKETCODE +
                ", st."+dbHelper.SCANCOUNT+", st."+dbHelper.STATUS+", st."+dbHelper.SYNCSTATUS + ", t."+dbHelper.ADMITS + ", t."+dbHelper.PAYCLASS + ", st."+dbHelper.KEY_ID + ", st."+dbHelper.SESSIONOPTION +
                " FROM " + dbHelper.TABLE_TICKETS + " t inner join " + dbHelper.TABLE_SCANTICKET + " st on t."+
                dbHelper.TICKETCODE + " = st." + dbHelper.TICKETCODE + " where st."+ dbHelper.SYNCSTATUS+" = 0 order by st.id asc";

        // " where t."+ dbHelper.INSTANCECODE + " = \"" + instance_code +

        Log.v("SyncData", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        uploadCount = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                Log.v("ScanHome", "SCANNED TICKETS FOUND ...... Provider id " + cursor.getString(0)  +
                        " Instance Code" + cursor.getString(1) +
                        " ScanDate " + cursor.getString(2)  +
                        " Ticket Code " + cursor.getString(3)  +
                        " ScanCount " + cursor.getString(4)  +
                        " Status " + cursor.getString(5) +
                        " Sync Status" +  cursor.getString(6)
                );
                if(Integer.parseInt(cursor.getString(6)) == 0){
                    String pi = cursor.getString(0);
                    String ic = cursor.getString(1);
                    String sd = cursor.getString(2);
                    String tc = cursor.getString(3);
                    String sc = cursor.getString(4);
                    String stat = cursor.getString(5);
                    String admit = cursor.getString(7);
                    String payclass = cursor.getString(8);
                    String session_log = cursor.getString(10);

                    final JSONObject post_dict = new JSONObject();
                    try {
                        post_dict.put("username" , uUsername);
                        post_dict.put("password", uPassword);
                        post_dict.put("provider_id", pi);
                        post_dict.put("instancecode", ic);
                        post_dict.put("ticketcode", tc);
                        post_dict.put("scan_date", sd);
                        post_dict.put("status", stat);
                        post_dict.put("scan_count", sc);
                        post_dict.put("valid_count", admit);
                        post_dict.put("payclass", payclass);
                        post_dict.put("session_log", session_log);
                        post_dict.put("type", "update_ticket");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mProcessingTask = new ProcessingTask();
                    mProcessingTask.execute(String.valueOf(post_dict));
                }
            } while (cursor.moveToNext());

        }else{
            pd.dismiss();
        }
    }

    public void updateScannedTicket(String ticketCode){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(dbHelper.STATUS, "synced");
        cv.put(dbHelper.SYNC_STATUS, 1);
        cv.put(dbHelper.SYNCDATE, currentDateTimeString);
        Log.d("UPDATING TICKETCODE", ticketCode);
        db.update(dbHelper.TABLE_TICKETS, cv, dbHelper.TICKETCODE + " = \"" + ticketCode +"\"",null);


        ContentValues cv2 = new ContentValues();
        cv2.put(dbHelper.SYNCSTATUS, 1);
        Log.d("UPDATING SCANTICKETCODE", ticketCode);
        db.update(dbHelper.TABLE_SCANTICKET, cv2, dbHelper.TICKETCODE + " = \"" + ticketCode +"\"",null);
    }

    public void processResponse (String response){
        String ticketcode;
        String msisdn;
        String[] vals;

        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();
            if(jsonStatus.equals("GOOD")) {
                //finish();
                //showProgress(false);
                String jsonData = obj.getString("data").toString();
                JSONObject jsonDObj = new JSONObject(jsonData);
                if(jsonDObj.getString("status").equals("updated")) {
                    ticketcode = jsonDObj.getString("ticketcode");
                    updateScannedTicket(ticketcode);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + t + "\"");
            vals = new String[]{"0", "0"};
            Config.noInternetDialog(this);
        }

        do {
            System.out.println("Response from Server COUNT "+ asyncCall);
            asyncCall++;
            //txtVsyncStatus.setText("Please update the scanned tickets");
            //pd.dismiss();
        }while (asyncCall<=uploadCount);
        pd.dismiss();
        txtVsyncStatus.setText("");
        //showMessageDialog("", vals);
        //showProgress(false);
    }

    public Boolean checkTicket(String ticketCode){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where " + dbHelper.TICKETCODE + " = \"" + ticketCode +
                "\" and " + dbHelper.INSTANCECODE + " = \""+instance_code+"\"";
        Cursor cursor = db.rawQuery(selectQuery, null);

        System.out.println("TICKET COUNT >>>>> "+ cursor.getCount());
        List<String> data = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    public void openTicketScan(View v){
        //Intent i = new Intent(LoadEventActivity.this, FullScannerActivity.class);
        Intent i = new Intent(ScanHomeActivity.this, ScanHomeActivity.class);
        i.putExtra("INSTANCE_CODE", instance_code);
        i.putExtra("EVENT_NAME", event_name);
        i.putExtra("PROVIDER_ID", provider_id);
        i.putExtra("SCAN_TYPE", "ONLINE");
        startActivity(i);
    }

    public void processLoadResponse (String response){

        String ticketcode;
        String msisdn;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy H:s:m");
        final String formattedDate = df.format(c.getTime());

        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();
            ArrayList<Item> items = new ArrayList<Item>();

            if(jsonStatus.equals("GOOD")){
                //finish();
                //showProgress(false);
                final JSONArray jsonArr = obj.getJSONArray("data");
                //String data = obj.getString("data").toString();
                System.out.println("Killing the Dialog... Bye!!");
                pds.dismiss();
                System.out.println("Calling the Dialog... hello!!");
                pd = new ProgressDialog(ScanHomeActivity.this);
                pd.setMax(jsonArr.length());
                pd.setMessage("Saving Data, please wait...");
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.show();
                pd.setCancelable(false);
                final Thread t = new Thread() {
                    @Override
                    public void run() {
                        int cnt = 0;

                        for (int i = 0; i < jsonArr.length(); i++) {

                            JSONObject ticketObject = null;
                            try {
                                ticketObject = jsonArr.getJSONObject(i);

                                pd.setProgress(i);
                                if (checkTicket(ticketObject.getString("ticketcode")) == Boolean.FALSE) {
                                    dbHelper = new DatabaseHelper(getBaseContext());
                                    db = dbHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(DatabaseHelper.PROVIDER_ID, provider_id);
                                    values.put(DatabaseHelper.INSTANCECODE, instance_code);
                                    values.put(DatabaseHelper.EVENT_NAME, event_name);
                                    values.put(DatabaseHelper.TICKETCODE, ticketObject.getString("ticketcode"));
                                    values.put(DatabaseHelper.PHONENUMBER, ticketObject.getString("msisdn"));
                                    values.put(DatabaseHelper.USAGELIMIT, ticketObject.getString("ticket_usage"));
                                    values.put(DatabaseHelper.DATEBOUGHT, ticketObject.getString("issued_on"));
                                    values.put(DatabaseHelper.ADMITS, ticketObject.getString("admits"));
                                    values.put(DatabaseHelper.STATUS, "loaded");
                                    values.put(DatabaseHelper.USED, ticketObject.getString("used"));
                                    values.put(DatabaseHelper.SCANCOUNT, ticketObject.getString("scan_count"));
                                    values.put(DatabaseHelper.SYNC_STATUS, 0);
                                    values.put(DatabaseHelper.SYNCDATE, formattedDate);
                                    values.put(DatabaseHelper.PAYCLASS, ticketObject.getString("payclass"));
                                    values.put(DatabaseHelper.TICKETNUMBER, ticketObject.getString("ticketno"));
                                    values.put(DatabaseHelper.TICKETUSER, ticketObject.getString("name"));
                                    values.put(DatabaseHelper.TICKETUSER, ticketObject.getString("name"));
                                    values.put(DatabaseHelper.TICKETREQUEST, ticketObject.getString("requests"));
                                    db.insert(DatabaseHelper.TABLE_TICKETS, null, values);
                                } else {
                                    System.out.println("UPDATE DATA" + ticketObject.toString());
                                    ContentValues cv = new ContentValues();
                                    cv.put(DatabaseHelper.PHONENUMBER, ticketObject.getString("msisdn"));
                                    cv.put(DatabaseHelper.USAGELIMIT, ticketObject.getString("ticket_usage"));
                                    cv.put(DatabaseHelper.DATEBOUGHT, ticketObject.getString("issued_on"));
                                    cv.put(DatabaseHelper.ADMITS, ticketObject.getString("admits"));
                                    cv.put(DatabaseHelper.USED, ticketObject.getString("used"));
                                    cv.put(DatabaseHelper.SCANCOUNT, ticketObject.getString("scan_count"));
                                    cv.put(DatabaseHelper.PAYCLASS, ticketObject.getString("payclass"));
                                    cv.put(dbHelper.SCANCOUNT, ticketObject.getString("scan_count"));
                                    cv.put(dbHelper.USAGELIMIT, ticketObject.getString("ticket_usage"));
                                    Log.d("UPDATING TICKETCODE", ticketObject.getString("scan_count"));
                                    db.update(dbHelper.TABLE_TICKETS, cv, dbHelper.TICKETCODE + " = \"" + ticketObject.getString("ticketcode") + "\"", null);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        pd.dismiss();
                    }
                };
                t.start();
            }else{
                //pd.dismiss();
                System.out.println("Bad response >>> " + jsonStatus + " " + response);
                Toast.makeText(this, "Error " + obj.getString("data").toString(), Toast.LENGTH_LONG);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + t + "\"");
            Config.noInternetDialog(this);
        }
        pds.dismiss();
        db.close();
    }

    public class ProcessingTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(system_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
                // json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine);// + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
                //response data
                Log.i(TAG,JsonResponse);
                //send to post execute
                return JsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            mProcessingTask = null;
            System.out.println("Response from Server>>> "+ response);
            processResponse(response);

            /**if (success=="") {
             loadHome();
             finish();
             } else {
             mPasswordView.setError(getString(R.string.error_incorrect_password));
             mPasswordView.requestFocus();
             }**/
        }

        @Override
        protected void onCancelled() {
            mProcessingTask = null;
            pd.dismiss();
        }
    }

    //Load Events AsyncTask
    public class LoadProcessingTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(system_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
                // json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine);// + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
                //response data
                Log.i(TAG,JsonResponse);
                //send to post execute
                return JsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            mProcessingTask = null;
            System.out.println("Haaaaleluya>>> "+ response);
            processLoadResponse(response);

            /**if (success=="") {
             loadHome();
             finish();
             } else {
             mPasswordView.setError(getString(R.string.error_incorrect_password));
             mPasswordView.requestFocus();
             }**/
        }

        @Override
        protected void onCancelled() {
            mProcessingTask = null;
            pd.dismiss();
        }
    }
}
