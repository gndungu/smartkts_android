package com.smarttickets.mobile;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
public class MTickets extends Fragment {
    private String data;
    private static final String TAG = "MyTickets";
    private MTickets.GetTicketTask mTicketTask = null;
    private String uUsername;
    private String uPassword;
    private View mProgressView;
    private View mView;
    static MyTicketAdapter adapter;
    ListView listview;

    ArrayList<TicketItem> items;
    JSONObject post_dict;

    TextView spipeAlert;
    private SwipeRefreshLayout swipeContainer;

    public MTickets() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mtickets, container, false);

        uUsername = SaveSharedPreference.getLoggedInUserUserName(getActivity());
        uPassword = SaveSharedPreference.getLoggedInUserPassword(getActivity());

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerTickets);
        spipeAlert = (TextView) rootView.findViewById(R.id.txtSwipeAlert);

        post_dict = new JSONObject();

        try {
            post_dict.put("username", uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("type", "get_tickets");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(getActivity().getIntent().getStringExtra("DATA") == null) {

            mView = rootView.findViewById(R.id.lvmyTickets);
            mProgressView = rootView.findViewById(R.id.progressBarAcc);

            showProgress(true);
            swipeContainer.setRefreshing(true);
            mTicketTask = new MTickets.GetTicketTask();
            mTicketTask.execute(String.valueOf(post_dict));
        }



        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                items.clear();
                showProgress(true);
                swipeContainer.setRefreshing(true);
                mTicketTask = new MTickets.GetTicketTask();
                mTicketTask.execute(String.valueOf(post_dict));
                swipeContainer.setRefreshing(false);
            }
        });

        adapter = new MyTicketAdapter(getActivity(), generateData());
        listview = (ListView) rootView.findViewById(R.id.lvmyTickets);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tc = (TextView) view.findViewById(R.id.txtTicketCode);
                Intent i = new Intent(getActivity(), TicketDetails.class);
                i.putExtra("TICKETCODE", tc.getText().toString());
                startActivity(i);
            }
        });
        listview.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onPause(){
        super.onPause();
        mTicketTask = null;
        showProgress(false);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mTicketTask = null;
        showProgress(false);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onStop(){
        super.onStop();
        mTicketTask = null;
        showProgress(false);
        swipeContainer.setRefreshing(false);
    }

    private ArrayList<TicketItem> generateData(){
        spipeAlert.setVisibility(View.GONE);
        data = getActivity().getIntent().getStringExtra("DATA");
        items = new ArrayList<TicketItem>();
        try {
            JSONObject jsonObjRecv = new JSONObject("{data:" + data +"}");
            JSONArray jsonArr = jsonObjRecv.getJSONArray("data");
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject menuObject = jsonArr.getJSONObject(i);
                String currency = "";
                if(menuObject.has("currency")){
                    currency = menuObject.getString("currency");
                }
                items.add(new TicketItem(
                        menuObject.getString("event_name"), menuObject.getString("price") + " " + currency,
                        menuObject.getString("category"),
                        menuObject.getString("date_received"),
                        menuObject.getString("payclass"),
                        menuObject.getString("ticket_code")
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
            adapter = new MyTicketAdapter(getActivity(), generateData());
            listview.setAdapter(adapter);
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");
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

    //This class is an example of bad coding practice, but i dont have time soo.....zerox
    public class GetTicketTask extends AsyncTask<String, String, String> {

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
            mTicketTask = null;
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
            mTicketTask = null;
            showProgress(false);
            swipeContainer.setRefreshing(false);
        }
    }

}
