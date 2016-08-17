package utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.lui2mi.testideaware.App;
import com.lui2mi.testideaware.R;

/**
 * Created by lui2mi on 16/08/16.
 */
public class Events {
    /**
     * Generic AlertDialog builder
     * @param con
     * @param title
     * @param content
     * @param posText
     * @param negText
     * @param posEvent
     * @param negEvent
     */
    public void dialog(Context con, String title, String content, String posText, String negText,
                       DialogInterface.OnClickListener posEvent, DialogInterface.OnClickListener negEvent){
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        builder.setTitle(title);
        if(content!=null)
            builder.setMessage(content);
        if(posText != null)
            builder.setPositiveButton(posText, posEvent);
        if(negText != null)
            builder.setNegativeButton(negText, negEvent);

        builder.show();
    }

    /**
     * This method returns the current location
     * @param con
     * @return
     */
    public Location getMyLocation(Context con){
        LocationManager lm =
                (LocationManager)con.getSystemService(con.LOCATION_SERVICE);

        return App.data.map.getMyLocation();//lm.getLastKnownLocation(lm.getBestProvider(new Criteria(),false));
    }

    public boolean isGooglePlay(Context con){
        try {
            ApplicationInfo info = con.getPackageManager().getApplicationInfo("com.google.android.gms",0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean isGPS(Context con){
        LocationManager lm=(LocationManager) con.getSystemService(Context
                .LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        }else{
                dialog(con,con.getString(R.string.dialog_gps_title),null,
                        con.getString(R.string.dialog_btn_positive),con.getString(R.string.dialog_btn_negative),
                        activateGPS(con),null);
            return false;
        }
    }
    public DialogInterface.OnClickListener activateGPS(final Context con) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                con.startActivity(intent);
            }
        };
    }
}
