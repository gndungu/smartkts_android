package com.smarttickets.mobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadEventActivity extends AppCompatActivity {

   private ProcessingTask mProcessingTask = null;
   private MemberProcessingTask mmProcessingTask = null;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    private String instance_code;
    private String provider_id;
    private String event_name;
    private String uUsername;
    private String uPassword;
    private static final String TAG = "LoadEventActivity";
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy H:s:m");
    String formattedDate = df.format(c.getTime());

    private View mProgressView;
    private View mView;
    private TextView pBarText;
    private TextView mProgressStatusMessageView;
    RelativeLayout rlOnlineScan;
    RelativeLayout rlOfflineScan;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    Button offlineScan;

    String system_url = Config.URL;
    int uploadCount = 0;

    JSONObject post_dict;

    //Volley Request Queue
    private RequestQueue requestQueue;

    ProgressDialog  pd;
    private Util util;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_event);

        if(SaveSharedPreference.getUserLoggedInStatus(this) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(this);
            uPassword = SaveSharedPreference.getLoggedInUserPassword(this);
        }

        provider_id = getIntent().getStringExtra("PROVIDER_ID");
        instance_code = getIntent().getStringExtra("INSTANCE_CODE");
        event_name = getIntent().getStringExtra("EVENT_NAME");
        util = new Util(this, this);
        System.out.println("ACTIVITY 2 load PROVIDER ID" + provider_id);

        SaveSharedPreference.setLoggedInUserInstanceCode(this, provider_id);

        requestQueue = Volley.newRequestQueue(this);

        post_dict = new JSONObject();

        rlOnlineScan = (RelativeLayout) findViewById(R.id.rlOnline);
        rlOfflineScan = (RelativeLayout) findViewById(R.id.rlOffline);

        mView = findViewById(R.id.llLoadEvent);
        pBarText = (TextView) findViewById(R.id.progressBarText);
        mProgressView = findViewById(R.id.progressBarLayout);

        pd = new ProgressDialog(LoadEventActivity.this);
        pd.setMessage("Downloading, please Wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(false);

        rlOfflineScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSharedPreference.setScanType(LoadEventActivity.this, "offline");
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

                dbHelper = new DatabaseHelper(getBaseContext());
                db = dbHelper.getReadableDatabase();

                String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where " +
                        dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";
                Cursor cursor = db.rawQuery(selectQuery, null);
                //String[] data      = null;
                String[] columnNames = cursor.getColumnNames();
                System.out.println("COUNT BEFORE COUNT >>>>> "+ cursor.getCount());

                if (cursor.getCount() > 0){
                    ticketsDialog("Would like to update the local Database");
                    return;
                }

                //showProgress(true);
                pd.show();
                if(scanned_tickets() > 0) {
                    showProgress(false);
                    myDialog("Warning", "You have un-synced Tickets. Please sync them to continue.");
                    return;
                }else{
                    requestQueue.add(get_session_option());
                    mProcessingTask = new ProcessingTask();
                    mProcessingTask.execute(String.valueOf(post_dict));
                    //getData();
                }
                System.out.println("TTICKET COUNT >>>>> "+ cursor.getCount());
                List<String> data = new ArrayList<String>();
                cursor.close();
                //System.out.print("Database Data"+data);


            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(event_name);
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

        if(id == R.id.action_logout){
            util.myLogoutDialog("");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Deprecated_getMembershipTicket(){

        try {
            post_dict.put("username" , uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("provider_id", provider_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //showProgress(true);
        if(scanned_tickets() > 0) {
            showProgress(false);
            myDialog("Warning", "You have un-synced Tickets. Please sync them to continue.");
            return;
        }else{
            mmProcessingTask = new MemberProcessingTask();
            mmProcessingTask.execute(String.valueOf(post_dict));
        }

    }

    //Request to get json from server we are passing an integer here
    //This integer will used to specify the page number for the request ?page = requestcount
    //This method would return a JsonArrayRequest that will be added to the request queue
    private JsonObjectRequest getDataFromServer() {
        //Initializing ProgressBar
        final ProgressDialog pd;
        pd = new ProgressDialog(LoadEventActivity.this);
        pd.setMessage("Please Wait...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);



        //JsonArrayRequest of volley
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, system_url, post_dict,
                new Response.Listener<JSONObject >() {
                    @Override
                    public void onResponse(JSONObject  response) {
                        //Calling method parseData to parse the json response
                        System.out.println("Volley received .... " + response.toString());
                        processResponse(response.toString());
                        //Hiding the progressbar
                        //pd.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        //If an error occurs that means end of the list has reached
                        Toast.makeText(LoadEventActivity.this, "No More Items Available " + error, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        //Returning the request
        return jsonArrayRequest;
    }

    private void getData() {
        //Adding the method to the queue by calling the method getDataFromServer
        requestQueue.add(getDataFromServer());
        //Incrementing the request counter
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        showProgress(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        showProgress(false);
    }

    private void myDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                Intent it = new Intent(LoadEventActivity.this, ScanHomeActivity.class);
                it.putExtra("INSTANCE_CODE", instance_code);
                it.putExtra("EVENT_NAME", event_name);
                it.putExtra("PROVIDER_ID", provider_id);
                it.putExtra("SCAN_TYPE", "OFFLINE");
                startActivity(it);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void ticketsDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Information");
        builder.setMessage(message);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestQueue.add(get_session_option());
                mProcessingTask = new ProcessingTask();
                mProcessingTask.execute(String.valueOf(post_dict));
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                Intent it = new Intent(LoadEventActivity.this, ScanHomeActivity.class);
                it.putExtra("INSTANCE_CODE", instance_code);
                it.putExtra("EVENT_NAME", event_name);
                it.putExtra("PROVIDER_ID", provider_id);
                it.putExtra("SCAN_TYPE", "OFFLINE");
                startActivity(it);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int scanned_tickets(){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + dbHelper.TABLE_TICKETS + " t inner join " + dbHelper.TABLE_SCANTICKET + " st on t."+
                dbHelper.TICKETCODE + " = st." + dbHelper.TICKETCODE + " where t."+ dbHelper.INSTANCECODE + " = \"" + instance_code + "\" and st."+
                dbHelper.SYNCSTATUS+" = 0";
        Log.v("SyncData", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        uploadCount = cursor.getCount();
        return uploadCount;
    }


    public Boolean checkTicket(String ticketCode){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where " + dbHelper.TICKETCODE + " = \"" + ticketCode +
                "\" and " + dbHelper.INSTANCECODE + " = \""+instance_code+"\"";
        Cursor cursor      = db.rawQuery(selectQuery, null);
        List<String> data = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            //System.out.println("TICKET EXISTS >>>>> "+ cursor.getCount());
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void openTicketScan(View v){
        //Intent i = new Intent(LoadEventActivity.this, FullScannerActivity.class);
        Intent i = new Intent(LoadEventActivity.this, ScanHomeActivity.class);
        i.putExtra("INSTANCE_CODE", instance_code);
        i.putExtra("EVENT_NAME", event_name);
        i.putExtra("PROVIDER_ID", provider_id);
        i.putExtra("SCAN_TYPE", "ONLINE");
        startActivity(i);
    }

    public void deleteEvents(String event_instance){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TICKETS, DatabaseHelper.INSTANCECODE + "!= \"" + event_instance+"\" and "+DatabaseHelper.SYNC_STATUS +"=1", null);
        db.delete(DatabaseHelper.TABLE_SCANTICKET, DatabaseHelper.INSTANCECODE + "!= \"" + event_instance+"\" and "+DatabaseHelper.SYNC_STATUS +"=1", null);
        db.close();
    }


    private JsonObjectRequest get_session_option() {
        //Initializing ProgressBar
//        pbar.setMessage("Please Wait...");
//        pbar.show();
        pd.setMessage("Please Wait...");
        pd.show();


        JSONObject post_dict = new JSONObject();

        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        try {
            post_dict.put("username" , uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("provider_id", provider_id);
            post_dict.put("instance_code", instance_code);
            post_dict.put("process", "get");
            post_dict.put("type", "event");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //JsonArrayRequest of volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, system_url, post_dict,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            String jsonStatus = response.getString("status").toString();
                            if(jsonStatus.equals("GOOD")){
                                JSONArray jsonArr = response.getJSONArray("data");
                                Log.d(TAG, "Processing Str - Json...." + jsonArr.length());
                                //String data = obj.getString("data").toString();
                                String session_options;
                                for(int i = 0; i < jsonArr.length(); i++){
                                    JSONObject menuObject = jsonArr.getJSONObject(i);
                                    session_options = menuObject.getString("valid_options");
                                    try {
                                        //Thread.sleep(cnt);
                                        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_EVENTTABLE + " where " + dbHelper.PROVIDER_ID + " = \"" + provider_id +
                                                "\" and " + dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";

                                        Cursor cursor = db.rawQuery(selectQuery, null);
                                        List<String> data = new ArrayList<String>();
                                        if (!cursor.moveToFirst()) {
                                            //if (checkTicket(ticketObject.getString("ticketcode")) == Boolean.FALSE) {
                                            //System.out.println("INSERT DATA " + ticketObject.toString());
                                            ContentValues values = new ContentValues();
                                            values.put(DatabaseHelper.PROVIDER_ID, provider_id);
                                            values.put(DatabaseHelper.INSTANCECODE, instance_code);
                                            values.put(DatabaseHelper.EVENT_NAME, event_name);
                                            values.put(DatabaseHelper.SESSIONOPTION, session_options);
                                            values.put(DatabaseHelper.CREATEDATE, currentDateTimeString);
                                            db.insert(DatabaseHelper.TABLE_EVENTTABLE, null, values);
                                        } else {
                                            //System.out.println("UPDATE DATA" + ticketObject.toString());
                                            ContentValues cv = new ContentValues();
                                            cv.put(DatabaseHelper.SESSIONOPTION, session_options);
                                            cv.put(DatabaseHelper.CREATEDATE, currentDateTimeString);
                                            Log.d("UPDATING EVENT", "Option "+session_options);
                                            db.update(dbHelper.TABLE_EVENTTABLE, cv, dbHelper.PROVIDER_ID + " = \"" + provider_id  + "\" and " + dbHelper.INSTANCECODE + " = \"" + instance_code + "\"", null);
                                        }
                                        cursor.close();
                                    }catch (Exception j){
                                        Log.e(TAG, "Error in Loop JSON: \"" + j + "\"");
                                    }
                                }

                                //pd.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //If an error occurs that means end of the list has reached
                Log.v("RegisterUserError: ", ""+ error);
                Toast.makeText(LoadEventActivity.this, "No More Items Available", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        System.out.print(jsonObjectRequest);
        //Returning the request
        return jsonObjectRequest;
    }


    public void processResponse (String response){
        String ticketcode;
        String msisdn;

        try {
            JSONObject obj = new JSONObject(response);
            //Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();
            ArrayList<Item> items = new ArrayList<Item>();


            if(jsonStatus.equals("GOOD")){
                //finish();
                //showProgress(false);
                //deleteEvents(instance_code);
                final JSONArray jsonArr = obj.getJSONArray("data");
                dbHelper = new DatabaseHelper(getBaseContext());
                db = dbHelper.getWritableDatabase();
                //pd.show();
                //String data = obj.getString("data").toString();
                final int totalProgressTime = 100;
                pd.dismiss();
                pd = new ProgressDialog(LoadEventActivity.this);
                pd.setMax(jsonArr.length());
                pd.setMessage("Saving Data, please wait...");
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.show();
                pd.setCancelable(false);
                //pd.setIndeterminate(true);
                final Thread t = new Thread() {
                    @Override
                    public void run() {
                        int cnt = 0;

                        for (int i = 0; i < jsonArr.length(); i++) {
                            try {
                                cnt++;
                                //Thread.sleep(cnt);
                                pd.setProgress(i);

                                JSONObject ticketObject = jsonArr.getJSONObject(i);
//                                Log.d("JSON DATA", ticketObject.toString());
                                String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where " + dbHelper.TICKETCODE + " = \"" + ticketObject.getString("ticketcode") +
                                        "\" and " + dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";
                                Cursor cursor = db.rawQuery(selectQuery, null);
                                List<String> data = new ArrayList<String>();
                                if (!cursor.moveToFirst()) {
                                    //if (checkTicket(ticketObject.getString("ticketcode")) == Boolean.FALSE) {
                                    //System.out.println("INSERT DATA " + ticketObject.toString());
                                    ContentValues values = new ContentValues();
                                    values.put(DatabaseHelper.PROVIDER_ID, provider_id);
                                    values.put(DatabaseHelper.INSTANCECODE, instance_code);
                                    values.put(DatabaseHelper.EVENT_NAME, event_name);
                                    values.put(DatabaseHelper.TICKETCODE, ticketObject.getString("ticketcode"));
                                    values.put(DatabaseHelper.PHONENUMBER, ticketObject.getString("msisdn"));
                                    values.put(DatabaseHelper.USAGELIMIT, ticketObject.getString("ticket_usage"));
                                    values.put(DatabaseHelper.DATEBOUGHT, ticketObject.getString("issued_on"));
                                    values.put(DatabaseHelper.ADMITS, ticketObject.getString("admits"));
                                    values.put(DatabaseHelper.SCANCOUNT, ticketObject.getString("scan_count"));
                                    values.put(DatabaseHelper.STATUS, "loaded");
                                    values.put(DatabaseHelper.USED, ticketObject.getString("used"));
                                    values.put(DatabaseHelper.SYNC_STATUS, 0);
                                    values.put(DatabaseHelper.SYNCDATE, formattedDate);
                                    values.put(DatabaseHelper.PAYCLASS, ticketObject.getString("payclass"));
                                    values.put(DatabaseHelper.TICKETNUMBER, ticketObject.getString("ticketno"));
                                    values.put(DatabaseHelper.TICKETUSER, ticketObject.getString("name"));
                                    values.put(DatabaseHelper.TICKETREQUEST, ticketObject.getString("requests"));
                                    Log.d("INSERT TICKETCODE", ticketObject.getString("scan_count"));
                                    db.insert(DatabaseHelper.TABLE_TICKETS, null, values);
                                } else {
                                    //System.out.println("UPDATE DATA" + ticketObject.toString());
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
                                cursor.close();
                            }catch (Exception j){
                                Log.e(TAG, "Error in Loop JSON: \"" + j + "\"");
                            }
                        }
                        pd.dismiss();
                        Intent it = new Intent(LoadEventActivity.this, ScanHomeActivity.class);
                        it.putExtra("INSTANCE_CODE", instance_code);
                        it.putExtra("EVENT_NAME", event_name);
                        it.putExtra("PROVIDER_ID", provider_id);
                        it.putExtra("SCAN_TYPE", "OFFLINE");
                        startActivity(it);
                    }
                };
                t.start();

            }else{
                pd.dismiss();
                //showProgress(false);
                //System.out.println("Bad response >>> " + jsonStatus + " " + response);
                Toast.makeText(this, "Error " + obj.getString("data").toString(), Toast.LENGTH_LONG);
            }
        } catch (Throwable t) {
            pd.dismiss();
            Log.e(TAG, "Could not parse malformed JSON: \"" + t + "\"");
        }

    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        pBarText.setText("Dowloading Data...Please wait");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mView.setVisibility(show ? View.GONE : View.VISIBLE);
            mView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
                //Log.i(TAG,JsonResponse);
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
            //System.out.println("Haaaaleluya>>> "+ response);
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
            //showProgress(false);
        }
    }


    public class MemberProcessingTask extends AsyncTask<String, String, String> {

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
                Log.i(TAG+"SECOND",JsonResponse);
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
            mmProcessingTask = null;
            //System.out.println("Haaaaleluya>>> "+ response);
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
            mmProcessingTask = null;
            //showProgress(false);
        }
    }
}
