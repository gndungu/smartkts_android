package com.smarttickets.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

/**
 * Created by gndungu on 4/21/2019.
 */

public class Util {

    Context context;
    private Activity _activity;

    public Util(Context context, Activity activity){
        this.context = context;
        this._activity = activity;
    }

    public void myLogoutDialog(String logout_message){
        String title = "Info";
        String message = "Are you sure you want to log out? ";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                Intent it = new Intent(context, LoginTabActivity.class);
                SaveSharedPreference.clearLoggedInUserAddress(context);
                context.startActivity(it);
                _activity.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
