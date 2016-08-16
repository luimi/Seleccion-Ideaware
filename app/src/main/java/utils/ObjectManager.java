package utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lui2mi.testideaware.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lui2mi on 16/08/16.
 */
public class ObjectManager {
    public Adapter_places adapter;
    public JSONArray places;
    private SharedPreferences sharedPreferences;
    public ObjectManager(Context con) {
        sharedPreferences=con.getSharedPreferences("places",Context.MODE_PRIVATE);
        try{
            places=new JSONArray(sharedPreferences.getString("data","[]"));
        }catch (Exception e){
            places=new JSONArray();
        }
        adapter=new Adapter_places(con);

    }

    private class Adapter_places extends BaseAdapter{
        Context con;

        public Adapter_places(Context con) {
            this.con = con;
        }

        @Override
        public int getCount() {
            return places.length();
        }

        @Override
        public Object getItem(int position) {
            return places.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(con).inflate(R.layout.adapter_places,null);
            JSONObject o= (JSONObject) getItem(position);
            ((TextView) convertView.findViewById(R.id.tv_title))
                    .setText(o.optString("title"));
            ((TextView) convertView.findViewById(R.id.tv_description))
                    .setText(o.optString("description"));
            ((ImageButton)convertView.findViewById(R.id.ibtn_edit))
                    .setOnClickListener(EditPlace(position));
            ((ImageButton)convertView.findViewById(R.id.ibtn_delete))
                    .setOnClickListener(DeletePlace(position));
            return convertView;
        }
        public View.OnClickListener DeletePlace(final int position){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Events().Dialog(con, "Do you want to delete this place?", null,
                            "DELETE", "CANCEL", delete(position), null);
                }

                private DialogInterface.OnClickListener delete(final int position) {
                    return new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            places.remove(position);
                            Adapter_places.this.notifyDataSetChanged();
                            saveNow();
                        }
                    };
                }
            };
        }
        public View.OnClickListener EditPlace(final int position){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            };
        }
    }
    public void AddPlace(JSONObject place){
        places.put(place);
        adapter.notifyDataSetChanged();
        saveNow();
    }
    public void saveNow(){
        sharedPreferences.edit().putString("data",places.toString()).commit();

    }
    public void DialogAdd(Context con,Location location){
        DialogView(con, "Save this place", -1, location);
    }
    public void DialogEdit(Context con,int position){
        DialogView(con,"Edit this place",position,null);
    }
    public void DialogView(Context con, String title, final int position, final Location location){
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        builder.setTitle(title);
        View view=LayoutInflater.from(con).inflate(R.layout.dialog_addedit,null);
        final EditText titleAdd=(EditText) view.findViewById(R.id.edt_title);
        final EditText descAdd=(EditText) view.findViewById(R.id.edt_description);
        if(position>=0){
            JSONObject o = places.optJSONObject(position);
            titleAdd.setText(o.optString("title"));
            descAdd.setText(o.optString("description"));
        }
        builder.setView(view);
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(position>=0){
                    try{
                        JSONObject o = places.optJSONObject(position);
                        o.put("title",titleAdd.getText().toString());
                        o.put("description",descAdd.getText().toString());
                        places.put(position,o);
                        adapter.notifyDataSetChanged();
                        saveNow();
                    }catch (Exception e){}

                }else{
                    try{
                        JSONObject o = new JSONObject();
                        o.put("title",titleAdd.getText().toString());
                        o.put("description",descAdd.getText().toString());
                        o.put("lat",location.getLatitude());
                        o.put("lng",location.getLongitude());
                        AddPlace(o);
                    }catch (Exception e){}
                }
            }
        });
        builder.setNegativeButton("CANCEL", null);

        builder.show();
    }

}
