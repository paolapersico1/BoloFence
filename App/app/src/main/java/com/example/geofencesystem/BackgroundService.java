package com.example.geofencesystem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.nio.channels.InterruptedByTimeoutException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 *
 *  This class implements a foreground service, which does the jobs required on the project specific.
 *
 *  Actually, the aims of this foreground service are the following:
 *  1.detecting position and current activity.
 *  2.sending data to server
 *  3.According to the geofences the user has got, receiving data and pushing notifications to him .
 *
 *  The class workflow can be triggered by three events:
 *  1)By user with switch button on main view.
 *  1)By receiving a call from ActivityRecognitionBroadcast
 *  2)Due to a "wake up" from a sleep status
 *
 *  To know witch one of them is calling, the putExtra intent statement is used.
 *
 *  When the f. service is triggered:
 *
 *  1) if the service is triggered by the user, the environment recover the past state if it is possible and
 *  then get its status on waiting for one of the two below events.
 *
 *  2)A new mobility mode triggers the service.
 *      Hence, the program'll store the new mobility mode.
 *
 *  3)if the services is triggered by a sleeping timer
 *      it is clear that mobility mode is not changed, so the program carries on the next step.
 *
 *  Finished above steps, the services gets the current user position and finally sent all data to server.
 *
 *  It will receive a new response, will store everyting and it will get waiting for a new event occurs
 *
 **/
public class BackgroundService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BS_LOG >>";

    private final String FIXED_URL = " https://bolofence.herokuapp.com/api/geofence/";
    private final int DEFAULT_INITIAL_PATHID = 0;
    private final long EXPIRATION_TIME = 12000; //12 seconds in millis. It never changes.
    private final long PRECISION_LIMIT = 60500; //about 60 seconds in millis. It never changes.
    private final long DEFAULT_TIME_TO_SLEEP = 60000 ; //about a minute in millis, it is the default starting value of time to sleep if the server doesn't replay

    private final int DEFAULT_ACTIVITY_VALUE = -1; //7 walk, 0 car, 1 bike, -1 unknown
    private final int DEFAULT_TRANSITION_ACTIVITY_VALUE = 1; //0 starting of, 1 finishing of an acitivity

    private long TIME_TO_SLEEP = DEFAULT_TIME_TO_SLEEP;
    boolean allowRepetition = true; //the param is used to avoid repetition if the system is killed by user while it is sleeping .

    //the names for the channel id where push the notifications.
    protected final String FOREGROUND_CHANNEL_ID ="foreground service channel" ;
    protected final String GEOFENCE_CHANNEL_ID = "geofence channel ";
    protected static final String ACTIVITY_CHANNEL_ID = "activity transition channel" ;

    //notification id of the geofence notification pushed on geofence channel
    private int notificationId = 5000;

    //this constants are used to generate the geojson field
    private final String mHead ;
    private final String mType ;
    private String mLatitudeText;
    private String mLongitudeText;
    private String mLastActivityText;

    //double to make check of optimization stuff
    private Location mLastLocation;
    private Location savedLastLocation;
    private int mLastActivity;
    private int savedLastActivity;
    private  String mCurrentGeofence;// = "No geofence found";
    private  int pathId;//= 0;

    private PendingIntent mPendingIntent;
    private PendingIntent mFirstPendingIntent;
    private SharedPreferences sharedPref;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private AsyncTask<Context, Void, Boolean> mAsyncTask;

    private boolean roundToFourDecimal = true;
    private boolean noRoundDigitCheange = true;

    /**
     * public class constructor
     * */
    public BackgroundService(){
        super();
        this.mHead = "Feature";
        this.mType = "Point";
        mLatitudeText = null;
        mLongitudeText = null;
        mLatitudeText = null;
        mLastLocation = null;
        savedLastLocation = null;
        mLastActivity = -1;
        savedLastActivity = -1;
        mCurrentGeofence = "No geofence found";
        pathId = 0;

        mPendingIntent = null;
        mGoogleApiClient = null;
    }

    /**
     * this method is an implementation required by inheriting of Service parent class.
     **/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "called onBind ");
        return null;
    }

    @Override
    public void onCreate() {
        mContext = this.getApplicationContext();
        sharedPref = mContext.getSharedPreferences( getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        makeItForeground();
        getFirstActivty(mContext);
        startLocalization(mContext);
        Log.d(TAG, "Background Service Created");
        Toast.makeText(mContext, "Background Service Created ", Toast.LENGTH_SHORT).show();
    }

    private void makeItForeground() {
        String input = "The App is monitoring your location and your activities";
        createNotificationChannels();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this,this.FOREGROUND_CHANNEL_ID)
                .setContentTitle("BoloFence ")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent) //this statement connects the open of main activity on touch of the icon.
                .build();
        Random random = new Random();
        int id = random.nextInt(9999 - 1000) + 1000;
        startForeground(id, notification);
    }

    private void createNotificationChannels() {
        // Create the NotificationChannels, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            createAChannel(this.FOREGROUND_CHANNEL_ID,
                    getString(R.string.channel_name_fservice),
                    getString(R.string.channel_description_fservice),
                    NotificationManager.IMPORTANCE_MIN);

            createAChannel(this.GEOFENCE_CHANNEL_ID,
                    getString(R.string.channel_name),
                    getString(R.string.channel_description),
                    NotificationManager.IMPORTANCE_HIGH);

            createAChannel(this.ACTIVITY_CHANNEL_ID,
                    getString(R.string.channel_name_activity_transition),
                    getString(R.string.channel_description_activity_transition),
                    NotificationManager.IMPORTANCE_NONE);

        }

    }

    private void createAChannel(String CHANNEL_ID, CharSequence name, String description, int importance){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLockscreenVisibility(NotificationCompat.DEFAULT_ALL);
            channel.setLightColor(NotificationCompat.DEFAULT_ALL);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }//else do nothing because any version lower than Oreo(8) doesn't need a chennel for notification.
    }

    private void getFirstActivty(Context context){
        Intent i = new Intent(context, ActivityRecognitionBroadcast.class);
        i.setAction(ActivityRecognitionBroadcast.FIRST_INTENT_ACTION);
        mFirstPendingIntent = PendingIntent.getBroadcast(context, 2,i, (PendingIntent.FLAG_UPDATE_CURRENT | 0));

        Task<Void> task = ActivityRecognition.getClient(context.getApplicationContext()).requestActivityUpdates(10000, mFirstPendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        System.out.println("Activity Recognition for first activity works !");
                        //Toast.makeText(mContext, "Activity Recognition for first activity works ! ", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        task.addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(Exception e) {
                      System.out.println("Activity Recognition for first activity doesn't work "+e.getCause());
                      //Toast.makeText(mContext, "Activity Recognition For first activity Error ! "+e.toString(), Toast.LENGTH_SHORT).show();
                  }
              }
        );
    }

    private void startActivitiesRecognition(Context context) {

        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Intent i = new Intent(context, ActivityRecognitionBroadcast.class);
        i.setAction(ActivityRecognitionBroadcast.INTENT_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(context, 1,i, (PendingIntent.FLAG_UPDATE_CURRENT | 0));

        Task<Void> task = ActivityRecognition.getClient(context.getApplicationContext()).requestActivityTransitionUpdates(request, mPendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        System.out.println("Activity Recognition works !");
                        Toast.makeText(mContext, "Activity Recognition works ! ", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        System.out.println("Activity Recognition not works "+e.getCause());
                        Toast.makeText(mContext, "Activity Recognition Error ! "+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * this is a core method and it is called whenever this services during algorithm life cycle is triggered by events.
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent == null ){
            return START_STICKY;
        }

        String command = intent.getStringExtra("command");

        if(command == null || !(command.equals("start"))) { //check if the command is not start, if so, the services stop itself
            allowRepetition = false;
            stopForeground(true);
            stopSelf();
            return START_STICKY;
        }


        boolean recoverStatus = intent.getBooleanExtra("recover_state",false);
        if(recoverStatus){
            recoverPathIdentifie(recoverStatus);
            Toast.makeText(this, " Tracking Service Started", Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, " Waiting for start a new activity", Toast.LENGTH_SHORT).show();
            return START_STICKY;
        }

        if(mAsyncTask!=null){
            mAsyncTask.cancel(true);
            Log.d(TAG,"Cancelled timers");
            //Toast.makeText(this, "Cancelled timers", Toast.LENGTH_SHORT).show();
        }

        int newActivityReceived = intent.getIntExtra("activityType", DEFAULT_ACTIVITY_VALUE); //put 7 walk, 0 car, 1 bike to test
        int newTransitionType = intent.getIntExtra("transitionType",DEFAULT_TRANSITION_ACTIVITY_VALUE); //put 0 to test ; it can be only two values enter = 0, exit = 1;

        if(newTransitionType == 0){//new action started detected
            allowRepetition = true;
            savedLastActivity = mLastActivity;
            mLastActivity = newActivityReceived;
            setCurrentActivity();
            makeTheJob();
        }else if(newTransitionType == 1){
            //it means the action is ended and now we don't know the action. While I'm waiting for a new action transition of enter occurrence occurs.
            //mLast Activity is default value (so unknown) until next update. No data will be send to server during this time...
            savedLastActivity= mLastActivity;
            mLastActivity = DEFAULT_ACTIVITY_VALUE;
            allowRepetition = false;
        }
        else if(newTransitionType == 2){//if the first activity is a valid one
            allowRepetition = true;

            savedLastActivity = mLastActivity;
            mLastActivity = newActivityReceived;
            setCurrentActivity(); //Setto le variabili e sono pronto per inviare.

            //unregister the first activity recognizer
            Task<Void> task = ActivityRecognition.getClient(mContext).removeActivityUpdates(mFirstPendingIntent);
            task.addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if(mFirstPendingIntent!=null)
                                mFirstPendingIntent.cancel();

                            //Toast.makeText(mContext, "First Activity Recognition unscribed ! ", Toast.LENGTH_SHORT).show();
                            //start activity transition recognition
                            startActivitiesRecognition(mContext);
                        }
                    }
            );

            task.addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            //Toast.makeText(mContext, "Failed First Activity Recognition unscribed ! ", Toast.LENGTH_SHORT).show();
                            System.out.println("On Remove Failure got" + e.getMessage());
                        }
                    }
            );
            //send the request
            makeTheJob();
        }
        else if(newTransitionType == -1){//if the first activity isn't a valid one
            Log.d(TAG, "Not valid first activity");
            savedLastActivity= mLastActivity;
            mLastActivity = DEFAULT_ACTIVITY_VALUE;
            allowRepetition = false;
        }

        return START_STICKY;
    }

    private void recoverPathIdentifie(boolean recoverState){

        if(recoverState){
            long expirationTime = sharedPref.getLong(getString(R.string.pathIdTSKey),0);
            //the first time, expiration is 0, so else occur
            if(System.currentTimeMillis() > expirationTime ){ //then two hours since last time sept
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove(this.getString(R.string.pathIdKey));
                editor.remove(this.getString(R.string.pathIdTSKey));
                editor.apply();
            }

            pathId = sharedPref.getInt(getString(R.string.pathIdKey),DEFAULT_INITIAL_PATHID);
        }

    }

    private boolean setCurrentActivity() {

        switch (mLastActivity){
            case 7:
                mLastActivityText = "walk";
                break;
            case 0:
                mLastActivityText = "car";
                break;
            case 1:
                mLastActivityText = "bike";
                break;
            default:
                mLastActivityText = "unknown";
                return false;
        }

        return true;
    }

    private void startLocalization(Context context) {
        if(mGoogleApiClient	==	null){
            mGoogleApiClient =	new	GoogleApiClient.Builder(context)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "CONNECTED to GPS Service \n");
    }

    private void makeTheJob() {
        
        if(mGoogleApiClient.isConnected()){
            Location newLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(newLocation!= null){
                savedLastLocation = mLastLocation;
                mLastLocation = newLocation;

                if((!noRoundDigitCheange) || !(savedLastActivity == mLastActivity && locationsAreEqual(roundToFourDecimal,savedLastLocation,mLastLocation))){
                    setmLatitudeText(perturbish(mLastLocation.getLatitude()));
                    setmLongitudeText(perturbish(mLastLocation.getLongitude()));
                    //Toast.makeText(this, "Sending GeoJson to server", Toast.LENGTH_SHORT).show();
                    sendToServer();
                }else{
                    //Toast.makeText(this, "Check got sleep the same time", Toast.LENGTH_SHORT).show();
                    updateTimeToSleep(TIME_TO_SLEEP);
                    sleepThenConnectAgain();
                }
            }else{
                Log.d(TAG, "NEW LOCATION IS NULL, probably your localization is turned off");
                Toast.makeText(this, "Please turn on you location", Toast.LENGTH_LONG).show();
                updateTimeToSleep(TIME_TO_SLEEP);
                sleepThenConnectAgain();
            }
        }else{
            mGoogleApiClient.connect();
            updateTimeToSleep(TIME_TO_SLEEP);
            sleepThenConnectAgain();
        }

    }

    private boolean locationsAreEqual(boolean isFourDigit ,Location one , Location two) {

        if (one == null && two == null) return true; //for the first execution
        if (one == null) return false; //so the secondone is not null
        if (two == null) return false; //never called but to be sure of data consistency.

        double latOne , latTwo , lngOne, lngTwo ;

        if(!isFourDigit){
            //Log.d(TAG, "checking on 3 digit ");
            latOne = truncateToThreeDigit(one.getLatitude());
            latTwo = truncateToThreeDigit(two.getLatitude());
            lngOne = truncateToThreeDigit(one.getLongitude());
            lngTwo = truncateToThreeDigit(two.getLongitude());
        }else{
            //Log.d(TAG, "checking on 4 digit ");
            latOne = truncateToFourDigit(one.getLatitude());
            latTwo = truncateToFourDigit(two.getLatitude());
            lngOne = truncateToFourDigit(one.getLongitude());
            lngTwo = truncateToFourDigit(two.getLongitude());
        }

        boolean latEqual = (latOne == latTwo );
        boolean lngEqual = (lngOne == lngTwo );

        return (latEqual && lngEqual);
    }

    private String perturbish(double coordinate) {

        double d = coordinate;

        if(roundToFourDecimal){
            Log.d(TAG,"Truncate to 4 decimal ");
            return ""+truncateToFourDigit(d);
        }

        Log.d(TAG,"Truncate to 3+1r decimal ");
        double x = truncateToThreeDigit(d);
        x= x * 10000;
        long seed = System.currentTimeMillis();
        int y = new Random(seed).nextInt(10); //it generats random number between 0 and 9
        double z = (x + y ) / 10000;
        return ""+z ;
        //back to double format !! the 0s are lost on division by 10000  ==> //String s = ""+x+""+y; //double z = Double.valueOf(s) ;
    }

    private double truncateToThreeDigit(double x){
        return Math.floor(x * 1000) / 1000; //rount to third digit
    }
    private double truncateToFourDigit(double x){
        return Math.floor(x * 10000) / 10000; //rount to third digit
    }


    private void setmLatitudeText(String s){  mLatitudeText = s;    }

    private void setmLongitudeText(String s){ mLongitudeText = s;   }

    private void sendToServer() {
        String url = FIXED_URL;
        // we use Retrofit to use the REST service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeofenceService gs = retrofit.create(GeofenceService.class);
        InputData mInput = generateGeoJSONInputData();
        Call<List<Geofence>> call = gs.getGeofence(mInput);
        call.enqueue(new Callback<List<Geofence>>() {
            @Override
            public void onResponse(Call<List<Geofence>> call, retrofit2.Response<List<Geofence>>  response) {
                List<Geofence> list = response.body();
                try {
                    if(list == null){
                        Log.d(TAG,"Response: " + response.errorBody().string());
                    }else{
                        for (Geofence g : list) {
                            Log.d(TAG,"REPLY FROM SERVER: SleepTime: " + g.getSleepTime() + " pathID "+g.getPathID()+" message "+g.getMessage());
                            long millisForTimeToSleep = (int) g.getSleepTime() * 1000; //from seconds to millis
                            updateTimeToSleep(millisForTimeToSleep);
                            //TIME_TO_SLEEP = millisForTimeToSleep;
                            Log.d(TAG, "NEW SLEEP FOR "+TIME_TO_SLEEP);
                            pathId = g.getPathID(); //set the new path id
                            saveOnPreferencesPathID(pathId); //save the new path id
                            String s = g.getMessage();
                            String saved = mCurrentGeofence;
                            if(!s.equals("No new geofence")){ //New message is always got except for the "no new geofence" returned message. In that case currentGeofence doesn't change
                                mCurrentGeofence = s;
                                //saveOnPreferencesLastCurrentGeofence(mCurrentGeofence); be careful with this
                            }
                            //these are the two cases for which I don't show the notification.
                            if(!mCurrentGeofence.equals("No geofence found") && !mCurrentGeofence.equals(saved)){
                                addNotification();
                            }
                            //two checks in allow repetition, before and after the sleeptime, to improve system performance.
                            if(allowRepetition){
                                sleepThenConnectAgain();
                            }else{
                                Log.d(TAG, "NO MORE CALLS TO LOCATION FORM NOW");
                            }
                        }
                    }

                }catch(IOException ce){
                    Log.d(TAG,"IOException "+ce.toString());
                }catch(Exception e){
                    Log.d(TAG,"Exception e :"+e.toString());
                }
            }
            @Override
            public void onFailure(Call<List<Geofence>> call, Throwable t) {
                //this code is executed if we fail
                Log.d(TAG,"send to server failed because "+t.getMessage());
                //if there's a network error, I'll try again
                if(allowRepetition){
                    updateTimeToSleep(DEFAULT_TIME_TO_SLEEP);
                    sleepThenConnectAgain();
                }else{
                    Log.d(TAG, "NO MORE CALLS TO LOCATION FORM NOW");
                }
            }
        });

    }

    /**
     * this method creates the element to sen to to server
     * */
    private InputData generateGeoJSONInputData() {
        String head = mHead ;
        //geometry params
        String type = mType ;
        double lat = Double.parseDouble(mLatitudeText);
        double lng = Double.parseDouble(mLongitudeText);
        //Properties params
        String activity = mLastActivityText ;
        int pathId = this.pathId;
        String currentGeofence = mCurrentGeofence;

        Geometry geom = new Geometry(type,lng,lat);
        Properties pp = new Properties(activity, pathId, currentGeofence);
        InputData inputdata = new InputData(head, geom, pp);
        Log.d(TAG, "sending to server... "+inputdata.toString());
        return inputdata;

    }
    private void addNotification() {

        //check if it is an url or a message
        //if it is a message, no action on Notification. Else, Action on Notification
        String message = mCurrentGeofence;
        int lastIndex = Math.min(message.length(), 50); //the min to avoid null pointer exception
        String reducedMessage =  message.substring(0,lastIndex) + "...";// "that's the message reduces to show in reduced icon format ▼ "
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GEOFENCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setShowWhen(true)
                .setContentTitle("Got new geofence !")
                .setContentText(reducedMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(false);

        if(mCurrentGeofence.substring(0,4).equals("http")){
            saveOnPreferencesLastURL(mCurrentGeofence); //only in this case i can save new url for view.
            Intent notifyIntent = new Intent(this, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notifyIntent.putExtra("url",mCurrentGeofence);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(notifyPendingIntent);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        int id = notificationId++;
        notificationManager.notify(id, builder.build());

    }


    private void sleepThenConnectAgain() {

        mAsyncTask = new AsyncTask<Context,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Context... contexts) {
                try{
                    Log.d(TAG, "start sleeping... "+TIME_TO_SLEEP+" millis");
                    Thread.sleep(TIME_TO_SLEEP);
                    Log.d(TAG, "wake up......");
                    return true;
                }catch (InterruptedException IE){
                    Log.d(TAG, "DELETED TIMER!! Interrupted Exception during a sleep moment "+IE.toString());
                    if(isCancelled()){
                        //Log.d(TAG,"is cancelled");
                        return true;
                    }
                    return false;
                }catch (Exception e){
                    Log.d(TAG, "Exception during a sleep moment "+e.toString());
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(allowRepetition){
                    savedLastActivity = mLastActivity; //on wake up, the activity is the old than before. So now it is also my past activty

                    if(!mGoogleApiClient.isConnected()){
                        mGoogleApiClient.connect(); //start again
                    }else{
                        makeTheJob();
                    }


                }else{//check utility of this else, actually, maybe it is never the case
                    if(mGoogleApiClient.isConnected()){
                        mGoogleApiClient.disconnect();
                    }
                }
            }

        }.execute(mContext);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"On Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("CONNECTION TO gAPI FAILED");
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    private void saveOnPreferencesPathID(int pathId) {
        long expiritionTimestamp = (System.currentTimeMillis() + EXPIRATION_TIME);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(this.getString(R.string.pathIdKey));
        editor.remove(this.getString(R.string.pathIdTSKey));
        editor.putInt(this.getString(R.string.pathIdKey),pathId);
        editor.putLong(this.getString(R.string.pathIdTSKey),expiritionTimestamp);
        editor.apply();

    }

    private void saveOnPreferencesLastURL(String url){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(this.getString(R.string.urlKey));
        editor.putString(this.getString(R.string.urlKey),url);
        editor.apply();
    }

    /**
     * called when the service terminates his execution
     * */
    @Override
    public void onDestroy() {
        saveOnPreferencesPathID(pathId);
        allowRepetition = false;
        stopLocalization();
        stopActivitiesRecognition();
        if(mAsyncTask!=null){
            mAsyncTask.cancel(true);
        }

        stopForeground(true);
        stopSelf();
        Log.d(TAG, "CALLED ON DESTROY ");
        Toast.makeText(this, "BoloFence Tracking Service now is Stopped", Toast.LENGTH_SHORT).show();
        //super.onDestroy();
    }

    private void stopLocalization() {
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    private void stopActivitiesRecognition() {
        // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
        Task<Void> task = ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mPendingIntent);
        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if(mPendingIntent!=null)
                            mPendingIntent.cancel();
                        //Toast.makeText(mContext, "Activity Recognition unscribed ! ", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Toast.makeText(mContext, "Failed Activity Recognition unscribed ! ", Toast.LENGTH_SHORT).show();
                        System.out.println("On Remove Task Failure got" + e.getMessage());
                    }
                }
        );
        Task<Void> task2 = ActivityRecognition.getClient(mContext).removeActivityUpdates(mFirstPendingIntent);
        task2.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if(mFirstPendingIntent!=null)
                            mFirstPendingIntent.cancel();
                        //Toast.makeText(mContext, "First Activity Recognition unscribed ! ", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        task2.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Toast.makeText(mContext, "Failed First Activity Recognition unscribed ! ", Toast.LENGTH_SHORT).show();
                        System.out.println("On Remove Task2 Failure got" + e.getMessage());
                    }
                }
        );
    }

    synchronized public void  updateTimeToSleep(long timeToSleep){

        int maxThreshold = 1800*1000; //30 minutes in millis
        if(timeToSleep > maxThreshold){
            timeToSleep = maxThreshold;
        }

        TIME_TO_SLEEP = timeToSleep;
        boolean pastRoundToFourDecimal = roundToFourDecimal;
        roundToFourDecimal = (TIME_TO_SLEEP < PRECISION_LIMIT);
        noRoundDigitCheange = (pastRoundToFourDecimal == roundToFourDecimal);
        Toast.makeText(mContext, "Time to sleep:"+TIME_TO_SLEEP, Toast.LENGTH_SHORT).show();
    }
}
