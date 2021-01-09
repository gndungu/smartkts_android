package com.smarttickets.mobile;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class Notification extends Fragment {

    private String data;
    private static final String TAG = "Notification";
    private GetNotificationTask mNotificationTask = null;
    private String uUsername;
    private String uPassword;
    private View mProgressView;
    private View mView;
    static NotificarionsAdapter adapter;
    ListView listview;

    ArrayList<NotificationItem> items;
    JSONObject post_dict;

    TextView spipeAlert;
    private SwipeRefreshLayout swipeContainer;


    public Notification() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_notification, container, false);
        uUsername = SaveSharedPreference.getLoggedInUserUserName(getActivity());
        uPassword = SaveSharedPreference.getLoggedInUserPassword(getActivity());

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerNotification);
        spipeAlert = (TextView) rootView.findViewById(R.id.txtSwipeAlertNotification);

        post_dict = new JSONObject();

        try {
            post_dict.put("username", uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("type", "get_notification");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mView = rootView.findViewById(R.id.lvmyNotif);
        mProgressView = rootView.findViewById(R.id.progressBarNotification);

        mView = rootView.findViewById(R.id.lvmyNotif);
        mProgressView = rootView.findViewById(R.id.progressBarNotification);

        showProgress(true);
        swipeContainer.setRefreshing(true);
        mNotificationTask = new GetNotificationTask();
        mNotificationTask.execute(String.valueOf(post_dict));

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                items.clear();
                showProgress(true);
                swipeContainer.setRefreshing(true);
                mNotificationTask = new GetNotificationTask();
                mNotificationTask.execute(String.valueOf(post_dict));
                swipeContainer.setRefreshing(false);
            }
        });

        adapter = new NotificarionsAdapter(getActivity(), generateData());
        listview = (ListView) rootView.findViewById(R.id.lvmyNotif);
        listview.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onPause(){
        super.onPause();
        mNotificationTask = null;
        showProgress(false);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mNotificationTask = null;
        showProgress(false);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onStop(){
        super.onStop();
        mNotificationTask = null;
        showProgress(false);
        swipeContainer.setRefreshing(false);
    }


    private ArrayList<NotificationItem> generateData(){
        spipeAlert.setVisibility(View.GONE);
        data = getActivity().getIntent().getStringExtra("DATA");
        items = new ArrayList<NotificationItem>();
        try {
            JSONObject jsonObjRecv = new JSONObject("{data:" + data +"}");
            JSONArray jsonArr = jsonObjRecv.getJSONArray("data");
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject menuObject = jsonArr.getJSONObject(i);
                items.add(new NotificationItem(
                        menuObject.getString("notification"),
                        menuObject.getString("notification_date"),
                        menuObject.getString("locale")
                ));
            }
            Log.v(TAG, "Items ArrayList: " + items);
        } catch (JSONException e) {
            e.printStackTrace();
            spipeAlert.setVisibility(View.VISIBLE);
        }
        return items;
    }

    public void loadHome(String response){
        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();

            if(jsonStatus.equals("GOOD")){
                //finish();
                getActivity().getIntent().putExtra("DATA", obj.getString("data").toString());
                showProgress(false);
            }else{
                getActivity().getIntent().putExtra("DATA", obj.getString("data").toString());
                showProgress(false);
            }
            adapter = new NotificarionsAdapter(getActivity(), generateData());
            listview.setAdapter(adapter);
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");
            Config.noInternetDialog(getActivity());
        }

        showProgress(false);
        swipeContainer.setRefreshing(false);
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


    //This class is an example of bad coding practice, but i dont have time soo.....zerox
    public class GetNotificationTask extends AsyncTask<String, String, String> {

        /*** private final String mEmail;
         private final String mPassword;

         private final String url = Config.URL;


         UserLoginTask(String email, String password) {
         mEmail = email;
         mPassword = password;
         }**/

        String system_url = Config.URL;

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
                    buffer.append(inputLine + "\n");
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
            mNotificationTask = null;
            System.out.println("Haaaaleluya>>> "+ response);
            loadHome(response);
            //getActivity().getIntent().putExtra ("DATA", "");
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
            mNotificationTask = null;
            showProgress(false);
            swipeContainer.setRefreshing(false);
        }
    }

}
