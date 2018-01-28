package org.videolan.libvlc.media;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.R;
import org.videolan.libvlc.receiver.ScreenObserver;
import org.videolan.libvlc.util.Constants;
import org.videolan.libvlc.util.DateUtil;
import org.videolan.libvlc.util.LogUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lsn on 2016/3/21.
 */
public class VideoViewLivelayout extends FrameLayout implements ScreenObserver.ScreenStateListener{
    private static VideoLiveView mVideoView;//视频播放窗口
    private static Context mContext;//上下文资源
    private static boolean isShowReplay = false;
    public static boolean hasReleaseLive = false;
    private Application mApplication;//应用application
    private Uri contentUri;//播放地址uri
    private Activity activity;//activity实例
    public static org.videolan.libvlc.MediaPlayer mMediaPlayer; //vlc播放器实例
    private static LibVLC mLibVLC;//vlc多媒体资源接口
    private static IVLCVout ivlcVout;//视频窗口大小回调监听
    private Media mCurrentMedia = null;//vlc多媒体资源
    private static int[] mPosition;//播放器位置大小
    private FrameLayout.LayoutParams mVideoParams,mBottomParams;//缩放屏幕时的播放窗口布局参数
    private String mUrl, mTitle;//视频地址，视频名
    private TextView mTimeText, mTitleText,mSlideCurTime,mSlideTolTime;
    private static TextView mVoiceText;//显示声音大小,播放的当前时间和总时间
    private static Button mPlayBtn;//播放暂停按钮
    private static SeekBar mVoiceBar;//视频播放进度条,声音进度条
    private Button mFullBtn;//全屏按钮
    private static LinearLayout mVoiceSlideLayout;//滑动时显示的声音布局
    private ImageView mVoiceImg, mLockImg,mBackImg,mCancleImg,mSlideImg;//声音图，锁屏图,返回按钮图，广告关闭按钮
    private int mTopHeight,mBottomHeight;//播放器上下工具布局
    public boolean isLock = false, isFullScreen = false, isToolsHide = false; //是否锁屏，是否全屏,工具栏是否隐藏
    private static ProgressBar mLoadingIcon;//加载中progressbar
    private LinearLayout mBottomLayout, mTopLayout;//工具栏布局
    private Timer mTimer;
    private static MyTimerTask mTimerTask;//定时器
    public static float mScrollIndexVol, mVolume = -1;//当前声音
    private static int mScreenHeight, mScreenWidth, mMaxVolume;
    private static AudioManager mAudioManager;//系统声音控制
    private GestureDetector mGestureDetector;//手势监听
    private static long mTotalTime;//播放总时长
    private static FrameLayout adLayout;
    private FrameLayout loopLayout;//广告外层布局，广告根布局
    private String slotId;//广告id
    private ViewPager viewpager;//广告布局里的viewpager
    private static LinearLayout mVideoSlideLayout;
    private LinearLayout mVideoSlideTxtLayout;
    private float scrollIndexMov = -1;
    private boolean isVip;
    private boolean isReZhuan = false;
    public  boolean isInZhuan = false;//是否是会员,是否重新计算转屏，是否正在转屏
    private int fullScreenWidth,fullScreenHeight;
    private static long mVideo = -1;//当前播放进度
    private static boolean isPause = true,isBackOrHome = false;//是否是暂停状态,是否是切换回来的
    protected boolean isScreenLock = false;//是否锁屏
    private ScreenObserver observer;//观察者，监听是否锁屏
    private static LogUtil log;//打印Log工具类
    private static long mLastExitTimem,mCurTime;//上次切换离开播放器的时间
    private static OrientationEventListener mOrientationListener;//屏幕旋转监听
    public boolean mScreenProtrait = true,mCurrentOrient = false;//屏幕旋转判断参数
    private static boolean isProgressing;
    private LayoutParams mTopParam;
    private boolean isfirst = false;
    private boolean isSmall = true;
    private static boolean surfaceCreated;

    /* 构造方法 */
    public VideoViewLivelayout(Context context, Application application, org.videolan.libvlc.MediaPlayer player, LibVLC libvlc, String videoUrl, String videoTitle, int[] array_position, boolean isVip,String slotId) {
        super(context);
        this.mContext = context;
        this.mApplication = application;
        this.mMediaPlayer = player;
        this.mLibVLC = libvlc;
        this.mUrl = videoUrl;
        this.mTitle = videoTitle;
        this.mPosition = array_position;
        this.isVip = isVip;
        this.slotId = slotId;
        activity = (Activity) mContext;
        contentUri = Uri.parse(mUrl);
        log = LogUtil.getInstance();
        log.i("url", videoUrl);
        init();
    }

    public VideoViewLivelayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
    * 初始化参数及布局
    */
    private void init() {
        if (mContext.getResources().getConfiguration().orientation==2){
            isfirst = true;
        }
        initObserver();
        getMaxVolume();
        log.i("orention", mContext.getResources().getConfiguration().orientation + "");
        getScreenInfo();
        initVideoView();
        initListener();
        new TimeThread().start();
        new VoiceThread().start();
        mTimer = new Timer(true);
    }

    /*
    * 初始化锁屏监听
    */
    private void initObserver() {
        observer = new ScreenObserver(mContext);
        observer.startObserver(this);
    }

    /*
    * 获取声音最大值
    */
    private void getMaxVolume() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /*
    * 根据屏幕方向获取屏幕宽高参数
    */
    private void getScreenInfo() {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        log.i("initsmall", mScreenWidth + "," + mScreenHeight + "," + mPosition[2] + "," + mPosition[3]);
        fullScreenWidth = dm.heightPixels;
        fullScreenHeight = dm.widthPixels;
    }
    /*
    * 初始化播放窗口控件VideoView及播放布局
    */
    public void initVideoView(){
        log.i("initVideoView", "initVideoView");
        mVideoView = new VideoLiveView(mContext);
        mVideoParams = new FrameLayout.LayoutParams(mPosition[2], mPosition[3]);
        mVideoParams.leftMargin = mPosition[0];
        mVideoParams.topMargin = mPosition[1];
        mVideoView.setLayoutParams(mVideoParams);
        this.setLayoutParams(mVideoParams);
        this.addView(mVideoView);
        try {
            setDataSource(mContext, contentUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDisplay();
        initToolLayout();
    }

    /*
    * 设置vlc播放资源uri
    */
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mCurrentMedia = new Media(mLibVLC, uri);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    /*
    * vlc播放窗口关联及监听回调，播放器播放事件回调设置
    */
    public void setDisplay() {
        if (mMediaPlayer != null) {
            ivlcVout = mMediaPlayer.getVLCVout();
            ivlcVout.setVideoView(mVideoView);
            ivlcVout.attachViews();
            ivlcVout.addCallback(callback);
            mMediaPlayer.setEventListener(mPlayerListener);
        }
    }

    /*
    * 由其他位置切换回视频播放时据方向重新初始化VideoView布局大小
    */
    public void reinitVideoView(){
        log.i("reinitVideoView",mContext.getResources().getConfiguration().orientation+"");
        if (mContext.getResources().getConfiguration().orientation == 2) {
            //返回时全屏时初始化VideoView
            mVideoParams = new FrameLayout.LayoutParams(fullScreenWidth, fullScreenHeight);
            mVideoView.setLayoutParams(mVideoParams);
        } else if (mContext.getResources().getConfiguration().orientation == 1) {
            //返回时小屏时初始化VideoView
            mVideoParams = new FrameLayout.LayoutParams(mPosition[2], mPosition[3]);
            mVideoParams.leftMargin = mPosition[0];
            mVideoParams.topMargin = mPosition[1];
            mVideoView.setLayoutParams(mVideoParams);
        }
        ivlcVout = mMediaPlayer.getVLCVout();
        ivlcVout.setVideoView(mVideoView);
        ivlcVout.attachViews();
        ivlcVout.addCallback(callback);
    }

    /*
    * 初始化播放器布局
    */
    private void initToolLayout() {
        mTopHeight =  mPosition[2]/ 10;
        mBottomHeight = mPosition[2] / 8;
        //顶部工具栏
        mTopLayout = new LinearLayout(mContext);
        mTopLayout.setBackgroundColor(Color.BLACK);
        mTopLayout.getBackground().setAlpha(153);
        mTopParam = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, mTopHeight);
        mTopParam.leftMargin = mPosition[0];
        mTopParam.topMargin = mPosition[1];
        mTopParam.gravity = Gravity.TOP;
        mTopLayout.setLayoutParams(mTopParam);
        mBackImg = new ImageView(mContext);
        mBackImg.setBackgroundResource(R.drawable.fullback);
        LinearLayout.LayoutParams llParamsback = new LinearLayout.LayoutParams(mTopHeight*2/3, mTopHeight*3/4);
        llParamsback.leftMargin = mTopHeight/2;
        llParamsback.gravity = Gravity.CENTER_VERTICAL;
        mBackImg.setLayoutParams(llParamsback);
        mTitleText = new TextView(mContext);
        LinearLayout.LayoutParams llParams0 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 8.5f);
        llParams0.gravity = Gravity.CENTER_VERTICAL;
        llParams0.leftMargin = 30;
        mTitleText.setLayoutParams(llParams0);
        mTitleText.setText(mTitle);
        mTimeText = new TextView(mContext);
        LinearLayout.LayoutParams llParams1 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        llParams1.gravity = Gravity.CENTER_VERTICAL;
        llParams1.rightMargin = 50;
        mTimeText.setLayoutParams(llParams1);
        mTimeText.setGravity(Gravity.RIGHT);
        mTitleText.setTextColor(Color.WHITE);
        mTimeText.setTextColor(Color.WHITE);
        mTopLayout.setOrientation(LinearLayout.HORIZONTAL);
        mTopLayout.addView(mBackImg);
        mTopLayout.addView(mTitleText);
        mTopLayout.addView(mTimeText);
        this.addView(mTopLayout);
        mTimeText.setVisibility(View.GONE);


        //底部工具栏
        LinearLayout.LayoutParams llParams2 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.5f);
        llParams2.leftMargin = 30;
        mBottomLayout = new LinearLayout(mContext);
        mBottomLayout.setBackgroundColor(Color.BLACK);
        mBottomLayout.getBackground().setAlpha(153);
        mBottomParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, mBottomHeight);
        mBottomParams.gravity = Gravity.BOTTOM;
        mBottomParams.leftMargin = mPosition[0];
        mBottomLayout.setLayoutParams(mBottomParams);

        LinearLayout mPlayBtnLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams imgparam = new LinearLayout.LayoutParams(mBottomHeight + 10, mBottomHeight - 20);
        imgparam.gravity = Gravity.CENTER_VERTICAL;
        mPlayBtnLayout.setLayoutParams(llParams2);
        mPlayBtn = new Button(mContext);
        mPlayBtn.setLayoutParams(imgparam);
        mPlayBtn.setBackgroundResource(R.drawable.pause);

        LinearLayout mCurrentTimeLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams txtparam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, mBottomHeight - 20);
        txtparam.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        mPlayBtnLayout.setLayoutParams(llParams2);
        TextView mCurrentTimeText = new TextView(mContext);
        mCurrentTimeText.setLayoutParams(txtparam);
        mCurrentTimeText.setGravity(Gravity.CENTER);
        mCurrentTimeText.setTextColor(Color.WHITE);
        mCurrentTimeText.setText("");
        mCurrentTimeText.setVisibility(INVISIBLE);

        LinearLayout mTotalTimeLayout = new LinearLayout(mContext);
        mPlayBtnLayout.setLayoutParams(llParams2);
        TextView mTotalTimeText = new TextView(mContext);
        mTotalTimeText.setLayoutParams(txtparam);
        mTotalTimeText.setGravity(Gravity.CENTER);
        mTotalTimeText.setTextColor(Color.WHITE);
        mTotalTimeText.setText("");
        mTotalTimeText.setVisibility(INVISIBLE);

        LinearLayout.LayoutParams seekparam = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5.0f);
        SeekBar mSeekBar = new SeekBar(mContext);
        seekparam.gravity = Gravity.CENTER_VERTICAL;
        mSeekBar.setLayoutParams(seekparam);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
        mSeekBar.setEnabled(true);
        mSeekBar.setVisibility(INVISIBLE);

        LinearLayout mFullBtnLayout = new LinearLayout(mContext);
        mPlayBtnLayout.setLayoutParams(llParams2);
        mFullBtn = new Button(mContext);
        mFullBtn.setLayoutParams(imgparam);
        mFullBtn.setBackgroundResource(R.drawable.expend);

        mBottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        mBottomLayout.addView(mPlayBtn);
        mBottomLayout.addView(mCurrentTimeText);
        mBottomLayout.addView(mSeekBar);
        mBottomLayout.addView(mTotalTimeText);
        mBottomLayout.addView(mFullBtn);

        mBottomLayout.setGravity(SCROLL_INDICATOR_BOTTOM);
        this.addView(mBottomLayout);

        //滑动的声音布局
        LinearLayout.LayoutParams linearparam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mVoiceSlideLayout = new LinearLayout(mContext);
        FrameLayout.LayoutParams voiceparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        voiceparams.gravity = Gravity.CENTER;
        mVoiceSlideLayout.setLayoutParams(voiceparams);
        mVoiceSlideLayout.setOrientation(LinearLayout.VERTICAL);
        mVoiceText = new TextView(mContext);
        mVoiceText.setLayoutParams(linearparam);
        mVoiceText.setText("0");
        mVoiceText.setTextSize(24);
        mVoiceImg = new ImageView(mContext);
        mVoiceImg.setLayoutParams(new LinearLayout.LayoutParams(mScreenWidth / 9, mScreenWidth / 9));
        mVoiceImg.setBackgroundResource(R.drawable.slidevoiceimg);
        mVoiceSlideLayout.addView(mVoiceText);
        mVoiceSlideLayout.addView(mVoiceImg);
        this.addView(mVoiceSlideLayout);
        mVoiceSlideLayout.setVisibility(View.GONE);

        //右侧声音seekbar
        int mVoiceBarHeight = (mScreenWidth - 140) * 2 / 3;
        log.i("mVoiceBarHeight", mVoiceBarHeight + "," + mPosition[3]);
        FrameLayout.LayoutParams voiceBarparam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mScreenWidth / 2);
        voiceBarparam.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        voiceBarparam.leftMargin = 50;
        mVoiceBar = new VerticalLiveSeek(mContext);
        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.seekbtn);
        mVoiceBar.setThumb(drawable);
        mVoiceBar.setLayoutParams(voiceBarparam);
        mVoiceBar.setMax(mMaxVolume);
        mVoiceBar.setOnSeekBarChangeListener(new OnmVoiceBarChangeListenerImp());
        mVoiceBar.setEnabled(true);
        this.addView(mVoiceBar);
        mVoiceBar.setVisibility(View.GONE);

        //进度滑动布局
        FrameLayout.LayoutParams videoslideparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoslideparams.gravity = Gravity.CENTER;
        mVideoSlideLayout = new LinearLayout(mContext);
        mVideoSlideLayout.setLayoutParams(videoslideparams);
        mVideoSlideLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams videotxtparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videotxtparams.gravity = Gravity.CENTER_HORIZONTAL;
        mVideoSlideTxtLayout = new LinearLayout(mContext);
        mVideoSlideTxtLayout.setTop(24);
        mVideoSlideTxtLayout.setOrientation(LinearLayout.HORIZONTAL);
        mVideoSlideTxtLayout.setLayoutParams(videotxtparams);

        mSlideCurTime = new TextView(mContext);
        mSlideCurTime.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSlideCurTime.setTextColor(Color.WHITE);
        mSlideCurTime.setText("00:00");
        mSlideCurTime.setTextSize(24);

        TextView slidetxt = new TextView(mContext);
        slidetxt.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        slidetxt.setTextColor(Color.WHITE);
        slidetxt.setText("/");
        slidetxt.setTextSize(24);

        mSlideTolTime = new TextView(mContext);
        mSlideTolTime.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSlideTolTime.setTextColor(Color.WHITE);
        mSlideTolTime.setText("00:00");
        mSlideTolTime.setTextSize(24);
        mVideoSlideTxtLayout.addView(mSlideCurTime);
        mVideoSlideTxtLayout.addView(slidetxt);
        mVideoSlideTxtLayout.addView(mSlideTolTime);
        mVideoSlideTxtLayout.setTop(50);

        LinearLayout.LayoutParams slideingparam = new LinearLayout.LayoutParams(mScreenHeight / 12, mScreenWidth / 12);
        slideingparam.gravity = Gravity.CENTER_HORIZONTAL;
        mSlideImg = new ImageView(mContext);
        mSlideImg.setLayoutParams(slideingparam);
        mSlideImg.setBackgroundResource(R.drawable.player_slidego);

        mVideoSlideLayout.addView(mSlideImg);
        mVideoSlideLayout.addView(mVideoSlideTxtLayout);
        this.addView(mVideoSlideLayout);
        mVideoSlideLayout.setVisibility(View.GONE);

        //缓冲时progressbar
        FrameLayout.LayoutParams loadingparam = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        loadingparam.gravity = Gravity.CENTER;
        mLoadingIcon = new ProgressBar(mContext);
        mLoadingIcon.setLayoutParams(loadingparam);
        this.addView(mLoadingIcon);
        isProgressing = true;

        //屏幕锁
        FrameLayout.LayoutParams lockparam = new FrameLayout.LayoutParams(mScreenWidth / 9, mScreenWidth / 9);
        lockparam.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lockparam.rightMargin = 50;
        mLockImg = new ImageView(mContext);
        mLockImg.setLayoutParams(lockparam);
        mLockImg.setBackgroundResource(R.drawable.unlock);
        this.addView(mLockImg);
        mLockImg.setVisibility(View.GONE);


        //暂停时广告
        if (!isVip) {
            FrameLayout.LayoutParams adparam = new FrameLayout.LayoutParams(mScreenHeight * 2 / 3 + mScreenHeight * 2 / 48, mScreenWidth * 2 / 3 + mScreenHeight * 2 / 48);
            FrameLayout.LayoutParams loopparam = new FrameLayout.LayoutParams(mScreenHeight * 2 / 3, mScreenWidth * 2 / 3);
            loopparam.gravity = Gravity.CENTER;
            adparam.gravity = Gravity.CENTER;
            loopLayout = new FrameLayout(mContext);
            loopLayout.setLayoutParams(loopparam);
            adLayout = new FrameLayout(mContext);
            viewpager = new android.support.v4.view.ViewPager(mContext);
            viewpager.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mCancleImg = new ImageView(mContext);
            FrameLayout.LayoutParams cancleparam = new FrameLayout.LayoutParams(mScreenWidth / 10, mScreenWidth / 10);
            cancleparam.gravity = Gravity.RIGHT | Gravity.TOP;
            mCancleImg.setLayoutParams(cancleparam);
            mCancleImg.setBackgroundResource(R.drawable.cancle);
            adLayout.setLayoutParams(adparam);
            loopLayout.addView(viewpager);
            adLayout.addView(loopLayout);
            adLayout.addView(mCancleImg);
            this.addView(adLayout);
            adLayout.setVisibility(View.GONE);
        }

    }

    /*
    * 初始化控件监听
    */
    private void initListener() {
        mGestureDetector = new GestureDetector(mContext, new MyGestureListener());
        startOrientationChangeListener();
        //播放暂停按钮点击事件
        mPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPause) {
                    mPlayBtn.setBackgroundResource(R.drawable.play);
                    log.i("ispuase", isPause + "");
                    mMediaPlayer.stop();
                    isPause = true;
                } else {
                    mPlayBtn.setBackgroundResource(R.drawable.pause);
                    mMediaPlayer.play();
                    if (!isVip) {
                        adLayout.setVisibility(View.GONE);
                    }
                    isPause = false;
                }
            }
        });

        //全屏按钮点击事件
        mFullBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFullScreen) {
                    mCurrentOrient = true;
                    if (mCurrentOrient != mScreenProtrait) {
                        mScreenProtrait = mCurrentOrient;
                    }
                    isInZhuan = true;
                    mHandler.sendEmptyMessage(Constants.SCREENSMALL);
                } else {
                    mCurrentOrient = false;
                    if (mCurrentOrient != mScreenProtrait) {
                        mScreenProtrait = mCurrentOrient;
                    }
                    isReZhuan=false;
                    isInZhuan = true;
                    mHandler.sendEmptyMessage(Constants.SCREENFULL);
                }
            }
        });
        //锁频按钮点击事件
        mLockImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLock) {
                    mLockImg.setBackgroundResource(R.drawable.unlock);
                    isLock = false;
                } else {
                    mLockImg.setBackgroundResource(R.drawable.lock);
                    isLock = true;
                }
            }
        });
        //绑定点击事件, 控制显示隐藏controller
        mVideoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isHideTools();
            }
        });
        mVideoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mGestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                return false;
            }
        });
        //全屏时返回按钮
        mBackImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {
                    mHandler.sendEmptyMessage(Constants.SCREENSMALL);
                }else{
                    activity.finish();
                }
            }
        });
        if (!isVip) {
            //广告取消按钮
            mCancleImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    adLayout.setVisibility(View.GONE);
                }
            });
        }
    }


    /*
        * 播放窗口绑定vlc后的回调
        */
    static IVLCVout.Callback callback = new IVLCVout.Callback() {
        @Override
        public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

        }

        @Override
        public void onSurfacesCreated(IVLCVout vlcVout) {
            log.i("SurfaceCreate", "mLastExitTimem=" + mLastExitTimem + "," + isPause);
            surfaceCreated = true;
            if (!isPause){
                mMediaPlayer.play();
            }
            isBackOrHome = false;
            mMediaPlayer.setTime(mLastExitTimem);
        }

        @Override
        public void onSurfacesDestroyed(IVLCVout vlcVout) {

            log.i("SurfacesDestroyed", "SurfacesDestroyed");
            if (!isPause){
                mMediaPlayer.stop();
            }else{
                adLayout.setVisibility(View.GONE);
            }
            ivlcVout.detachViews();
            isBackOrHome = true;
        }
    };

    /*
    * vlc播放器各种事件的监听
    */
    private static org.videolan.libvlc.MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(mContext);
    private static class MyPlayerListener implements org.videolan.libvlc.MediaPlayer.EventListener {
        //private WeakReference mOwner;
        Context context;

        public MyPlayerListener(Context context) {
            this.context = context;
        }

        @Override
        public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {
            switch (event.type) {
                case org.videolan.libvlc.MediaPlayer.Event.Playing:
                    log.i("Event1", "MediaPlayerPlaying");
                    isShowReplay = false;
                    isProgressing = false;
                    mLoadingIcon.setVisibility(View.GONE);
                    mPlayBtn.setVisibility(View.VISIBLE);
                    mTotalTime = mMediaPlayer.getLength();
                    break;
                case org.videolan.libvlc.MediaPlayer.Event.Buffering:
                    log.i("Event2", "MediaPlayerBuffering");
                    mLoadingIcon.setVisibility(View.VISIBLE);
                    break;
                case org.videolan.libvlc.MediaPlayer.Event.Paused:
                    log.i("Event3", "MediaPlayerPaused");
                    break;
                case org.videolan.libvlc.MediaPlayer.Event.Stopped:
                    log.i("Event4", "MediaPlayerStopped");
                    break;
                case org.videolan.libvlc.MediaPlayer.Event.EndReached:

                    break;
                default:
                    break;
            }
        }
    }

    /*
       *播放事件
       */
    public void toPlayLive() {
        if (mMediaPlayer!=null&&surfaceCreated){
            mMediaPlayer.play();
            isPause = false;
            if (mOrientationListener!= null) {
                mOrientationListener.enable();
            }
        }
    }
    /*
    * 重新播放
    */
    private void initReplay() {
        mLibVLC = new LibVLC(); //FIXME, this is wrong
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mLibVLC);
        try {
            setDataSource(mContext, Uri.parse(mUrl));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDisplay();
    }

    /*
    * 视频进度条变化
    */
    private class OnSeekBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {

        private int progress;

        // 触发操作，拖动
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            this.progress = progress;
        }

        // 表示进度条刚开始拖动，开始拖动时候触发的操作
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        // 停止拖动时候
        public void onStopTrackingTouch(SeekBar seekBar) {
            mMediaPlayer.setTime((long) progress);
            VideoViewLivelayout.mHandlerLiveSeekbar.sendEmptyMessage(Constants.VIDEOBARSTOP);
        }
    }

    /*
    * 声音垂直进度条变化
    */
    private class OnmVoiceBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {
        private int progress;
        // 触发操作，拖动
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            log.i("VoiceProgress", "progress="+progress);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            mVoiceBar.setProgress(progress);
            this.progress = progress;
        }

        // 表示进度条刚开始拖动，开始拖动时候触发的操作
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        // 停止拖动时候
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    /*
    * 更新系统时间
    */
    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = Constants.UPDATETIME;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    /*
    * 更新声音进度条
    */
    public class VoiceThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(500);
                    Message msg = new Message();
                    msg.what = Constants.VOICECHANGE;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    /*
    * 定时隐藏播放布局工具条
    */
    public void StartLockWindowTimer() {
        if (mTimer != null) {
            if (mTimerTask != null) {
                mTimerTask.cancel();  //将原任务从队列中移除
            }
            mTimerTask = new MyTimerTask();  // 新建一个任务
            mTimer.schedule(mTimerTask, 5000);
        }
    }

    /*
     * 定时器任务
     */
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message msg = mHandler.obtainMessage(Constants.HIDETOOLS);
            msg.sendToTarget();
        }

    }

    /*
         * handler处理多任务
         */
    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.UPDATETIME:
                    long sysTime = System.currentTimeMillis();
                    mCurTime = 0;
                    if (mMediaPlayer != null) {
                        mCurTime = mMediaPlayer.getTime();
                    }
                    CharSequence sysTimeStr = new SimpleDateFormat("HH:mm").format(new Date(sysTime));
                    CharSequence mCurTimeStr = DateUtil.formatTime(mCurTime);
                    mTimeText.setText(sysTimeStr);
                    break;
                case Constants.HIDETOOLS:
                    isToolsHide = false;
                    isHideTools();
                    break;
                case Constants.VOICECHANGE:
                    setVolume();
                    break;
                case Constants.SCREENSMALL:
                    log.i("qq0","isSmall="+isSmall);
                    fullEnable(false);
                    small();
                    mZhuanHandler.sendEmptyMessageDelayed(0, 1000);
                    break;
                case Constants.SCREENFULL:
                    log.i("qq1","isfirst="+isfirst);
                    if (isfirst){
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        isfirst = false;
                        isSmall = false;
                    }else {
                        fullEnable(true);
                        full(false);
                    }
                    mZhuanHandler.sendEmptyMessageDelayed(0, 1000);
                    break;
                case Constants.SCREENFULLR:
                    log.i("qq2","isfirst="+isfirst);
                    if (isfirst){
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        isfirst = false;
                        isSmall = false;
                    }else {
                        fullEnable(true);
                        //log.i("full2","full2");
                        full(true);
                    }
                    mZhuanHandler.sendEmptyMessageDelayed(0, 1000);
                    break;
                case Constants.LOCKEDFALSE:
                    log.i("lock","lock");
                    isScreenLock = false;
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    /*
    * 声音变化时重置系统声音及声音进度条
    */
    private void setVolume() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVoiceBar.setProgress(currentVolume);
    }

    /*
    * 静态handler处理声音进度任务
    */
    public static Handler mHandlerLiveSeekbar = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.VOICEBARSTOP:
                    if (mAudioManager==null){
                        log.i("null123","null");
                    }
                    mVolume = mAudioManager
                            .getStreamVolume(AudioManager.STREAM_MUSIC);
                    log.i("null11223",""+mVolume);
                    mVoiceText.setText((int) ((100 * mVolume) / mMaxVolume) + " %");
                    break;
                case Constants.VIDEOBARSTOP:
                    mVideo = mMediaPlayer.getTime();
                    log.i("VideoTime", DateUtil.formatTime(mVideo)+"");
                    break;
                case Constants.GESTUREUP:
                    log.i("null456","null");
                    mVideo = -1;
                    mVolume = mScrollIndexVol;
                    break;
            }
            return false;
        }
    });

    /*
     * 隐藏播放布局上的工具条
     */
    private void isHideTools() {

        log.i("isrplay",isShowReplay+"");
        if (isToolsHide) {
            mBottomLayout.setVisibility(View.VISIBLE);
            if (isFullScreen) {
                mVoiceBar.setVisibility(View.VISIBLE);
                mTopLayout.setVisibility(View.VISIBLE);
                mTimeText.setVisibility(View.VISIBLE);
                mLockImg.setVisibility(View.VISIBLE);
            }else{
                mTopLayout.setVisibility(View.VISIBLE);
                mTimeText.setVisibility(View.GONE);
            }
            isToolsHide = false;
            StartLockWindowTimer();
        } else {
            mTopLayout.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.GONE);
            mLockImg.setVisibility(View.GONE);
            mVoiceBar.setVisibility(View.GONE);
            isToolsHide = true;
        }
        if (isShowReplay||isProgressing){
            mLockImg.setVisibility(View.GONE);
            mVoiceBar.setVisibility(View.GONE);
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }

    /*
    *手势监听
    */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /*
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /*
         * 左右两边滑动调节音量两度
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // if (mScreenStatus == Constants.DETAIL_PLAYERFULL) {
            if (!isLock) {
                float mOldX = e1.getX();
                float mOldY = e1.getY();
                float mNewX = e2.getX();
                float mNewY = e2.getY();
                if (Math.abs(mOldX - mNewX) > Math.abs(mOldY - mNewY) * 4) {
                    /*scrollIndexMov = onVideoSlide(-(mOldX - mNewX)
                            / mScreenWidth);*/
                } else if (Math.abs(mOldY - mNewY) > Math.abs(mOldX - mNewX) * 4) {
                    float s = mOldY - mNewY;
                    log.i("xDistance-yDistance", Math.abs(mOldY - mNewY) + "," + Math.abs(mOldX - mNewX));
                    mScrollIndexVol = onVolumeSlide((mOldY - mNewY)
                            / (5.0f * mScreenHeight));
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /*
     * 滑动改变进度快慢
     */
    private float onVideoSlide(float percent) {
        if (mMediaPlayer != null && mMediaPlayer.getLength() != 0) {
            if (mVideo == -1) {
                mVideo = mMediaPlayer.getTime();
                if (mVideo < 0) {
                    mVideo = 0;
                }
            }
            int index = (int) (mVideo + ((mMediaPlayer
                    .getLength() / 4) * percent));
            if (index > (int) mMediaPlayer.getLength()) {
                index = (int) mMediaPlayer.getLength() - 1000;
            }
            if (index < 0) {
                index = 0;
            }
            if (percent > 0) {
                mSlideImg.setBackgroundResource(R.drawable.player_slidego);
            } else {
                mSlideImg.setBackgroundResource(R.drawable.player_slideback);
            }
            mSlideCurTime.setText(DateUtil.formatTime(index));
            mSlideTolTime.setText(DateUtil.formatTime((int) mMediaPlayer.getLength()));
            log.i("onMovieSlide", percent + "," + index + "," + mMediaPlayer.getTime());
            mVideoSlideLayout.setVisibility(View.VISIBLE);
            mMediaPlayer.setTime(index);
            Message msg = new Message();
            msg.what = Constants.MOVIEPROFINISH;
            mUIHandler.sendMessageDelayed(msg, 1500);

        }
        return 0;
    }

    /*
     * 滑动改变声音大小
     */
    private float onVolumeSlide(float percent) {
        mVoiceSlideLayout.setVisibility(View.VISIBLE);
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;
        }
        float index = (percent * ((float) mMaxVolume)/(float)0.3)+mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;
        // 变更声音
        mAudioManager
                .setStreamVolume(AudioManager.STREAM_MUSIC, (int) index, 0);
        mVoiceText.setText((int) ((index / (float) mMaxVolume) * 100) + " %");
        log.i("onVolumeSlide", percent + "," + index + "," + (int) ((index / (float) mMaxVolume) * 100));
        Message msg = new Message();
        msg.what = Constants.DETAIL_VOICEFINISH;
        mUIHandler.sendMessageDelayed(msg, 1500);
        return index;
    }
    /*
    * 调整声音进度后定时隐藏滑动布局
    */
    public  Handler mUIHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DETAIL_VOICEFINISH:
                    mVoiceSlideLayout.setVisibility(View.GONE);
                    break;
                case Constants.MOVIEPROFINISH:
                    mVideoSlideLayout.setVisibility(View.GONE);
                    break;

            }
            return false;
        }
    });

    /*
    * 监听屏幕转动
    */
    private void startOrientationChangeListener() {
        mOrientationListener = new OrientationEventListener(mContext) {
            @Override
            public void onOrientationChanged(int rotation) {
                //log.i("isInZhuan",isInZhuan+"");
                if (!isLock&&!isInZhuan) {
                    if (((rotation >= 0) && (rotation <= 45))|| (rotation >= 315)||((rotation >= 135) && (rotation <= 225))) {// portrait

                        if(isReZhuan){
                            mCurrentOrient = true;
                            if (mCurrentOrient != mScreenProtrait) {
                                isInZhuan = true;
                                mScreenProtrait = mCurrentOrient;
                                mHandler.sendEmptyMessage(Constants.SCREENSMALL);
                            }
                        }
                    } else if ((rotation > 45) && (rotation < 135)) {// landscape
                        isReZhuan=true;
                        mCurrentOrient = false;
                        if (mCurrentOrient != mScreenProtrait) {
                            isInZhuan = true;
                            mScreenProtrait = mCurrentOrient;
                            mHandler.sendEmptyMessage(Constants.SCREENFULLR);

                        }


                    }else if ((rotation > 225) && (rotation < 315)){
                        isReZhuan=true;
                        mCurrentOrient = false;
                        if (mCurrentOrient != mScreenProtrait) {
                            isInZhuan = true;
                            mScreenProtrait = mCurrentOrient;
                            mHandler
                                    .sendEmptyMessage(Constants.SCREENFULL);

                        }
                    }
                }
            }
        };
    }

    public interface CallBackScreenLive{
        public void fullClick();
        public void smallClick();
    }
    CallBackScreenLive callBackScreenLive;
    public void setFullClick(CallBackScreenLive callBackScreen){
        this.callBackScreenLive = callBackScreen;
    }
    public void full(boolean isReverse) {
        callBackScreenLive.fullClick();
        if (isReverse) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        log.i("orifullscreen", fullScreenWidth + "," + fullScreenHeight);
        mVideoParams = new FrameLayout.LayoutParams(fullScreenWidth, fullScreenHeight);
        mVideoParams.leftMargin = 0;
        mVideoParams.topMargin = 0;
        mVideoView.setLayoutParams(mVideoParams);
        this.setLayoutParams(mVideoParams);
        mTopParam.leftMargin = 0;
        mTopParam.topMargin = 0;
        mTopLayout.setLayoutParams(mTopParam);
        mBottomParams.leftMargin = 0;
        mBottomLayout.setLayoutParams(mBottomParams);
        if (!isToolsHide) {
            mTopLayout.setVisibility(View.VISIBLE);
            mTimeText.setVisibility(View.VISIBLE);
            mVoiceBar.setVisibility(View.VISIBLE);
            mLockImg.setVisibility(View.VISIBLE);
        }
        isFullScreen = true;
        mFullBtn.setBackgroundResource(R.drawable.small);
        if (isPause && !isVip) {
            adLayout.setVisibility(View.VISIBLE);
        }
    }
    public void small() {
        callBackScreenLive.smallClick();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log.i("orismallscreen", mPosition[2] + "," + mPosition[3]+"," +mVideoParams.leftMargin+"," +mVideoParams.topMargin);
        mVideoParams = new FrameLayout.LayoutParams(mPosition[2], mPosition[3]);
        if (isSmall){
            mVideoParams.leftMargin = mPosition[0];
            mVideoParams.topMargin = mPosition[1];
            mTopParam.leftMargin = mPosition[0];
            mTopParam.topMargin = mPosition[1];
            mTopLayout.setLayoutParams(mTopParam);
            mBottomParams.leftMargin = mPosition[0];
            mBottomLayout.setLayoutParams(mBottomParams);
        }else{
            mVideoParams.leftMargin = mPosition[0];
            mVideoParams.topMargin = mPosition[1];
        }
        isSmall = true;
        mVideoView.setLayoutParams(mVideoParams);
        this.setLayoutParams(mVideoParams);

        mVoiceBar.setVisibility(View.GONE);
        if (!isToolsHide) {
            mTopLayout.setVisibility(View.VISIBLE);
            mTimeText.setVisibility(View.GONE);
        }
        if (!isVip) {
            adLayout.setVisibility(View.GONE);
        }
        mLockImg.setVisibility(View.GONE);
        isFullScreen = false;
        mFullBtn.setBackgroundResource(R.drawable.expend);
    }
    private void fullEnable(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(lp);
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attr);
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private Handler mZhuanHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            isInZhuan = false;
            return false;
        }
    });

    public void shutdownObserver(){
        observer.shutdownObserver();
    }
    public static void setLastTime(long time){
        mLastExitTimem = time;
    }
    /*
    * 屏幕亮的状态
    */
    @Override
    public void onScreenOn() {
        log.i("onScreen1", "onScreen1");
    }

    /*
    * 锁屏的动作触发
    */
    @Override
    public void onScreenOff() {
        if (mMediaPlayer!=null&&!isPause&&!isBackOrHome) {
            log.i("onScreen2", "onScreen2");
            mMediaPlayer.stop();
        }
        isScreenLock = true;
    }

    /*
    * 解锁屏的动作触发
    */
    @Override
    public void onUserPresent() {
        if (mMediaPlayer!=null&&!isPause&&!isBackOrHome) {
            log.i("onScreen3", "onScreen3");
            mMediaPlayer.play();
        }
        mHandler.sendEmptyMessageDelayed(Constants.LOCKEDFALSE, 1000);
    }


    /*
    * 释放广告播放器
    */
    public static void releasePlayer() {
        hasReleaseLive = true;
        isPause = true;
        mLastExitTimem = 0;
        mOrientationListener.disable();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
            log.i("release", "release");
        }
        ivlcVout.removeCallback(callback);
        ivlcVout.detachViews();
        mLibVLC.release();
    }
}
