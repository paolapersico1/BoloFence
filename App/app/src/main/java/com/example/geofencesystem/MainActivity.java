package com.example.geofencesystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * The Main Activity class
 * */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MA_LOG" ;

    private static final String DEFAULT_INITIAL_URL_GEOFENCE = "https://it.wikipedia.org/wiki/Geo-fence" ;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int PERMISSION_ALL = 100; // id group used during the permissions request

    //used as key values on shared preferences
    private String urlKey;
    private String switchKey;
    private String isFirstStartKey;
    private SharedPreferences sharedPref;

    private Switch mSwitch;
    private WebView mWebView ;
    private TextView mTextView;

    private boolean isFirstStart;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "ON CREATE ! ");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        urlKey = this.getString(R.string.urlKey); // resource value is url
        switchKey = this.getString(R.string.switchKey);//resource value is sb_key";
        isFirstStartKey = this.getString(R.string.isFirstStartKey);
        sharedPref = this.getSharedPreferences( getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        mSwitch = (Switch) findViewById(R.id.switch1);
        mTextView = (TextView) findViewById(R.id.textView);
        mWebView = (WebView) findViewById(R.id.webViewHome);

        mSwitch.setOnCheckedChangeListener(this);

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebViewClient(new WebViewClient());
        WebSettings webViewSettings = mWebView.getSettings();
        webViewSettings.setLoadsImagesAutomatically(true);
        mWebView.clearCache(true);
        webViewSettings.setJavaScriptEnabled(true);

        isFirstStart = sharedPref.getBoolean(isFirstStartKey,true);
        Log.d(TAG, " ON CREATE ===> FIRST START IS "+isFirstStart);

        if(savedInstanceState == null){
            boolean status = sharedPref.getBoolean(switchKey,false);
            setmTextView(status);
            mSwitch.setChecked(status);
        }

        String appName = getResources().getString(R.string.app_name);
        Toast.makeText(getApplicationContext(), "Welcome to "+appName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){

        Intent intent = getIntent();
        String mUrl = intent.getStringExtra(getResources().getString(R.string.urlKey));
        if(mUrl == null)
            mUrl = sharedPref.getString(urlKey, DEFAULT_INITIAL_URL_GEOFENCE);
        mWebView.loadUrl(mUrl);
        mWebView.clearHistory();
        if(!checkPermission())
        {
            askPermissions();
        }

        super.onResume();
    }

    private void setmTextView(boolean status){
        if(status)
            mTextView.setText(getResources().getString(R.string.BackG_ON));
        else
            mTextView.setText(getResources().getString(R.string.BackG_OFF));

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

        if(isChecked){
            if(checkPermission()){
                if(isFirstStart){
                    startBackgroundService();
                    isFirstStart = false;
                    saveFirstStartOnSharedPreferences(isFirstStart);
                }else{
                    //Toast.makeText(this, " TRAKING SERVICE ALREADY IN EXECUTION IT WILL BE NO TRIGGERED AGAIN",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SERVICE ALREADY IN EXECUTION IT WILL BE NO TRIGGERED AGAIN = "+isFirstStart);
                }

            }else{
                askPermissions();
                compoundButton.setChecked(false); //in this case the button stays on false
            }
        }else{
            stopBackgroundService();
            isFirstStart = true;
            saveFirstStartOnSharedPreferences(isFirstStart);
            Log.d(TAG, " THE NEXT IS FIRST START WILL BE true =="+isFirstStart);
        }

        setmTextView(isChecked);
        saveSwitchOnSharedPreferences(isChecked);

    }

    private void askPermissions() {

        for(String p: PERMISSIONS){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, p)){ //this guards misure the importance of the permition we are asking on
                Toast.makeText(this, "You should allow always this permission",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }else{
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }

    }

    private boolean checkPermission() {

        for(String p : PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void saveFirstStartOnSharedPreferences(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(isFirstStartKey);
        editor.putBoolean(isFirstStartKey, value);
        editor.commit();
    }

    private void saveSwitchOnSharedPreferences(boolean switchButton) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(switchKey);
        editor.putBoolean(switchKey, switchButton);
        editor.commit();
    }

    private void stopBackgroundService() {

        Intent mInt = new Intent(this, BackgroundService.class);
        mInt.putExtra("command","stop");
        this.stopService(mInt);
        System.out.println("!!! Stopping Tracking Service");
    }

    private void startBackgroundService() {

        System.out.println("!!!Starting Tracking Service");
        Intent myIntent = new Intent(this, BackgroundService.class);
        myIntent.putExtra("command","start");
        myIntent.putExtra("recover_state",true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(myIntent);
        }else{
            this.startService(myIntent);
        }
    }

    /**
     * this method handle the web view navigator
     * */
    @Override
    public void onBackPressed() {

        if(mWebView.canGoBack()) {
            mWebView.goBack();
        }

        if(!mWebView.canGoBack()){
            this.moveTaskToBack(true);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * Called when the app is destroyed by user or OS
     * */
    @Override
    protected void onDestroy() {
        setmTextView(false);
        mSwitch.setChecked(false);
        finish();
        super.onDestroy();
    }
}
