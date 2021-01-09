package com.smarttickets.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.smarttickets.mobile.offline.EventActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DashboardActivity extends AppCompatActivity {

    ConnectionDetector cd;

    Boolean isInternetPresent = false;

    RelativeLayout logOut;
    RelativeLayout profile;
    RelativeLayout discover;
    RelativeLayout rlEvent;
    Float currentVersion;
    String logout_message;
    private Util util;
    GetVersionCode mVersionCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        util = new Util(this, this);

        try {
            String currentV = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            currentVersion = Float.parseFloat(currentV);
            mVersionCode = new GetVersionCode();
            mVersionCode.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isInternetPresent == false) {
            Toast.makeText(this, "You do not have internet Access", Toast.LENGTH_LONG).show();
            logout_message = "If you logout while offline you will be required to be online to login again.";
        }

        logOut = (RelativeLayout) findViewById(R.id.rlLogout);
        rlEvent = (RelativeLayout) findViewById(R.id.rlEvent);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.myLogoutDialog(logout_message);
            }
        });

        profile = (RelativeLayout) findViewById(R.id.rlProfile);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent == false) {

                    Intent i = new Intent(DashboardActivity.this, OffLineList.class);
                    startActivity(i);

                    /**if(!event_instance.equals("") && !provider_id.equals("") && !scan_type.equals("") ){
                     if(scan_type.equals("OFFLINE")) {
                     Intent i = new Intent(HomeActivity.this, ScanHomeActivity.class);
                     i.putExtra("INSTANCE_CODE", event_instance);
                     i.putExtra("EVENT_NAME", event);
                     i.putExtra("PROVIDER_ID", provider_id);
                     i.putExtra("SCAN_TYPE", "OFFLINE");
                     startActivity(i);
                     finish();
                     }

                     } else {
                     AlertDialog.Builder builder = new AlertDialog.Builder(this);
                     builder.setTitle("Warning");
                     builder.setMessage("You do not have any saved event. You need internet to scan events.");
                     builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    }
                    });
                     AlertDialog dialog = builder.create();
                     dialog.show();
                     return;
                     }**/
                }else{
                    Intent it = new Intent(DashboardActivity.this, HomeActivity.class);
                    startActivity(it);
                }
            }
        });

        discover = (RelativeLayout) findViewById(R.id.rlDiscover);
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(DashboardActivity.this, DiscoverPage.class);
                startActivity(i);
            }
        });

        rlEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, EventActivity.class));
            }
        });
    }

    private void myDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent it = new Intent(DashboardActivity.this, LoginTabActivity.class);
                SaveSharedPreference.clearLoggedInUserAddress(DashboardActivity.this);
                startActivity(it);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
            util.myLogoutDialog(logout_message);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class GetVersionCode extends AsyncTask<Void, Document, Document> {

        private Document document;

        @Override
        protected Document doInBackground(Void... voids) {

            String newVersion = null;

//            try {
//
//                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + DashboardActivity.this.getPackageName() + "&hl=en").timeout(30000)
//
//                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
//
//
//                        .referrer("http://www.google.com")
//
//                        .get()
//                        .select("div:containsOwn(Current Version) + div")
//                        .first()
//                        .text();
//
//                return newVersion;
//
//            } catch (Exception e) {
//
//                e.printStackTrace();
//                Log.e("DASHBOARD", "Could not parse malformed JSON: \"" + e + "\"");
//                return newVersion;
//
//            }

            try {
                document = Jsoup.connect("https://play.google.com/store/apps/details?id="+DashboardActivity.this.getPackageName() +"&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                        .get();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return document;

        }


        @Override
        protected void onPostExecute(Document d) {//String onlineVersion) {

            super.onPostExecute(d);//onlineVersion);

//            if (onlineVersion != null && !onlineVersion.isEmpty()) {
//
//                if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
//
//                    //show dialog
//
//                }
//
//            }
//
//            Log.d("update", "Current version " + currentVersion + "playstore version " + onlineVersion);
            try{
                Elements es = d.body().getElementsByClass("xyOfqd").select(".hAyfc");
                String newVersion = es.get(3).child(1).child(0).child(0).ownText();

                if (newVersion != null && !newVersion.isEmpty()) {
                    String onlineVersion = "" + newVersion.charAt(0);

                    int retval = Float.compare(Float.valueOf(currentVersion), Float.valueOf(onlineVersion));
                    Log.i("DASHBOARD", " is new version === " + retval);
                    if (retval < 0) {
                        if (currentVersion < 2) {
                            Log.i("DASHBOARD", Float.valueOf(currentVersion) + "Online === " + Float.valueOf(onlineVersion));
                            AlertDialog alertDialog = new AlertDialog.Builder(DashboardActivity.this).create();
                            alertDialog.setTitle("Application Update");
                            alertDialog.setMessage("This Application is outdated. Please Delete and install the new version.");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object

                                            Intent intent = new Intent(Intent.ACTION_DELETE);
                                            //Enter app package name that app you wan to install
                                            intent.setData(Uri.parse("package:" + getPackageName()));
                                            startActivity(intent);
                                        }
                                    });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();

                        } else {
                            Log.i("DASHBOARD", Float.valueOf(currentVersion) + " newVersion=== " + Float.valueOf(onlineVersion));
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashboardActivity.this);
                            alertDialog.setTitle("Application Update");
                            alertDialog.setMessage("This Application is outdated. Please update now.");
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                        }
                                    });
                            alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = alertDialog.create();
                            dialog.show();
                        }
                    }
                }
                Log.i("DASHBOARD", currentVersion + " newVersion=== "+newVersion.charAt(0));
            } catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }

}
