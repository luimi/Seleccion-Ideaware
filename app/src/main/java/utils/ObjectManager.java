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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lui2mi.testideaware.MainActivity;
import com.lui2mi.testideaware.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lui2mi on 16/08/16.
 */
public class ObjectManager {
    public Adapter_places adapter;
    public JSONArray places;
    public GoogleMap map;
    private SharedPreferences sharedPreferences;

    /**
     * This class is made to manage basic structure for the test
     * @param con
     */
    public ObjectManager(Context con) {
        sharedPreferences=con.getSharedPreferences("places",Context.MODE_PRIVATE);
        try{
            places=new JSONArray(sharedPreferences.getString("data","[]"));
        }catch (Exception e){
            places=new JSONArray();
        }
    }

    /**
     * Its necesary initiate the adapter on the activity to store the activity context
     * @param con
     */
    public void initAdapter(Context con){
        adapter=new Adapter_places(con);
    }

    /**
     * This class is de adapter for the list of places stored
     */
    private class Adapter_places extends BaseAdapter{
        private Context con;

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(con).inflate(R.layout.adapter_places,null);
            JSONObject o= (JSONObject) getItem(position);
            ((TextView) convertView.findViewById(R.id.tv_title))
                    .setText(o.optString("title"));
            ((TextView) convertView.findViewById(R.id.tv_description))
                    .setText(o.optString("description"));
            ((ImageButton)convertView.findViewById(R.id.ibtn_edit))
                    .setOnClickListener(EditPlace(position));
            ((ImageButton)convertView.findViewById(R.id.ibtn_delete))
                    .setOnClickListener(deletePlace(position));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addMarker(position);
                    ((MainActivity)con).pager.setCurrentItem(0);
                }
            });
            return convertView;
        }

        /**
         * Delete selected place of the list
         * @param position
         * @return
         */
        public View.OnClickListener deletePlace(final int position){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Events().dialog(con,con.getString(R.string.dialog_delete_1), null,
                            con.getString(R.string.dialog_btn_delete),
                            con.getString(R.string.dialog_btn_cancel),
                            delete(position), null);
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

        /**
         * Display a dialog that modify the selected place
         * @param position
         * @return
         */
        public View.OnClickListener EditPlace(final int position){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogEdit(con, position);
                }
            };
        }
    }

    /**
     * This method allow the user to add and save places to the list, the structure of the json is:
     * {"title":"String","description":"String","lat":double,"lng":double}
     * @param place
     */
    public void AddPlace(JSONObject place){
        places.put(place);
        sortPlaces();
        adapter.notifyDataSetChanged();
        saveNow();
    }

    /**
     * Saves current list of places
     */
    public void saveNow(){
        sharedPreferences.edit().putString("data",places.toString()).commit();

    }

    /**
     * Dialogs to Add or modify places
     * @param con
     * @param location
     */
    public void dialogAdd(Context con, Location location){
        dialog(con, con.getString(R.string.dialog_newplace), -1, location);
    }
    public void dialogEdit(Context con, int position){
        dialog(con, con.getString(R.string.dialog_editplace), position, null);
    }
    private void dialog(final Context con, String title, final int position, final Location location){
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
        builder.setPositiveButton(con.getString(R.string.dialog_btn_done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!titleAdd.getText().toString().equals("") &&
                        !descAdd.getText().toString().equals("")){
                    if(position>=0){
                        try{
                            JSONObject o = places.optJSONObject(position);
                            o.put("title",titleAdd.getText().toString());
                            o.put("description",descAdd.getText().toString());
                            places.put(position,o);
                            sortPlaces();
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
                }else{
                    new Events().dialog(con,con.getString(R.string.dialog_error_newplace),
                            null,
                            con.getString(R.string.dialog_btn_ok),null,null,null);
                }

            }
        });
        builder.setNegativeButton(con.getString(R.string.dialog_btn_cancel), null);

        builder.show();
    }

    /**
     * This sort should not be used, its better store all data in DB
     */
    private void sortPlaces(){
        List<JSONObject> sorted = new ArrayList<JSONObject>();
        for (int i = 0; i < places.length(); i++) {
            sorted.add(places.optJSONObject(i));
        }
        Collections.sort(sorted, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = a.optString("title");
                String valB = b.optString("title");
                return valA.compareTo(valB);
            }
        });

        for (int i = 0; i < sorted.size(); i++) {
            try {
                places.put(i,sorted.get(i));
            } catch (JSONException e) {}
        }
    }

    /**
     * Add marker to map
     */
    public void addMarker(int position){
        map.clear();
        JSONObject place=places.optJSONObject(position);
        try{
            LatLng point=new LatLng(place.getDouble("lat"),place.getDouble("lng"));
            map.addMarker(new MarkerOptions().position(point).title(place.optString("title"))
                    .snippet(place.optString("description")).flat(true));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14f));
        }catch (Exception e){}

    }

}
