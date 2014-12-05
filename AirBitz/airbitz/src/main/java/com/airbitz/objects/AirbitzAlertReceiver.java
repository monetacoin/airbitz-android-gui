/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted provided that
 * the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.objects;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.AirbitzAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*
 * Airbitz alerts are notifications of critical bug fixes or new businesses nearby
 * They are intended to be checked daily or weekly with a quick request to a server
 * If server not responsive in 2 seconds, the alarm is skipped
 */
public class AirbitzAlertReceiver extends BroadcastReceiver {
    final private String TAG = getClass().getSimpleName();

    private static final String TYPE = "com.airbitz.airbitalert.Type";

    public static final int ALERT_NOTIFICATION_CODE = 1;
    public static final String ALERT_NOTIFICATION_TYPE = "com.airbitz.airbitalert.NotificationType";
    final private static int REPEAT_NOTIFICATION_MILLIS = 1000 * 60 * 60 * 24; // 1 Day intervals

    public static final int ALERT_NEW_BUSINESS_CODE = 2;
    public static final String ALERT_NEW_BUSINESS_TYPE = "com.airbitz.airbitalert.NewBusinessType";
    final private static int REPEAT_NEW_BUSINESS_MILLIS = 1000 * 60 * 60 * 1; //24 * 7; // 1 week intervals

    NotificationTask mNotificationTask;
    NewBusinessTask mNewBusinessTask;
    PowerManager.WakeLock mWakeLock;
    Handler mHandler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getString(R.string.app_name));
        //Acquire the lock
        mWakeLock.acquire();

        String type = intent.getStringExtra(TYPE);
        mHandler.postDelayed(murderPendingTasks, 2000); // 2 second TTL

        if(type.equals(ALERT_NOTIFICATION_TYPE)) {
            mNotificationTask = new NotificationTask(context);
            mNotificationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else if(type.equals(ALERT_NEW_BUSINESS_TYPE)) {
            mNewBusinessTask = new NewBusinessTask(context);
            mNewBusinessTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    final Runnable murderPendingTasks = new Runnable() {
        @Override
        public void run() {
            if(mNotificationTask != null && !mNotificationTask.isCancelled()) {
                mNotificationTask.cancel(true);
            }
            if(mNewBusinessTask != null && !mNewBusinessTask.isCancelled()) {
                mNewBusinessTask.cancel(true);
            }
            //Release the lock
            if(mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    };


    public static void SetRepeatingAlertAlarm(Context context, int code)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AirbitzAlertReceiver.class);
        PendingIntent pi;
        if(code==ALERT_NOTIFICATION_CODE) {
            intent.putExtra(TYPE, ALERT_NOTIFICATION_TYPE);
            pi = PendingIntent.getBroadcast(context, ALERT_NOTIFICATION_CODE, intent, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    REPEAT_NOTIFICATION_MILLIS, pi);
        }
        else if(code==ALERT_NEW_BUSINESS_CODE) {
            intent.putExtra(TYPE, ALERT_NEW_BUSINESS_TYPE);
            pi = PendingIntent.getBroadcast(context, ALERT_NEW_BUSINESS_CODE, intent, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    REPEAT_NEW_BUSINESS_MILLIS, pi);
        }
    }

    public static void CancelNextAlertAlarm(Context context, int code)
    {
        if(code != ALERT_NOTIFICATION_CODE && code != ALERT_NEW_BUSINESS_CODE) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AirbitzAlertReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, code, intent, 0);
        alarmManager.cancel(sender);
    }

    private void issueOSNotification(Context context, String type) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Airbitz notification")
                .setSmallIcon(R.drawable.ic_launcher);
        Intent resultIntent = new Intent(context, NavigationActivity.class);

        int code = 0;
        String message = "no setting";
        if(type.equals(ALERT_NOTIFICATION_TYPE)) {
            message = context.getString(R.string.alert_notification_message);
            code = ALERT_NOTIFICATION_CODE;
        }
        else if(type.equals(ALERT_NEW_BUSINESS_TYPE)) {
            message = context.getString(R.string.alert_new_business_message);
            code = ALERT_NEW_BUSINESS_CODE;
        }
        builder.setContentText(message);
        resultIntent.setType(type);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                        code, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(code, builder.build());
    }

    public class NotificationTask extends AsyncTask<Void, Void, String> {
        String mMessageId;
        String mBuildNumber;
        Context mContext;

        NotificationTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            AirbitzAPI api = AirbitzAPI.getApi();
            PackageInfo pInfo;
            try {
                pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            mMessageId = String.valueOf(getMessageIDPref(mContext));

//            mBuildNumber = "2014111801"; // TESTING
            mBuildNumber = String.valueOf(pInfo.versionCode);

            return api.getMessages(mMessageId, mBuildNumber);
        }

        @Override
        protected void onPostExecute(final String response) {
            Log.d(TAG, "Notification response of " + mMessageId + "," + mBuildNumber + ": " + response);
            if(response != null && response.length() != 0) {
                if(hasAlerts(response)) {
                    issueOSNotification(mContext, ALERT_NOTIFICATION_TYPE);
                }
            }
            mHandler.removeCallbacks(murderPendingTasks);
            mHandler.post(murderPendingTasks);
        }
     }

    private int getMessageIDPref(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(NavigationActivity.LAST_MESSAGE_ID, 0); // default to Automatic
    }

    private boolean hasAlerts(String input) {
        try {
            JSONObject json = new JSONObject(input);
            int count = json.getInt("count");
            if(count > 0) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public class NewBusinessTask extends AsyncTask<Void, Void, String> {
        Context mContext;

        NewBusinessTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            AirbitzAPI api = AirbitzAPI.getApi();
            // format is ISO8601, ex: 2014-09-25T01:42:03.000Z
            // get one week ago
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) - 1);
            Date date = new Date(cal.getTimeInMillis());

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            String weekAgo = df.format(date);
            Log.d(TAG, "WeekAgo: " + weekAgo);

            return api.getNewBusinesses(weekAgo);
        }

        @Override
        protected void onPostExecute(final String response) {
//            Log.d(TAG, "New Business response: "+response);
            if(response != null && response.length() != 0) {
                if(hasAlerts(response)) {
                    issueOSNotification(mContext, ALERT_NEW_BUSINESS_TYPE);
                }
            }
            mHandler.removeCallbacks(murderPendingTasks);
            mHandler.post(murderPendingTasks);
        }
    }

}