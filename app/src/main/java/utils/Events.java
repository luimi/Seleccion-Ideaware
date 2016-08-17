package utils;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

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
        return lm.getLastKnownLocation(lm.getBestProvider(new Criteria(),false));
    }
}
