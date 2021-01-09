package com.smarttickets.mobile;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by gndungu on 6/30/2017.
 */
public class Config {
    public static final String URL = "http://188.226.251.9:1200/dntthda/";
    //public static final String URL = "http://raw.smartkts.com:1200/dntthda/";
//    /public static final String URL = "http://dev.smartkts.com/dntthda/";

//    public static final String URL = "https://smartkts.com/dntthda/";
    //public static final String SURL = "http://dev.smartkts.com/";
    public static final String SURL = "https://smartkts.com/";

    public  static void myDialog(Context ctx, String[] data){
        String title = data[0];
        String message = data[1];
        AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public  static void noInternetDialog(Context ctx){
        try {
            String title = "Error";
            String message = "Please make sure you have internet access.";
            AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
