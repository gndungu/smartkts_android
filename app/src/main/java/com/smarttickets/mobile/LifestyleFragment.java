package com.smarttickets.mobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class LifestyleFragment extends Fragment {

    private GetActivityListTask mActivityTask = null;
    String instance_code;
    String event_name;
    String provider_id;
    String business;
    String category;
    ListView listview;
    private View mProgressView;
    private View mView;
    private String uUsername;
    private String uPassword;
    public static final String TAG = "ActivityList";
    String system_url = Config.URL;

    static MyAdapter adapter;

    public LifestyleFragment() {
        // Required empty public constructor
    }


    public static LifestyleFragment newInstance(String param1, String param2) {
        LifestyleFragment fragment = new LifestyleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vi = inflater.inflate(R.layout.fragment_lifestyle, container, false);

        if(SaveSharedPreference.getUserLoggedInStatus(getActivity()) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(getActivity());
            uPassword = SaveSharedPreference.getLoggedInUserPassword(getActivity());
        }

        provider_id = getActivity().getIntent().getStringExtra("PROVIDERID");
        business =  getActivity().getIntent().getStringExtra("BIZ");
        category =  "lifestyle";

        listview = (ListView) vi.findViewById(R.id.lvActivity);
        SaveSharedPreference.setLoggedInUserProviderId(getActivity(), provider_id);

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
                Intent view_intent = new Intent(getActivity(), LoadEventActivity.class);

                view_intent.putExtra("INSTANCE_CODE", item);
                view_intent.putExtra("EVENT_NAME", cat);
                view_intent.putExtra("PROVIDER_ID", provider_id);

                startActivity(view_intent);
                // Show Alert
                //Toast.makeText(getApplicationContext(),
                //"Position :"+itemPosition+"  ListItem : " + item , Toast.LENGTH_LONG).show();

            }

        });

        JSONObject post_dict = new JSONObject();

        try {
            post_dict.put("username" , uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("provider_id", provider_id);
            post_dict.put("category", category);
            post_dict.put("type", "activity");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mView = vi.findViewById(R.id.lvActivity);
        mProgressView = vi.findViewById(R.id.login_progress);


        showProgress(true);
        mActivityTask = new GetActivityListTask();
        mActivityTask.execute(String.valueOf(post_dict));
        return vi;
    }

    @Override
    public void onPause(){
        super.onPause();
        mActivityTask = null;
        showProgress(false);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mActivityTask = null;
        showProgress(false);
    }

    @Override
    public void onStop(){
        super.onStop();
        mActivityTask = null;
        showProgress(false);
    }

    /**
     * Processs the Response
     */
    public void processResponse (String response){

        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, "Processing Str - Json...." + obj.toString());
            String jsonStatus = obj.getString("status").toString();
            ArrayList<Item> items = new ArrayList<Item>();

            if(jsonStatus.equals("GOOD")){
                //finish();
                //showProgress(false);
                JSONArray jsonArr = obj.getJSONArray("data");
                Log.d(TAG, "Processing Str - Json...." + jsonArr.length());
                //String data = obj.getString("data").toString();
                for(int i = 0; i < jsonArr.length(); i++){
                    JSONObject menuObject = jsonArr.getJSONObject(i);
                    instance_code = menuObject.getString("instancecode");
                    event_name = menuObject.getString("name");

                    items.add(new Item(event_name, instance_code));
                }
                adapter = new MyAdapter(getActivity(), items);
                listview.setAdapter(adapter);

            }else{
                //showProgress(false);
                System.out.println("Bad response >>> " + jsonStatus + " " + response);
                Toast.makeText(getActivity(), "Error " + obj.getString("data").toString(), Toast.LENGTH_LONG);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e(TAG, "Could not parse malformed JSON: \"" + t + "\"");
            Config.noInternetDialog(getActivity());
        }
        showProgress(false);

    }

    public static void loadData(String newText)

    {
        // where new text is the searched keyword .
        adapter.getFilter().filter(newText); // This is a custom adapter.So adapter needed to implement filterable interface.
        //searchListView.setAdapter(adapter);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        try {
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
        } catch (Exception e){
        e.printStackTrace();
    }
    }

    public class GetActivityListTask extends AsyncTask<String, String, String> {

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
            mActivityTask = null;
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
            mActivityTask = null;
            //showProgress(false);
        }
    }
}
