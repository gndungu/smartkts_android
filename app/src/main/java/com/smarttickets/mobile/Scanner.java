package com.smarttickets.mobile;

import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.sourceforge.zbar.ImageScanner;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class Scanner extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    TextView scanText;
    Button scanButton;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private LoadEventActivity.ProcessingTask mAuthTask = null;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private String provider_id;
    private String instancecode;
    private String uUsername;
    private String uPassword;
    private static final String TAG = "MainActivity";
    AlertDialog pdialog;
    AlertDialog.Builder pbuilder;
    LayoutInflater pfactory;
    View pview;
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    private ZBarScannerView mScannerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_scanner);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(SaveSharedPreference.getUserLoggedInStatus(this) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(this);
            uPassword = SaveSharedPreference.getLoggedInUserPassword(this);
            provider_id = SaveSharedPreference.getLoggedInUserProviderId(this);
            instancecode = SaveSharedPreference.getLoggedInUserInstanceCode(this);
        }

        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.beep06);
        Log.v("Scanner", rawResult.getContents()); // Prints scan results
        Log.v("Scanner", rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)
        mp.start();
        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }

}
