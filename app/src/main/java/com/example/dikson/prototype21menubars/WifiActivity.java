package com.example.dikson.prototype21menubars;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class WifiActivity extends AppCompatActivity {
    //-----------------Toolbar---------------------------
    private DrawerLayout drawerLayout;

    //-------------------WIFI----------------------------
    WifiManager wifiManager;
    String[] availableWifiList, bssidList;
    String timeStamp;
    Handler handler;
    List<ScanResult> scanResults;
    Runnable runnable;
    EditText input;
    Spinner spinner;
    StringBuilder wifisb;
    boolean isEduroamFiltered = true;

    //------------------------Location-------------------------
    private final static int ALL_PERMISSIONS_RESULT = 101;

    LocationManager locationManager;
    Location location;

    double latitude, longitude;

    ArrayList<String> permissions;
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();

    boolean canGetLocation = true;

    TextView LongitudeValue, LatitudeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

//------------------------------------Toolbars--------------------------------------------------------
//Enables the functions of the custom toolbars
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.T_WifiTools);
        setSupportActionBar(toolbar);
//Setting the navigation menu button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.DL_WifiMenuBar);

        NavigationView navigationView = findViewById(R.id.NV_WifiLists);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //Selected item is highlighted
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {

                    case R.id.I_WifiList:
                        //Closes the drawer when item is selected
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.I_F3A:
                        Intent F3 = new Intent(WifiActivity.this, MainActivity.class);
                        startActivity(F3);
                        //Closes the drawer when item is selected
                        drawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });

//---------------------------------------------------WIFI-----------------------------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        handler = new Handler();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        scanWifi();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 3000);
                scanWifi();
//                updateUI(location);
            }
        };
        handler.post(runnable);

//-----------------------------------Location--------------------------------------------

//        tvLatitude = findViewById(R.id.tvLatitude);
//        tvLongitude = findViewById(R.id.tvLongitude);
//        tvTime = findViewById(R.id.tvTime);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

        permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);


        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            //Connection off
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
            alertDialog.setTitle("Location is Disabled");
            alertDialog.setMessage("Enable location?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Toast.makeText(WifiActivity.this, "Location Service is required", Toast.LENGTH_SHORT).show();
                }
            });

            alertDialog.show();

        } else {
            //connection on
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    canGetLocation = false;
                }
            }
            getLocation();
        }
    }

    //------------------------------Back Button Function------------------------------------------------
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();

        } else
            super.onBackPressed();
    }

    //------------------------------Populate the option menu-------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_view, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //-------------------------------Functions for app bar's button------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        item.setChecked(true);
        switch (item.getItemId()) {
//enables the menu button to be clicked

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.I_Comment:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("COMMENTS");
                alertDialog.setMessage("Enter Remarks");

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);

                spinner = new Spinner(this);
                layout.addView(spinner);

                input = new EditText(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                layout.addView(input);

                alertDialog.setView(layout);

                alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveData();
                    }
                });

                Spinner_items();
                alertDialog.create().show();
                return true;

            case R.id.I_Eduroam:
                isEduroamFiltered = true;
                scanWifi();
                return true;

            case R.id.I_ListAvailable:
                isEduroamFiltered = false;
                scanWifi();
                return true;

            case R.id.I_GPS:
                AlertDialog.Builder GPSDialog = new AlertDialog.Builder(this);
                GPSDialog.setTitle("GPS Information");

                LinearLayout layout_GPS = new LinearLayout(this);
                layout_GPS.setOrientation(LinearLayout.VERTICAL);

                TextView Longitude = new TextView(this);
                layout_GPS.addView(Longitude);
                Longitude.setText("Longitude:");

                LongitudeValue = new TextView(this);
                layout_GPS.addView(LongitudeValue);
                LongitudeValue.setText(Double.toString(longitude));


                TextView Latitude = new TextView(this);
                layout_GPS.addView(Latitude);
                Latitude.setText("Latitude:");

                LatitudeValue = new TextView(this);
                layout_GPS.addView(LatitudeValue);
                LatitudeValue.setText(Double.toString(latitude));

                GPSDialog.setView(layout_GPS);

                GPSDialog.create().show();


                return true;


        }
//        updateUI(location);

        return super.onOptionsItemSelected(item);

    }

    //--------------------------------------------Populating the spinner view----------------------------------------------------------------------------
    public void Spinner_items() {
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.Conditions, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    //------------------------------------------------WIFI------------------------------------------------------------
    public void scanWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                Toast.makeText(this, "Enabling Wifi", Toast.LENGTH_SHORT).show();
            } else {
                wifiManager.startScan();
                timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                scanResults = wifiManager.getScanResults();
                availableWifiList = new String[scanResults.size()];

                if (scanResults.size() == 0) {
                    Toast.makeText(this, "No Access point", Toast.LENGTH_SHORT).show();
                } else {
                    if (!isEduroamFiltered) {
                        noFilter();
                    } else {
                        setFilter();
                    }
                }
            }
        }
    }

    public void listWifi() {
        ListView wifiListView = findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter
                = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableWifiList);
        wifiListView.setAdapter(arrayAdapter);
    }


    public void noFilter() {
        bssidList = new String[scanResults.size()];

        for (int i = 0; i < scanResults.size(); i++) {
            availableWifiList[i] = ("SSID: " + scanResults.get(i).SSID
                    + "\nBSSID: " + scanResults.get(i).BSSID
                    + "\nRSSI: " + scanResults.get(i).level + "dBm");
        }
        listWifi();
    }

    public void setFilter() {
        int count = 0;
        wifisb = new StringBuilder();
        for (int i = 0; i < scanResults.size(); i++) {
            String wifiSSID = scanResults.get(i).SSID;
            if (wifiSSID.equals("eduroam")) {
                count++;
            }
        }

        availableWifiList = new String[count];
        bssidList = new String[count];
        count = 0;

        for (int i = 0; i < scanResults.size(); i++) {
            String wifiSSID = scanResults.get(i).SSID;
            if (wifiSSID.equals("eduroam")) {
                availableWifiList[count] = ("SSID: " + scanResults.get(i).SSID
                        + "\nBSSID: " + scanResults.get(i).BSSID
                        + "\nRSSI: " + scanResults.get(i).level + "dBm");
                wifisb.append("," + scanResults.get(i).BSSID + "," + scanResults.get(i).level);
                count++;
            }
        }
        listWifi();
    }

    //-------------------------------------------Saving Datas----------------------------------------------
    public void saveData() {
        //FYP BOOK
        String getRemark;
        String getSpinner;


        String x, y, z;
        EditText ET_x, ET_y, ET_z;
        ET_x = findViewById(R.id.wifi_ET_X);
        ET_y = findViewById(R.id.wifi_ET_Y);
        ET_z = findViewById(R.id.wifi_ET_Z);
        x = ET_x.getText().toString();
        y = ET_y.getText().toString();
        z = ET_z.getText().toString();

        StringBuilder sb = new StringBuilder();
        String layoutData = getIntent().getStringExtra("Extra_LayoutData");
//        String gpsData = getIntent().getStringExtra(" Extra_GpsData");
        getRemark = input.getText().toString();
        getSpinner = spinner.getSelectedItem().toString();

        try {
//            FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), "CollectedData.json"));
            File file1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filePath1 = file1.getAbsolutePath() + "/CollectedData.json";
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(filePath1, true));

            JsonWriter jsonWriter = new JsonWriter(new FileWriter(filePath1, true));
            jsonWriter.setIndent("  ");


            jsonWriter.beginObject();
            jsonWriter.name("dataset:").value("DikSon_2");
            jsonWriter.name("X").value(x);
            jsonWriter.name("Y").value(y);
            jsonWriter.name("Z").value(z);
            jsonWriter.name("timestamp:").value(timeStamp);
            jsonWriter.name("tag:").value(getSpinner+getRemark);
            jsonWriter.name("dims:");
            jsonWriter.beginObject();

            for(int i=0; i<scanResults.size() ;i++)
            {
                String wifiID = scanResults.get(i).SSID;
                if (wifiID.equals("eduroam"))
                {
                    jsonWriter.name(scanResults.get(i).BSSID).value(scanResults.get(i).level);
                }

            }

            jsonWriter.endObject();
            jsonWriter.endObject();
            writer1.append(",");
            writer1.close();
            jsonWriter.close();

//            FileOutputStream fos2 = new FileOutputStream(new File(getFilesDir(), "CollectedData.csv"));
            File file2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filePath2 = file2.getAbsolutePath() + "/CollectedData.csv";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath2, true));

            sb.append("DikSon_2" + "," + x + "," + y + "," + z + "," + timeStamp + "," + getSpinner + getRemark + wifisb);

            sb.append("\n");

            writer.append(sb.toString());
            writer.close();
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //--------------------------------------Location----------------------------------------------------
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateUI(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            getLocation();
        }

        @Override
        public void onProviderDisabled(String s) {

            if (locationManager != null) {
                //stops location updates when location services are disabled
                locationManager.removeUpdates(this);
            }

        }
    };

    private void getLocation() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null)
                        updateUI(location);
                }

            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null)
                        updateUI(location);
                }
            } else {
                location.setLatitude(0);
                location.setLongitude(0);
                updateUI(location);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void updateUI(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

//        tvTime.setText(DateFormat.getTimeInstance().format(location.getTime()));
    }

//--------------------------Location Permissions---------------------------------------------

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(permissionsRejected.toArray(
                                                    new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    canGetLocation = true;
                    getLocation();
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(WifiActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}