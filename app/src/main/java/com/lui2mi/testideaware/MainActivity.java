package com.lui2mi.testideaware;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.Events;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter adapter;

    public ViewPager pager;

    private TabLayout tabs;
    public static FloatingActionButton floatingButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.data.initAdapter(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabs = (TabLayout) findViewById(R.id.tabs);
        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
        floatingButton =(FloatingActionButton) findViewById(R.id.fab);
        if(!new Events().isGooglePlay(this)){
            new Events().dialog(this,getString(R.string.dialog_error_googlemaps),null,
                    getString(R.string.dialog_btn_ok),null,exit(),null);
        }
        new Events().isGPS(this);
    }
    private DialogInterface.OnClickListener exit(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_spotme) {//Acction spotme search the nearest point and display on the map
            if(App.data.places.length()>0){
                Location now=new Events().getMyLocation(this);
                int best=0;
                JSONObject o=App.data.places.optJSONObject(0);
                Location temp=new Location("");
                temp.setLatitude(o.optDouble("lat"));
                temp.setLongitude(o.optDouble("lng"));
                float bestDistance= now.distanceTo(temp);
                for (int i=1;i<App.data.places.length();i++){
                    o=App.data.places.optJSONObject(i);
                    temp=new Location("");
                    temp.setLatitude(o.optDouble("lat"));
                    temp.setLongitude(o.optDouble("lng"));
                    if(now.distanceTo(temp)<bestDistance){
                        best=i;
                        bestDistance=now.distanceTo(temp);
                    }
                }
                App.data.addMarker(best);
                pager.setCurrentItem(0);
            }else{
                new Events().dialog(this,getString(R.string.dialog_error_spotme),null,
                        getString(R.string.dialog_btn_ok),null,null,null);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Adapter for the pager, this is usually better dynamic adding a List of fragments and titles
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new FragmentMap();
                case 1:
                    return new FragmentPlaces();
            }
            return new UnknowFragment();
        }
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_map);
                case 1:
                    return getString(R.string.tab_places);
            }
            return "NO REGISTERED TAB";
        }


    }

    /**
     * Simple fragment for non added fragments
     */
    public static class UnknowFragment extends Fragment{
        public UnknowFragment(){}

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_unknown,container,false);
        }
    }
    /**
     * Fragment places
     */
    public static class FragmentPlaces extends Fragment{

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View main=inflater.inflate(R.layout.fragment_places,container,false);
            ListView lv= (ListView) main.findViewById(R.id.lv_places);
            lv.setAdapter(App.data.adapter);

            return main;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            if(isVisibleToUser){
                floatingButton.setImageResource(android.R.drawable.ic_menu_delete);
                floatingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Events().dialog(getContext(), getString(R.string.dialog_delete_all), null,
                                getString(R.string.dialog_btn_positive),
                                getString(R.string.dialog_btn_negative), deleteAll(), null);
                    }

                    private DialogInterface.OnClickListener deleteAll() {
                        return new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                App.data.places = new JSONArray();
                                App.data.saveNow();
                            }
                        };
                    }
                });
            }
            super.setUserVisibleHint(isVisibleToUser);
        }
    }
    /**
     * Fragment map
     */
    public static class FragmentMap extends Fragment{
        View mainView;
        MapView mapView;
        private ImageButton gps;
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mainView=inflater.inflate(R.layout.fragment_map,container,false);
            gps=((ImageButton)mainView.findViewById(R.id.ibtn_mylocation));
            mapView= (MapView) mainView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            try{
                MapsInitializer.initialize(getActivity().getApplicationContext());
            }catch (Exception e){}
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    App.data.map = googleMap;
                    //Check if user allow app to use GPS
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        App.data.map.setMyLocationEnabled(true);
                        App.data.map.getUiSettings().setMyLocationButtonEnabled(false);
                        showMyLocation();
                        gps.setEnabled(true);
                        floatingButton.setEnabled(true);
                    } else {
                        gps.setEnabled(false);
                        floatingButton.setEnabled(false);
                    }


                }
            });
            gps.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           //Move screen to my current location
                                           showMyLocation();
                                       }
                                   }
            );

            return mainView;
        }
        private void showMyLocation(){
            Location l= new Events().getMyLocation(getContext());
            if(l!=null){
                App.data.map.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude())
                                , 14f));
            }else{
                new Events().dialog(getContext(),getString(R.string.dialog_error_location),null,
                        getString(R.string.dialog_btn_ok),null,null,null);
            }
        }
        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            if(isVisibleToUser){
                floatingButton.setImageResource(android.R.drawable.ic_menu_add);
                floatingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Location loc = new Events().getMyLocation(getContext());
                        if (loc != null) {
                            App.data.dialogAdd(getContext(), loc);
                        } else {
                            new Events().dialog(getContext(), getString(R.string.dialog_error_location), null,
                                    getString(R.string.dialog_btn_ok), null, null, null);
                        }

                    }
                });
            }
            super.setUserVisibleHint(isVisibleToUser);
        }
    }
}
