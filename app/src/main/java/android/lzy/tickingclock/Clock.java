package android.lzy.tickingclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Clock extends AppCompatActivity {

    TextView Clock;
    int isDark = 0;
    Timer timer = new Timer();
    int impulseTimes = 0;
    int killedImpulse = 0;
    private static final String TAG = "Clock";
    public static Context mContext;
    private static final int time = 900000;
    TextView locUp;
    TextView locDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);
        LitePal.getDatabase();
        Clock = (TextView) findViewById(R.id.Clock);
        Clock.setText(getSystemTime());
        Clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDark == 0) {
                    changeAppBrightness(255);
                    isDark = 1;
                }
                else if (isDark == 1){
                    changeAppBrightness(127);
                    isDark = 2;
                }else {
                    changeAppBrightness(0);
                    isDark = 0;
                }
            }
        });
        Clock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Snackbar.make(v, "已添加", Snackbar.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        impulseTimes++;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {      // UI thread
                                    @Override
                                    public void run() {
                                        Vibrator vibrator = (Vibrator) Clock.this.getSystemService(Clock.this.VIBRATOR_SERVICE);
                                        vibrator.vibrate(1000);
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(Clock.this);
                                        dialog.setTitle("提示");
                                        dialog.setMessage("还想做吗?");
                                        dialog.setCancelable(false);
                                        dialog.setPositiveButton("想", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                impulse impulse = new impulse();
                                                SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
                                                Date nowDate = new Date(System.currentTimeMillis());
                                                impulse.setTime(df.format(nowDate));
                                                impulse.setSolved("false");
                                                impulse.save();
                                            }
                                        });
                                        dialog.setNegativeButton("不想", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                killedImpulse++;
                                                impulse impulse = new impulse();
                                                SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
                                                Date nowDate = new Date(System.currentTimeMillis());
                                                impulse.setTime(df.format(nowDate));
                                                impulse.setSolved("true");
                                                impulse.save();
                                            }
                                        });
                                        dialog.show();
                                    }
                                });
                            }
                        }, time);
                        Log.w(TAG, "run: " + impulseTimes);
                    }
                }).start();
                return true;
            }
        });
        registerReceiver(mTimeRefreshReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private BroadcastReceiver mTimeRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                Clock.setText(getSystemTime());
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mTimeRefreshReceiver);
        Clock.super.onDestroy();
    }

    //核对日期
    public static boolean verifyDate(String endDate){
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        Date nowDate = new Date(System.currentTimeMillis());
        Date endTimeDate = null;
        try {
            if (!endDate.isEmpty()){
                nowDate = df.parse(df.format(nowDate));
                endTimeDate = df.parse(endDate);
            }
        }catch (ParseException e){
            Toast.makeText(mContext, "发生错误, 请联系我们!", Toast.LENGTH_LONG).show();
        }
        if (nowDate.getTime() == endTimeDate.getTime()){
            return true;
        }else return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private String getSystemTime() {
        long sysTime = System.currentTimeMillis();
        SimpleDateFormat format=new SimpleDateFormat("HH:mm");
        Date d1 = new Date(sysTime);
        String t1 = format.format(d1);
        return t1;
    }

     public void changeAppBrightness(int brightness) {
         Window window = this.getWindow();
         WindowManager.LayoutParams lp = window.getAttributes();
         if (brightness == -1) {
             lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
         } else {
             lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
         }
         window.setAttributes(lp);
     }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(Clock.this);
        dialog.setTitle("提示");
        final List<impulse> impulses = DataSupport.where("solved = ?", "true").find(impulse.class);
        int size = impulses.size();
        int size1 = 0;
        for (int i = 0; i < size; i++){
            if (verifyDate(impulses.get(i).getTime())) size1++;
        }
        dialog.setMessage("共消灭了" + Integer.toString(size) + "个冲动!" + "\n" + "今天消灭了" + Integer.toString(size1) + "个冲动!");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Clock.super.onBackPressed();
            }
        });
        dialog.show();
    }
}
