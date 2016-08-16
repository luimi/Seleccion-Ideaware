package utils;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lui2mi.testideaware.R;

import org.json.JSONObject;

/**
 * Created by lui2mi on 16/08/16.
 */
public class Events {
    public void Dialog(Context con, String title, String content,String posText,String negText,
                       DialogInterface.OnClickListener posEvent,DialogInterface.OnClickListener negEvent){
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        builder.setTitle(title);
        if(content!=null)
            builder.setMessage(content);
        if(posText != null)
            builder.setPositiveButton(posText, posEvent);
        if(negText != null)
            builder.setNegativeButton(negText, posEvent);

        builder.show();
    }
}
