package com.smarttickets.mobile;

import android.os.Environment;

/**
 * Created by gndungu on 3/14/2018.
 */

public class CheckForSDCard {
    public boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
