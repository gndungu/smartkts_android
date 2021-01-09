package com.smarttickets.mobile;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DiscoverPage extends AppCompatActivity {

    WebView wvDiscover;
    ProgressBar pbar;
    ConnectionDetector cd;
    Boolean isInternetPresent = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_page);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Discover");

        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();

        wvDiscover = (WebView) findViewById(R.id.wvDiscover);
        pbar = (ProgressBar) findViewById(R.id.progressBar1);

        WebSettings webSettings = wvDiscover.getSettings();
        wvDiscover.setWebViewClient(new MyWebViewClient());

       loadSite();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_discovery, menu);
        return true;
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
            case R.id.disc_refresh:
                loadSite();
            default:
                // return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSite(){
        if (isInternetPresent == false) {
            Toast.makeText(this, "You do not have internet Access", Toast.LENGTH_LONG).show();
        }else {
//            String url = "https://smartkts.com/?api=1";
            String url = "https://smartkts.com";
//            String url = "https://www.google.com/";
            wvDiscover.getSettings().setJavaScriptEnabled(true);
//            wvDiscover.getSettings().setBuiltInZoomControls(true);
//            wvDiscover.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
//            wvDiscover.setBackgroundColor(Color.TRANSPARENT);
//            wvDiscover.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_INSET);

            wvDiscover.getSettings().setLoadWithOverviewMode(true);
            wvDiscover.getSettings().setUseWideViewPort(true);
            wvDiscover.getSettings().setJavaScriptEnabled(true);
            wvDiscover.getSettings().setAppCacheEnabled(false);
            wvDiscover.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            wvDiscover.getSettings().setDatabaseEnabled(false);
            wvDiscover.getSettings().setDomStorageEnabled(false);
            wvDiscover.getSettings().setGeolocationEnabled(false);
            wvDiscover.getSettings().setSaveFormData(false);
            wvDiscover.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // chromium, enable hardware acceleration
                wvDiscover.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                // older android version, disable hardware acceleration
                wvDiscover.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            wvDiscover.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");

            wvDiscover.loadUrl(url);
            wvDiscover.setWebViewClient(new MyWebViewClient());
        }
    }

    public class MyWebViewClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            pbar.setVisibility(View.GONE);

        }

    }
}
