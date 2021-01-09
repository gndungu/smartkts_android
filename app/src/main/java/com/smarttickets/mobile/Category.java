package com.smarttickets.mobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.ArrayList;

public class Category extends AppCompatActivity {

    private GetProviderIdTask mAuthTask = null;
    private String business_id;
    private String business;
    private static final String TAG = "Category";
    private View mProgressView;
    private View mView;
    private String uUsername;
    private String uPassword;
    String system_url = Config.URL;
    ListView listview;
    String provider_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        if(SaveSharedPreference.getUserLoggedInStatus(this) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(this);
            uPassword = SaveSharedPreference.getLoggedInUserPassword(this);
        }

        business_id = getIntent().getStringExtra("BIZ_ID");
        business = getIntent().getStringExtra("BIZ");

        JSONObject post_dict = new JSONObject();

        try {
            post_dict.put("username" , uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("businessid", business_id);
            post_dict.put("type", "providerid");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        listview = (ListView) findViewById(R.id.lvCategory);

        mView = findViewById(R.id.lvCategory);
        mProgressView = findViewById(R.id.login_progress);


        showProgress(true);
        mAuthTask = new GetProviderIdTask();
        mAuthTask.execute(String.valueOf(post_dict));

        // ListView Item Click Listener
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String  itemValue = "Data";//(String) listview.getItemAtPosition(position);

                final TextView catdisplay = (TextView) view.findViewById(R.id.labelTextView);
                final TextView selected = (TextView) view.findViewById(R.id.valueTextView);
                //System.out.println(" Item "+selected.getText().toString());
                final String item = selected.getText().toString();
                final String cat = catdisplay.getText().toString();
                Intent view_intent = new Intent(Category.this, ActivityList.class);

                view_intent.putExtra("PROVIDERID", item);
                view_intent.putExtra("BIZ", business);
                view_intent.putExtra("CATEGORY", cat);
                startActivity(view_intent);
                // Show Alert
                //Toast.makeText(getApplicationContext(),
                //"Position :"+itemPosition+"  ListItem : " + item , Toast.LENGTH_LONG).show();

            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(getIntent().getStringExtra("BIZ"));
    }

    private ArrayList<Item> generateData(){

        ArrayList<Item> items = new ArrayList<Item>();
        try {
            JSONObject jsonObjRecv = new JSONObject("{data: [{\"category_id\": " + provider_id + ", \"category\": \"Event\"}, " +
                    "{\"category_id\": " + provider_id + ", \"category\": \"Lifestyle\"}," +
                    "{\"category_id\": " + provider_id + ", \"category\": \"Travel\"}," +
                    "{\"category_id\": " + provider_id + ", \"category\": \"Permit\"}]}");

            JSONArray jsonArr = jsonObjRecv.getJSONArray("data");
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject menuObject = jsonArr.getJSONObject(i);
                items.add(new Item(menuObject.getString("category"), menuObject.getString("category_id")));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Processs the Response
     */
    public void processResponse (String response){

        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();

            String category;
            ArrayList<Item> items = new ArrayList<Item>();

            if(jsonStatus.equals("GOOD")){
                //finish();
               //showProgress(false);
                JSONArray jsonArr = obj.getJSONArray("data");
                //String data = obj.getString("data").toString();
                for(int i = 0; i < jsonArr.length(); i++){
                    JSONObject menuObject = jsonArr.getJSONObject(i);
                    //provider_id = menuObject.getString("provider_id");
                    provider_id = menuObject.getString("provider_id");
                    category = menuObject.getString("category");
                    items.add(new Item(category.toUpperCase(), provider_id));
                }
                //MyAdapter adapter = new MyAdapter(this, generateData());
                MyAdapter adapter = new MyAdapter(this, items);
                listview.setAdapter(adapter);

            }else{
                //showProgress(false);
                System.out.println("Bad response >>> " + jsonStatus + " " + response);
                Toast.makeText(this, "Error " + obj.getString("data").toString(), Toast.LENGTH_LONG);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + t + "\"");
        }
        showProgress(false);

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
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

    public class GetProviderIdTask extends AsyncTask<String, String, String> {

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
            mAuthTask = null;
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
            mAuthTask = null;
            //showProgress(false);
        }
    }
}
