package com.smarttickets.mobile;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeCodeActivity extends AppCompatActivity  implements MessageDialogFragment.MessageDialogListener {

    private ProcessingTask mProcessingTask = null;


    public static final String TAG = "TypeCode Activity";

    String instance_code;
    String event_name;
    String provider_id;
    private String uUsername;
    private String uPassword;
    String scan_type;

    EditText myTicketcode;
    Button myCheckTicket;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    String netStat = "o";
    private String session_option;

    Button mySession;

    String system_url = Config.URL;

    ProgressDialog pd;

    //Volley Request Queue
    private RequestQueue requestQueue;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_code);

        if(SaveSharedPreference.getUserLoggedInStatus(this) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(this);
            uPassword = SaveSharedPreference.getLoggedInUserPassword(this);
            session_option = SaveSharedPreference.getSessionOption(this);
        }

        provider_id = getIntent().getStringExtra("PROVIDER_ID");
        instance_code = getIntent().getStringExtra("INSTANCE_CODE");
        event_name = getIntent().getStringExtra("EVENT_NAME");
        scan_type = getIntent().getStringExtra("SCAN_TYPE");
        util = new Util(this, this);
        requestQueue = Volley.newRequestQueue(this);

        pd = new ProgressDialog(TypeCodeActivity.this);

        actionB(session_option);

        myTicketcode = (EditText) findViewById(R.id.editTextTicketCode);
        myCheckTicket = (Button) findViewById(R.id.btnTicketCheck);
        mySession = (Button) findViewById(R.id.btnSession);

        mySession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FULLSCANPAGE", "Scan Type "+ scan_type);
                if(scan_type.equals("ONLINE")) {
                    requestQueue.add(get_session_option("get", null));
                }

                if(scan_type.equals("OFFLINE")) {
                    get_offline_options();
                    Log.d("FULLSCANPAGE", "Scan Type "+ scan_type);
//                    String[] options = {"Offline1", "Offline2"};
//                    sessionListDialog(options);
                }

            }
        });

        myCheckTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cancel = false;
                View focusView = null;

                String ticketCode = myTicketcode.getText().toString();

                if (TextUtils.isEmpty(ticketCode)) {
                    myTicketcode.setError(getString(R.string.error_field_required));
                    focusView = myTicketcode;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    Log.v(TAG, "SCANNING STATUS +++++++++++ " + scan_type);
                    if (scan_type.equals("OFFLINE")) {

                        if(!ticketCount()){
                            scanDialog("There not tickets to scan. Please download tickets before scanning.");
                            return;
                        }
                        String[] found = getDatabaseTicketCode(ticketCode);
                        showMessageDialog("TICKET VALIDATION", found);

                    } else if (scan_type.equals("ONLINE")) {
                        final JSONObject post_dict = new JSONObject();
                        String tc = Base64.encodeToString(ticketCode.getBytes(), Base64.DEFAULT);
                        try {
                            post_dict.put("username", uUsername);
                            post_dict.put("password", uPassword);
                            post_dict.put("provider_id", provider_id);
                            post_dict.put("instancecode", instance_code);
                            post_dict.put("ticketcode", tc);
                            post_dict.put("option", session_option);
                            post_dict.put("type", "validate_ticket");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mProcessingTask = new ProcessingTask();
                        mProcessingTask.execute(String.valueOf(post_dict));
                    } else {

                    }
                }
            }
        });

        Log.d("TypeCode", "Scanning Event "+ event_name);
    }

    private void actionB(String s){
        System.out.println("FUNCTION ONLINE "+ scan_type);
        if(scan_type.equals("ONLINE"))
            netStat = "i";

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(netStat+"-"+event_name + " [" + s + "]");
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

    public void showMessageDialog(String message, String [] params) {
        DialogFragment fragment = MessageDialogFragment.newInstance("Scan Results", message, params, this);
        fragment.show(getSupportFragmentManager(), "scan_results");
    }

    public Boolean ticketCount(){
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where " +
                dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";
        Cursor cursor = db.rawQuery(selectQuery, null);
        //String[] data      = null;
        String[] columnNames = cursor.getColumnNames();
        System.out.println("COUNT BEFORE COUNT >>>>> "+ cursor.getCount());

        if (cursor.getCount() <= 0){
            return false;
        }
        return true;
    }

    private void scanDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage(message);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String[] getDatabaseTicketCode(String ticketCode){

        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();
        ticketCode = Base64.encodeToString(ticketCode.getBytes(), Base64.DEFAULT);
        Integer scan_count, admits, ticket_usage;
        String[] params = new String[]{};

        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where "+ dbHelper.TICKETCODE+" = \"" + ticketCode + "\" and " +
                dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";
        Log.v(TAG, selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            Log.v(TAG, "Ticket Found ...... id " + cursor.getString(0)  +
                    " Provider Id " + cursor.getString(1)  +
                    " Event Name " + cursor.getString(2)  +
                    " Instance Code " + cursor.getString(3)  +
                    " Ticket Code " + cursor.getString(4)  +
                    " Phone Number " + cursor.getString(5)  +
                    " Usage Limit " + cursor.getString(6)  +
                    " Bought Date " + cursor.getString(7)  +
                    " status " + cursor.getString(8)  +
                    " used " + cursor.getString(9)  +
                    " Scan Count " + cursor.getString(10)  +
                    " Admits " + cursor.getString(11)  +
                    " Scan Date " + cursor.getString(12)  +
                    " Sync Status " + cursor.getString(13)  +
                    " Sync Date " + cursor.getString(14)+
                    " Pay Class " + cursor.getString(15));

            scan_count = Integer.parseInt(cursor.getString(10)) + 1;
            admits = Integer.parseInt(cursor.getString(11));
            ticket_usage = Integer.parseInt(cursor.getString(6));
            String pc = cursor.getString(15);
            String ticket_no = cursor.getString(16);
            String name = cursor.getString(17);
            String trequest = cursor.getString(18);
            byte[] data = Base64.decode(cursor.getString(4), Base64.DEFAULT);
            String ticketCodeString = "";
            try {
                ticketCodeString = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String status = "true";
            params = new String[]{
                    "1",
                    "" + scan_count,
                    ticketCodeString,
                    pc,
                    ""+admits,
                    "",
                    ticket_no,
                    name,
                    trequest
            };
            if(ticket_usage != 0) {
                if (scan_count > ticket_usage) {
                    params = new String[]{
                            "0",
                            "" + scan_count,
                            ticketCodeString,
                            pc,
                            "" + admits,
                            "Ticket exceeds its usage count",
                            ticket_no,
                            name,
                            trequest
                    };
                    status = "false";
                }
            }
            updateScannedTicket(ticketCode, scan_count, status);
            return params;
        }else{
            //return getMemberShipDatabaseTicketCode(ticketCode);
        }
        Log.v(TAG, "Ticket Not Found ......++++");
        params = new String[]{
                "0",
                "X",
                "",
                "",
                "",
                "Ticket Not Found!",
                "",
                "",
                ""
        };
        return params;
    }

    public String[] getMemberShipDatabaseTicketCode(String ticketCode){

        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();
        Integer scan_count, admits, ticket_usage;
        String[] params = new String[]{};

        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_TICKETS + " where "+ dbHelper.TICKETCODE+" = \"" + ticketCode + "\" and " +
                dbHelper.PROVIDER_ID + " = \"" + provider_id + "\"";
        Log.v(TAG, selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            Log.v(TAG, "MemberShip Ticket Found ...... id " + cursor.getString(0)  +
                    " Provider Id " + cursor.getString(1)  +
                    " Event Name " + cursor.getString(2)  +
                    " Instance Code " + cursor.getString(3)  +
                    " Ticket Code " + cursor.getString(4)  +
                    " Phone Number " + cursor.getString(5)  +
                    " Usage Limit " + cursor.getString(6)  +
                    " Bought Date " + cursor.getString(7)  +
                    " status " + cursor.getString(8)  +
                    " used " + cursor.getString(9)  +
                    " Scan Count " + cursor.getString(10)  +
                    " Admits " + cursor.getString(11)  +
                    " Scan Date " + cursor.getString(12)  +
                    " Sync Status " + cursor.getString(13)  +
                    " Sync Date " + cursor.getString(14));

            scan_count = Integer.parseInt(cursor.getString(10)) + 1;
            admits = Integer.parseInt(cursor.getString(11));
            ticket_usage = Integer.parseInt(cursor.getString(6)) ;

            String status = "true";
            params = new String[]{"1", "" + scan_count};
            if(ticket_usage != 0) {
                if (scan_count > ticket_usage) {
                    params = new String[]{"0", "" + scan_count};
                    status = "false";
                }
            }
            updateScannedTicket(ticketCode, scan_count, status);
            return params;
        }
        Log.v(TAG, "Ticket Not Found Membership too......++++");
        params = new String[]{"0", "X"};
        return params;
    }

    public void updateScannedTicket(String ticketCode, Integer scan_count, String status){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(dbHelper.STATUS, "scanned");
        cv.put(dbHelper.SYNC_STATUS, 0);
        cv.put(dbHelper.SCANCOUNT, scan_count);
        cv.put(dbHelper.USED, 1);
        cv.put(dbHelper.SCANDATE, currentDateTimeString);

        Log.d("UPDATING TICKETCODE ", ticketCode);
        db.update(dbHelper.TABLE_TICKETS, cv, dbHelper.TICKETCODE + " = \"" + ticketCode +"\" and "+ dbHelper.INSTANCECODE +" = \"" + instance_code +"\"",null);

        Log.d("SCANTICKET INSERT LOG ", ticketCode);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TICKETCODE, ticketCode);
        values.put(DatabaseHelper.INSTANCECODE, instance_code);
        values.put(DatabaseHelper.STATUS, status);
        values.put(DatabaseHelper.SCANCOUNT, scan_count);
        values.put(DatabaseHelper.SYNCSTATUS, 0);
        values.put(dbHelper.SESSIONOPTION, session_option);
        values.put(DatabaseHelper.SCANDATE, currentDateTimeString);
        db.insert(DatabaseHelper.TABLE_SCANTICKET, null, values);
        db.close();
    }

    public void processResponse (String response){
        String ticketcode;
        String msisdn;
        String[] vals;

        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();
            ArrayList<Item> items = new ArrayList<Item>();


            //db.execSQL("delete from "+ DatabaseHelper.TABLE_TICKETS);

            if(jsonStatus.equals("GOOD")){
                //finish();
                //showProgress(false);
                String jsonData = obj.getString("data").toString();
                String jsonDataStatus = obj.getString("status").toString();

                JSONObject jsonDObj = new JSONObject(jsonData);

                if(jsonDObj.getString("status").equals("true")){
                    System.out.println("TICKET STATUS >>><<<<< " + jsonDObj.getString("status"));
                    vals = new String[]{
                            "1",
                            jsonDObj.getString("count").toString(),
                            jsonDObj.getString("ticketcode"),
                            jsonDObj.getString("category"),
                            jsonDObj.getString("admits"),
                            jsonDObj.getString("message"),
                            jsonDObj.getString("ticketno"),
                            jsonDObj.getString("name"),
                            jsonDObj.getString("reqt"),
                    };
                }
                else{
                    System.out.println("TICKET STATUS >>><<<<< " + jsonDObj.getString("status"));
                    vals = new String[]{
                            "0",
                            jsonDObj.getString("count").toString(),
                            jsonDObj.getString("ticketcode"),
                            jsonDObj.getString("category"),
                            jsonDObj.getString("admits"),
                            jsonDObj.getString("message"),
                            jsonDObj.getString("ticketno"),
                            jsonDObj.getString("name"),
                            jsonDObj.getString("reqt"),
                    };
                }

                //String data = obj.getString("data").toString();

            }else{
                //showProgress(false);
                System.out.println("Bad response >>> " + jsonStatus + " " + response);
                vals = new String[]{
                        "0",
                        "0",
                        "",
                        "",
                        "",
                        "System Error",
                        "",
                        "",
                        ""
                };
                //Toast.makeText(this, "Error " + obj.getString("data").toString(), Toast.LENGTH_LONG);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + t + "\"");
            Config.noInternetDialog(this);
            vals = new String[]{
                    "0",
                    "0",
                    "",
                    "",
                    "",
                    "Error",
                    "",
                    "",
                    ""
            };
        }
        showMessageDialog("", vals);
        //showProgress(false);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    private JsonObjectRequest get_session_option(String process, String opt) {
        //Initializing ProgressBar
//        pbar.setMessage("Please Wait...");
//        pbar.show();
        pd.setMessage("Please Wait...");
        pd.show();


        JSONObject post_dict = new JSONObject();

        try {
            post_dict.put("username" , uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("provider_id", provider_id);
            post_dict.put("instance_code", instance_code);
            if(process == "put"){
                post_dict.put("process", "put");
                post_dict.put("option", opt);
            }else {
                post_dict.put("process", "get");
            }
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
                                    sessionListDialog(session_options.split(","));
                                }

                                pd.dismiss();
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
                Toast.makeText(TypeCodeActivity.this, "No More Items Available", Toast.LENGTH_SHORT).show();
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

    private void get_offline_options(){

        String session_v = "None";
        dbHelper = new DatabaseHelper(getBaseContext());
        db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_EVENTTABLE + " where " + dbHelper.PROVIDER_ID + " = \"" + provider_id +
                "\" and " + dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";

        Cursor cursor = db.rawQuery(selectQuery, null);
        List<String> data = new ArrayList<String>();
        System.out.println("OFFLINE TICKET COUNT >>>>> "+ cursor.getCount() + " "+provider_id);

        if (cursor.moveToFirst()) {
            do {
                System.out.print("OFFLINE DB SELECT" + cursor.getString(4));
                session_v = cursor.getString(4);
                data.add(session_v);
            } while (cursor.moveToNext());
        }

        sessionListDialog(session_v.split(","));
    }

    private void sessionListDialog(String[] options){
        AlertDialog.Builder b = new AlertDialog.Builder(TypeCodeActivity.this);
        b.setTitle("Session");
        ArrayList<String> list = new ArrayList<>();
        list.add("Add New");
        Collections.addAll(list, options);
        String[] mStringArray = new String[list.size()];
        mStringArray = list.toArray(mStringArray);

        for(int i = 0; i < mStringArray.length ; i++){
            Log.d("string is",(String)mStringArray[i]);
        }

        final String[] finalMStringArray = mStringArray;
        b.setItems(mStringArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                System.out.print(">>>>>>>>>" + finalMStringArray[which] +"<<<<<<<<<<");
                String s = finalMStringArray[which];
                if (s =="Add New"){
                    addSessionDialog();
                }else{
                    SaveSharedPreference.setSessionOption(TypeCodeActivity.this, s);
                    actionB(s);
                    session_option = s;
                }
//
            }

        });

        b.show();
    }

    private void addSessionDialog(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(TypeCodeActivity.this);
        View promptsView = li.inflate(R.layout.session_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                TypeCodeActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogSession);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(scan_type.equals("OFFLINE")) {
                                    add_option_offline(userInput.getText().toString());
                                    get_offline_options();
                                }
                                if(scan_type.equals("ONLINE")) {
                                    requestQueue.add(get_session_option("put", userInput.getText().toString()));
                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void add_option_offline(String option){
        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        try {
            //Thread.sleep(cnt);
            String selectQuery = "SELECT  * FROM " + dbHelper.TABLE_EVENTTABLE + " where " + dbHelper.PROVIDER_ID + " = \"" + provider_id +
                    "\" and " + dbHelper.INSTANCECODE + " = \"" + instance_code + "\"";

            Cursor cursor = db.rawQuery(selectQuery, null);
            List<String> data = new ArrayList<String>();
            String session_v = "";
            if (cursor.moveToFirst()) {
                do {
                    session_v = cursor.getString(4);
                } while (cursor.moveToNext());
            }
            String sessionOption = session_v + "," + option;
            System.out.print("OFFLINE DB ADD/UPDATE" + sessionOption);
            if (!cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.PROVIDER_ID, provider_id);
                values.put(DatabaseHelper.INSTANCECODE, instance_code);
                values.put(DatabaseHelper.EVENT_NAME, event_name);
                values.put(DatabaseHelper.SESSIONOPTION, sessionOption);
                values.put(DatabaseHelper.CREATEDATE, currentDateTimeString);
                db.insert(DatabaseHelper.TABLE_EVENTTABLE, null, values);
            } else {
                //System.out.println("UPDATE DATA" + ticketObject.toString());
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.SESSIONOPTION, sessionOption);
                cv.put(DatabaseHelper.CREATEDATE, currentDateTimeString);
                Log.d("UPDATE EVENTTABLE", "OPTION "+option);
                db.update(dbHelper.TABLE_EVENTTABLE, cv, dbHelper.PROVIDER_ID + " = \"" + provider_id  + "\" and " + dbHelper.INSTANCECODE + " = \"" + instance_code + "\"", null);
            }
            cursor.close();
        }catch (Exception j){
            Log.e(TAG, "Error in Loop JSON: \"" + j + "\"");
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
}
