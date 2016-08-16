package com.lui2mi.testideaware;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
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
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager pager;

    private TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabs = (TabLayout) findViewById(R.id.tabs);
        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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
                    return "SAVE PLACE";
                case 1:
                    return "SHOW MY PLACES";
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

    }
    /**
     * Fragment map
     */
    public static class FragmentMap extends Fragment{
        View mainView;
        MapView mapView;
        private GoogleMap map;
        private ImageButton gps,add;
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mainView=inflater.inflate(R.layout.fragment_map,container,false);
            gps=((ImageButton)mainView.findViewById(R.id.ibtn_mylocation));
            add=((ImageButton)mainView.findViewById(R.id.ibtn_add));
            mapView= (MapView) mainView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            try{
                MapsInitializer.initialize(getActivity().getApplicationContext());
            }catch (Exception e){}
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map=googleMap;
                    //Check if user allow app to use GPS
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(true);
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                        ShowMyLocation();
                    } else {
                        gps.setEnabled(false);
                        add.setEnabled(false);
                    }


                }
            });
            gps.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           //Move screen to my current location
                                           ShowMyLocation();
                                       }
                                   }
            );
            add.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           //Event add new place
                                           App.data.DialogAdd(getContext(), GetMyLocation());
                                       }
                                   }
            );
            return mainView;
        }
        private void ShowMyLocation(){
            Location l=GetMyLocation();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude())
                    , 14f));
        }
        private Location GetMyLocation(){
            LocationManager lm =
                    (LocationManager)getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            return lm.getLastKnownLocation(lm.getBestProvider(new Criteria(),false));
        }
    }
}
