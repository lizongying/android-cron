package com.lizongying.cron;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_JOB_START = 0;
    public static final int MSG_JOB_STOP = 1;
    public static final int MSG_JOB_SUCCESS = 2;
    public static final int MSG_JOB_ERROR = 3;
    private static final String TAG = "MainActivity";
    public static final String MESSENGER_INTENT_KEY = TAG + ".MESSENGER_INTENT_KEY";
    public static String WIFI = "";
    ComponentName mServiceComponent;
    private int mJobId = 0;
    private IncomingMessageHandler mHandler;
    private FloatingActionButton fab;
    private boolean running = false;
    private long JOB_INTERVAL = 15 * 60 * 1000L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new IncomingMessageHandler(this);
        mServiceComponent = new ComponentName(this, MyJobService.class);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = !running;
                if (running) {
                    fab.setImageResource(android.R.drawable.button_onoff_indicator_on);
                    scheduleJob();
                } else {
                    fab.setImageResource(android.R.drawable.button_onoff_indicator_off);
                    cancelJob();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent startServiceIntent = new Intent(this, MyJobService.class);
        Messenger messengerIncoming = new Messenger(mHandler);
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
        startService(startServiceIntent);
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, MyJobService.class));
        super.onStop();
    }

    public void scheduleJob() {
        JobInfo.Builder builder = new JobInfo.Builder(mJobId, mServiceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(JOB_INTERVAL);
        builder.setPersisted(true);
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert mJobScheduler != null;
        mJobScheduler.schedule(builder.build());
        Toast.makeText(MainActivity.this, R.string.job_scheduled, Toast.LENGTH_LONG).show();
    }

    public void cancelJob() {
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert mJobScheduler != null;
        mJobScheduler.cancel(mJobId);
        Toast.makeText(MainActivity.this, R.string.job_canceled, Toast.LENGTH_LONG).show();
    }

    /**
     * {@link Handler}允许您发送与线程相关联的消息。
     * {@link Messenger}使用此处理程序从{@link MyJobService}进行通信。
     * 它也用于使开始和停止视图在短时间内闪烁。
     */
    private static class IncomingMessageHandler extends Handler {

        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super();
            this.mActivity = new WeakReference<>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            final MainActivity mSchedulerActivity = mActivity.get();
            if (mSchedulerActivity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_JOB_SUCCESS:
                    Log.e(TAG, mSchedulerActivity.getString(R.string.job_success));
                    Toast.makeText(mSchedulerActivity, R.string.job_success, Toast.LENGTH_LONG).show();
                    break;
                case MSG_JOB_ERROR:
                    Log.e(TAG, mSchedulerActivity.getString(R.string.job_error));
                    Toast.makeText(mSchedulerActivity, R.string.job_error, Toast.LENGTH_LONG).show();
                    break;
                case MSG_JOB_START:
                    Log.i(TAG, mSchedulerActivity.getString(R.string.job_started));
                    Toast.makeText(mSchedulerActivity, R.string.job_started, Toast.LENGTH_LONG).show();
                    break;
                case MSG_JOB_STOP:
                    Log.i(TAG, mSchedulerActivity.getString(R.string.job_finished));
                    Toast.makeText(mSchedulerActivity, R.string.job_finished, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }
}
