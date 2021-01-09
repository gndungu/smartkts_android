package com.smarttickets.mobile;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class TicketDetails extends AppCompatActivity {

    TextView myEvent;
    TextView myCategory;
    TextView myticketCode;
    TextView myReceiveDate;
    TextView myStatus;
    TextView myScanDate;
    TextView myAmount;
    TextView myPayclass;
    TextView myDownload;
    private LinearLayout llLeft, llimage, llTicketDetails;
    ImageView imageViewTicket;
//    TextView myOpenImage;
    private View mProgressView;
    private View mView;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 112;

    JSONObject post_dict;
    String system_url = Config.SURL;

    private static final String TAG = "MyTicketsDetails";
    private TicketDetails.GetTicketTask mTicketTask = null;


    private String uUsername;
    private String uPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        setTitle("Ticket Details");

        myEvent = (TextView) findViewById(R.id.txtmtEvent);
        myCategory = (TextView) findViewById(R.id.txtmtCategory);
        myticketCode = (TextView) findViewById(R.id.txtmtTicketCode);
        myReceiveDate = (TextView) findViewById(R.id.txtmtReceiveDate);
        myStatus = (TextView) findViewById(R.id.txtmtStatus);
        myScanDate = (TextView) findViewById(R.id.txtmtScanDate);
        myAmount = (TextView) findViewById(R.id.txtmtAmount);
        myPayclass = (TextView) findViewById(R.id.txtmtPayClass);
        myDownload = (TextView) findViewById(R.id.txtmtDownload);
        llTicketDetails = findViewById(R.id.llTicketDetails);
        imageViewTicket = findViewById(R.id.imageViewTicket);
//        myOpenImage = (TextView) findViewById(R.id.txtmtOpenTicket);

        mView = findViewById(R.id.scrollViewTD);
        mProgressView = findViewById(R.id.progressBarAcctd);

        uUsername = SaveSharedPreference.getLoggedInUserUserName(this);
        uPassword = SaveSharedPreference.getLoggedInUserPassword(this);

        final String ticketcode = getIntent().getStringExtra("TICKETCODE");

        final String url = system_url + "events/q/"+ticketcode+"/download/";
        Log.v(TAG, "Url: " + url);



        if (ContextCompat.checkSelfPermission(TicketDetails.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(TicketDetails.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Storage Permission");
                builder.setMessage("you need to enable the camera permissions for this application to be able to use the camera.");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", TicketDetails.this.getPackageName(), null);
                        intent.setData(uri);
                        TicketDetails.this.startActivity(intent);
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(TicketDetails.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(TicketDetails.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(TicketDetails.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Storage Permission");
                builder.setMessage("you need to enable the camera permissions for this application to be able to use the camera.");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", TicketDetails.this.getPackageName(), null);
                        intent.setData(uri);
                        TicketDetails.this.startActivity(intent);
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(TicketDetails.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        String downloadFileName = "Ticket-"+ticketcode+".jpg";
        File apkStorage = new File(
                Environment.getExternalStorageDirectory() + "/"
                        + "smarttickets");
        Log.v(TAG, "Folder :- "+apkStorage);
        if (apkStorage.exists()) {
            File outputFile = new File(apkStorage, downloadFileName);
            if (outputFile.exists()) {
                llTicketDetails.setVisibility(View.GONE);
                myDownload.setVisibility(View.GONE);
                imageViewTicket.setVisibility(View.VISIBLE);

                Bitmap myBitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                imageViewTicket.setImageBitmap(myBitmap);
                //open_file(outputFile.toString());
//                imageViewTicket.setImageBitmap();
//                ImageView imageView = new ImageView(this);
//                // Create layout parameters for ImageView
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                lp.weight = 1;
//                lp.height = 500;
//                // Add layout parameters to ImageView
//                imageView.setLayoutParams(lp);

                // Finally, add the ImageView to layout

            }
        }

        myDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_file_action(ticketcode, url);
            }
        });

        imageViewTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_file_action(ticketcode, url);
            }
        });





//        myOpenImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String downloadFileName = "";
//                File apkStorage = null;
//                File outputFile = null;
//                downloadFileName = "Ticket-"+ticketcode+".jpg";
//                if (new CheckForSDCard().isSDCardPresent()) {
//                    apkStorage = new File(
//                            Environment.getExternalStorageDirectory() + "/"
//                                    + "smarttickets");
//                    Log.v(TAG, "Folder :- "+apkStorage);
//                    if (apkStorage.exists()) {
//                        outputFile = new File(apkStorage, downloadFileName);
//                        if (outputFile.exists()) {
//                            open_file(outputFile.toString());
//                        } else
//                            Toast.makeText(TicketDetails.this, "Oops!! File not Found.", Toast.LENGTH_SHORT).show();
//                    } else
//                        Toast.makeText(TicketDetails.this, "Oops!! Folder not Found.", Toast.LENGTH_SHORT).show();
//
//                } else
//                    Toast.makeText(TicketDetails.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
//
//            }
//        });

        Log.v(TAG, "Ticket Code" + ticketcode);
        post_dict = new JSONObject();

        try {
            post_dict.put("username", uUsername);
            post_dict.put("password", uPassword);
            post_dict.put("ticketcode", ticketcode);
            post_dict.put("type", "get_tickets");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        showProgress(true);
        mTicketTask = new TicketDetails.GetTicketTask();
        mTicketTask.execute(String.valueOf(post_dict));

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
        return super.onOptionsItemSelected(item);
    }

    private void open_file_action(String ticketcode, String url){
        String downloadFileName = "";
        File apkStorage = null;
        File outputFile = null;
        downloadFileName = "Ticket-"+ticketcode+".jpg";
        if (new CheckForSDCard().isSDCardPresent()) {
            apkStorage = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + "smarttickets");
            Log.v(TAG, "Folder :- "+apkStorage);
            if (apkStorage.exists()) {
                outputFile = new File(apkStorage, downloadFileName);
                if (outputFile.exists()) {
                    open_file(outputFile.toString());

                } else
                    new DownloadTask(TicketDetails.this, url, ticketcode);
            } else
                new DownloadTask(TicketDetails.this, url, ticketcode);

        } else
            Toast.makeText(TicketDetails.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
    }

    private void open_file(String filePath){
        File file = new File(filePath);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "image/jpeg";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);
        intent.setDataAndType(data, type);

//        startActivity(intent);

        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri apkURI = FileProvider.getUriForFile(
                this,
                this.getApplicationContext()
                        .getPackageName() + ".provider", file);
        install.setDataAndType(apkURI, type);
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(install);


    }

    private void generateData(String data){
        try {
            JSONObject jsonObjRecv = new JSONObject("{data:" + data +"}");
            JSONArray jsonArr = jsonObjRecv.getJSONArray("data");
            DecimalFormat formatter = new DecimalFormat("#,###,###");
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject menuObject = jsonArr.getJSONObject(i);
                myEvent.setText(menuObject.getString("event_name"));
                myAmount.setText(formatter.format(Double.parseDouble(menuObject.getString("price"))) + " " + menuObject.getString("currency"));
                myCategory.setText(menuObject.getString("payclass"));
                myReceiveDate.setText(menuObject.getString("date_received"));
                myPayclass.setText(menuObject.getString("payclass"));
                myticketCode.setText(menuObject.getString("ticket_code"));
                Log.v(TAG, "Items Status: " + menuObject.getString("used").toString());
                if( menuObject.getString("used").toString().equals("0")) {
                    myStatus.setBackgroundColor(Color.RED);
                    myStatus.setText("Not Used");
                }else{
                    myStatus.setBackgroundColor(Color.GREEN);
                    myStatus.setText("Used");
                }
            }
            Log.v(TAG, "Items ArrayList: " + data);
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    public void loadHome(String response){
        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();

            if(jsonStatus.equals("GOOD")){
                //finish();
                String data = obj.getString("data").toString();
                generateData(data);
                showProgress(false);
            }else{

            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");
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
        }
    }
}
