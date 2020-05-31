package com.example.geofencesystem;


import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class ActivityRecognitionBroadcast extends BroadcastReceiver {
    public final String TAG ="ARB_LOG";
    public static final String INTENT_ACTION = "com.example.geofencesystem.ACTION_PROCESS_ACTIVITY_TRANSITIONS";
    public static final String FIRST_INTENT_ACTION = "com.example.geofencesystem.ACTION_PROCESS_FIRST_ACTIVITY";
    private Context context;

    /**
     * don't remove it, it is called from os every time. without it, we can't receive message from TRANSITION API
     * */
    public ActivityRecognitionBroadcast(){
        Log.d(TAG,"called 0 parametres constructor");
    }

    public ActivityRecognitionBroadcast(Context context){  this.context = context;  }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"ON RECEIVE ACTIVITY RECOGNITION");
        final PendingResult pendingResult = goAsync();
        Task asyncTask = new Task(context,pendingResult, intent); //because of android background restrictions
        asyncTask.execute();

    }


    private static class Task extends AsyncTask<String, Integer, String> {
	// final before
        private static String TAG ="AsyncTask_log";
        private Context context;
        private PendingResult pendingResult;
        private Intent intent;

        public Task(Context context, PendingResult pendingResult, Intent intent) {
            this.context = context;
            this.pendingResult = pendingResult;
            this.intent = intent;
        }
	
	@Override
	protected void onPreExecute() {
	    try {
		    Log.d(TAG,"Trying to wake up BG SERVICE");
		    wakeUpBackgroundService(context,intent) ;
	    } catch (Exception e) {
		    Log.d(TAG,"ERROR: " + e);
	    }finally{
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
            super.onPreExecute();
        }
	}

        @Override
        protected String doInBackground(String... strings) {
            return "finished";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }


        private void wakeUpBackgroundService(Context context, Intent intent) throws Exception {

            String action = intent.getAction();

            if (intent != null && INTENT_ACTION.equals(action)) {

                if (ActivityTransitionResult.hasResult(intent)) {
                    ActivityTransitionEvent lastActivity = null;
                    ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);

                    /*for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                        Log.d(TAG," EVENT GET ON BR ==> "+event.toString());
                    }*/
                    ActivityTransitionEvent event = result.getTransitionEvents().get(result.getTransitionEvents().size()-1 ) ;

                    if(((SystemClock.elapsedRealtime()-(event.getElapsedRealTimeNanos()/1000000))/1000) <= 60*3) { //if it is older than 3 minutes
                        lastActivity = event;
                    }else{
                        return;
                    }

                    /*SharedPreferences sharedPref;
                    sharedPref = context.getSharedPreferences( context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    String switchKey = context.getString(R.string.switchKey);//resource value is sb_key";
                    boolean status = sharedPref.getBoolean(switchKey,false);
                    if(!status) return;*/

                    String message = print(lastActivity);
                    String channelID = BackgroundService.ACTIVITY_CHANNEL_ID;
                    int notificationId = 2000;

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("New Activity Transition !")
                            .setContentText(message)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setShowWhen(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(notificationId, builder.build());

                    Intent i = new Intent( context, BackgroundService.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("command", "start");
                    i.putExtra("activityType",lastActivity.getActivityType());
                    i.putExtra("transitionType",lastActivity.getTransitionType());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    }else{
                        context.startService(i);
                    }
                }
            }
            else if (intent != null && FIRST_INTENT_ACTION.equals(action)) {

                if (ActivityRecognitionResult.hasResult(intent)) {
                    ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                    // Get the most probable activity from the list of activities in the update
                    DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                    // Get the type of activity
                    int activityType = mostProbableActivity.getType();
                    //Log.d(TAG,"ACTIVITY GET IS ==> "+mostProbableActivity);

                    int transitionType = 2;
                    if (activityType == DetectedActivity.ON_FOOT || activityType == DetectedActivity.WALKING) {
                        activityType = 7;
                    }
                    else if (activityType == DetectedActivity.ON_BICYCLE) {
                        activityType = 1;
                    }
                    else if (activityType == DetectedActivity.IN_VEHICLE) {
                        activityType = 0;
                    }else{
                        activityType = -1;
                        transitionType = -1;
                    }

                    String message = mostProbableActivity.toString();
                    String channelID = BackgroundService.ACTIVITY_CHANNEL_ID;
                    int notificationId = 3000;

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("FIRST ACTIVITY !")
                            .setContentText(message)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setShowWhen(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(notificationId, builder.build());

                    Intent i = new Intent( context, BackgroundService.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("command", "start");
                    i.putExtra("activityType",activityType);
                    i.putExtra("transitionType",transitionType);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    }else{
                        context.startService(i);
                    }
                }
            }
        }

        private String print(ActivityTransitionEvent lastActivity) {
            String res = "> ";

            switch (lastActivity.getActivityType()){
                case 7:
                    res +=" Walking";
                    break;
                case 0:
                    res +=" On_Car";
                    break;
                case 1:
                    res +=" On_Bike";
                    break;
                default:
                    res+= " Unknown";
                    break;
            }

            if(lastActivity.getTransitionType() == 0 ){
                res+=" Started";
            }else{
                res+=" Finished";
            }
            return res;
        }
    }
}
