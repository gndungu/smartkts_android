package com.smarttickets.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Created by gndungu on 7/1/2017.
 */
public class SaveSharedPreference {
    static final String PREF_LOGGEDIN_USER_User_Name = "logged_in_user_name";
    static final String BUSINESS = "business";
    static final String EVENT = "event";
    static final String PREF_LOGGEDIN_USER_Password = "logged_in_user_password";
    static final String PREF_LOGGEDIN_USER_Providerid = "logged_in_user_providerid";
    static final String PREF_LOGGEDIN_USER_Instancecode = "logged_in_user_instancecode";
    static final String PREF_USER_LOGGEDIN_STATUS = "logged_in_status";
    static final String PREF_LOGGEDIN_USER_ScanType = "scan_type";
    static final String SESSIONOPTION = "session_option";

    public static SharedPreferences getSharedPreferences(Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserLoggedInStatus(Context ctx, boolean status)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_USER_LOGGEDIN_STATUS, status);
        editor.commit();
    }

    public static void setLoggedInUserUserName(Context ctx, String User)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_User_Name, User);
        editor.commit();
    }

    public static void setLoggedInUserPassword(Context ctx, String Password)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_Password, Password);
        editor.commit();
    }

   public static void setLoggedInUserProviderId(Context ctx, String Providerid)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_Providerid, Providerid);
        editor.commit();
    }

    public static void setLoggedInUserInstanceCode(Context ctx, String Instancecode)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_Instancecode, Instancecode);
        editor.commit();
    }

    public static void setScanType(Context ctx, String scanType)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_ScanType, scanType);
        editor.commit();
    }

    public static void setEvent(Context ctx, String evt)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(EVENT, evt);
        editor.commit();
    }

    public static void setSessionOption(Context ctx, String option)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(SESSIONOPTION, option);
        editor.commit();
    }

    /**  public static void setBusiness(CategoryList categoryList, String biz)
    {
        Editor editor = getSharedPreferences(categoryList).edit();
        editor.putString(BUSINESS, biz);
        editor.commit();
    }

    public static void setEvent(LoadTickets ctx, String evt)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(EVENT, evt);
        editor.commit();
    }
**/
    public static String getScanType(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_ScanType, "");
    }

    public static String getBusiness(Context ctx)
    {
        return getSharedPreferences(ctx).getString(BUSINESS, "");
    }

    public static String getEvent(Context ctx)
    {
        return getSharedPreferences(ctx).getString(EVENT, "");
    }

    public static boolean getUserLoggedInStatus(Context ctx)
    {
        return getSharedPreferences(ctx).getBoolean(PREF_USER_LOGGEDIN_STATUS, false);
    }

    public static String getLoggedInUserUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_User_Name, "");
    }
    public static String getLoggedInUserPassword(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_Password, "");
    }

    public static String getLoggedInUserProviderId(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_Providerid, "");
    }
    public static String getLoggedInUserInstanceCode(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_Instancecode, "");
    }
    public static String getSessionOption(Context ctx)
    {
        return getSharedPreferences(ctx).getString(SESSIONOPTION, "default");
    }

    public static void clearLoggedInUserAddress(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_LOGGEDIN_USER_User_Name);
        editor.remove(PREF_LOGGEDIN_USER_Password);
        editor.remove(PREF_USER_LOGGEDIN_STATUS);
        editor.remove(PREF_LOGGEDIN_USER_Providerid);
        editor.remove(PREF_LOGGEDIN_USER_Instancecode);
        editor.commit();
    }

    public static void clearAllSharedPreferences(Context ctx){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
