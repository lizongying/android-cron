package com.lizongying.cron;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Calendar;

import static com.lizongying.cron.MainActivity.MESSENGER_INTENT_KEY;
import static com.lizongying.cron.MainActivity.MSG_JOB_ERROR;
import static com.lizongying.cron.MainActivity.MSG_JOB_START;
import static com.lizongying.cron.MainActivity.MSG_JOB_STOP;
import static com.lizongying.cron.MainActivity.MSG_JOB_SUCCESS;


public class MyJobService extends JobService {
    private static final String TAG = MyJobService.class.getSimpleName();

    private Messenger mActivityMessenger;

    private JobParameters mJobParameters;


    private static boolean isBelong() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return (hour == 10)
                ||
                (19 < hour);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        mJobParameters = params;

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Void> mTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                sendMessage(MSG_JOB_START, mJobParameters.getJobId());
//                if (!isBelong()) {
//                    return null;
//                }
                try {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.setClassName("com.client.xrxs.com.xrxsapp", "com.client.xrxs.com.xrxsapp.activity.WelcomeActivity");
                    intent.setClassName("com.example.noandroid", "com.example.noandroid.MainActivity");
                    startActivity(intent);
                    sendMessage(MSG_JOB_SUCCESS, mJobParameters.getJobId());
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    sendMessage(MSG_JOB_ERROR, mJobParameters.getJobId());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                jobFinished(mJobParameters, true);
                sendMessage(MSG_JOB_STOP, mJobParameters.getJobId());
                super.onPostExecute(result);
            }
        };
        mTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessage(MSG_JOB_STOP, params.getJobId());
        return false;
    }

    private void sendMessage(int messageID, @Nullable Object params) {
        if (mActivityMessenger == null) {
            System.out.println(messageID);
            Log.i(TAG, getString(R.string.server_background));
            return;
        }
        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, getString(R.string.server_error));
        }
    }
}