package com.app.com.vlcstudio;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.videolan.libvlc.media.MainPlayer;

import java.util.ArrayList;

public class AudioPlayerActivity extends AppCompatActivity{


    private static MainPlayer mediaPlayer=null;
    private RelativeLayout videolayout;
    private ArrayList<String> list_adurl;
    private String videoUrl;
    private String videoTitle;
    private int screenWidth;
    private int screenHeight;
    private int[] array_position;
    private Application application;
    private boolean isVip;
    private boolean isRestart=false;
    private long mLastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持播放器界面长亮状态
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isRestart = true;
        /*if (mediaPlayer!=null&&!mediaPlayer.isScreenLock()){

        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRestart) {
            setContentView(R.layout.activity_audio_player);
           /* videoUrl = "http://fmradio.smgtech.net:1935/live/977/playlist.m3u8";*/
            videoUrl = getIntent().getStringExtra("url");
            videoTitle = getIntent().getStringExtra("title");
            isVip = getIntent().getBooleanExtra("isVip", false);
            application = getApplication();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            //获取屏幕信息
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;

            videolayout = (RelativeLayout) findViewById(R.id.videolayout);
            //在这里调用者可以自己设置位置和大小，array_position数组前两个值是坐标绝对值，后两个是宽高
            array_position = new int[]{0, 0, screenWidth, 9 * screenWidth / 16};
            //array_position = new int[]{20, 20, 900, 720};
            mediaPlayer = new MainPlayer(this, application);
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.audiobackimg);
            mediaPlayer.initAudioPlayer(videolayout,array_position,videoUrl,drawable);
        }
        isRestart = false;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.audioPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.audioReleasePlayer();
    }
}
