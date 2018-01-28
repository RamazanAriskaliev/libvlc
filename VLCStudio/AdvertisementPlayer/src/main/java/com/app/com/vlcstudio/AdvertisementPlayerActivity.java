package com.app.com.vlcstudio;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.videolan.libvlc.media.MainPlayer;

import java.util.ArrayList;

public class AdvertisementPlayerActivity extends AppCompatActivity{


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
    private boolean isLive;
    private String slotid = "52830107";
    private ScrollView scrollView;
    private LinearLayout ll1;
    private ArrayList<View> listimg;
    private boolean isList;
    private ImageView play_btn;
    private ImageView cbn_news_details_video_bg;
    private RelativeLayout relalayout;

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
        Log.i("isLock",mediaPlayer.isScreenLock()+"");
        if (mediaPlayer!=null&&!mediaPlayer.isScreenLock()){
            mediaPlayer.initVideoView();
            if (mLastTime != 0) {
                mediaPlayer.setTime(mLastTime);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRestart) {
            Log.i("onResume", "onResume");
            setContentView(R.layout.activity_vlc_player);
            TextView cbn_news_details_news_NewsTitle = (TextView) findViewById(R.id.cbn_news_details_news_NewsTitle);
            play_btn = (ImageView) findViewById(R.id.play_btn);
            cbn_news_details_video_bg = (ImageView) findViewById(R.id.cbn_news_details_video_bg);
            scrollView = (ScrollView) findViewById(R.id.video_scrollview);
            LinearLayout linearlayout_cbn_news_details_ad_01 = (LinearLayout) findViewById(R.id.linearlayout_cbn_news_details_ad_01);
            LinearLayout linearlayout_cbn_news_details_ad_02 = (LinearLayout) findViewById(R.id.linearlayout_cbn_news_details_ad_02);
            listimg = new ArrayList<View>();
            listimg.add(cbn_news_details_news_NewsTitle);
            listimg.add(linearlayout_cbn_news_details_ad_01);
            listimg.add(linearlayout_cbn_news_details_ad_02);

           /* videoUrl = "http://fmradio.smgtech.net:1935/live/977/playlist.m3u8";*/
            /*videoUrl = "rtmp://180.168.73.14/live/beijing";*/
            /*videoUrl = "http://a1.livecdn.yicai.com/hls/live/CBN_ld/live.m3u8";*/
            videoUrl = getIntent().getStringExtra("url");
            videoTitle = getIntent().getStringExtra("title");
            isVip = getIntent().getBooleanExtra("isVip", false);
            isLive = getIntent().getBooleanExtra("isLive", false);
            application = getApplication();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            //获取屏幕信息
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;

            videolayout = (RelativeLayout) findViewById(R.id.videolayout);
            //在这里调用者可以自己设置位置和大小，array_position数组前两个值是坐标绝对值，后两个是宽高
            array_position = new int[]{0, 0, screenWidth, screenWidth*9/16};
            //array_position = new int[]{20, 20, 900, 720};
            mediaPlayer = new MainPlayer(this, application);
            mediaPlayer.initMainPlayer(videolayout, scrollView, listimg, array_position, videoUrl, videoTitle, isVip, isLive,slotid);

            play_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.toPlay();
                    cbn_news_details_video_bg.setVisibility(View.GONE);
                    play_btn.setVisibility(View.GONE);
                }
            });
            cbn_news_details_news_NewsTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdvertisementPlayerActivity.this,Main2Activity.class));
                }
            });

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
        mLastTime = mediaPlayer.getPlayTime();
        Log.i("pause123", "pause123"+mLastTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("des123","des123");
        mLastTime = 0;
        mediaPlayer.observerDestory();
        mediaPlayer.releasePlayer();
        /*mHandler.sendEmptyMessage(111);*/
    }
   /*public static Handler mHandler = new Handler(new Handler.Callback() {
       @Override
       public boolean handleMessage(Message msg) {
           switch (msg.what){
               case 111:

           }
           return false;
       }
   });*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode ==KeyEvent.KEYCODE_BACK){
            mediaPlayer.backPress();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }
}
