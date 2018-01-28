package exo;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;
import org.videolan.libvlc.R;
import org.videolan.libvlc.receiver.ScreenObserver;
import org.videolan.libvlc.util.LogUtil;
import java.util.List;
import exoplayer.DemoPlayer;
import exoplayer.HlsRendererBuilder;

/**
 * Created by lsn on 2016/4/28.
 */
public class AudioPlayerlayout extends FrameLayout implements DemoPlayer.Listener, DemoPlayer.CaptionListener, DemoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener,ScreenObserver.ScreenStateListener{

    private static LogUtil log;
    private Uri contentUri;
    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    Context mContext;
    Application application;
    int[] mPosition;
    String mAudiourl;
    Drawable mDrawable;
    private RelativeLayout mRelalayout;
    private LayoutParams layoutParams;
    public ImageButton centerPlayButton;
    public PlayerControl mPlayControl;
    private ScreenObserver observer;
    private boolean isPause = true;

    /* 构造方法 */
    public AudioPlayerlayout(Context mContext, Application application, int[] array_position, String audiourl, Drawable bitmap) {
        super(mContext);
        this.mContext = mContext;
        this.application = application;
        this.mPosition = array_position;
        this.mAudiourl = audiourl;
        this.mDrawable = bitmap;
        log = LogUtil.getInstance();
        initObserver();
        initView();
        initListener();
        initPlayer();
    }
    public AudioPlayerlayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
   * 初始化锁屏监听
   */
    private void initObserver() {
        observer = new ScreenObserver(mContext);
        observer.startObserver(this);
    }

    /*
    * 初始化布局
    */
    private void initView() {
        mRelalayout = new RelativeLayout(mContext);
        layoutParams = new FrameLayout.LayoutParams(mPosition[2], mPosition[3]);
        layoutParams.leftMargin = mPosition[0];
        layoutParams.topMargin = mPosition[1];
        mRelalayout.setLayoutParams(layoutParams);
        mRelalayout.setBackground(mDrawable);
        this.setLayoutParams(layoutParams);
        this.addView(mRelalayout);

        FrameLayout.LayoutParams playparam = new FrameLayout.LayoutParams(mPosition[2] / 4, mPosition[2] / 4);
        playparam.gravity = Gravity.CENTER;
        centerPlayButton = new ImageButton(mContext);
        centerPlayButton.setLayoutParams(playparam);
        centerPlayButton.setBackgroundResource(R.drawable.pause);
        this.addView(centerPlayButton);
    }

    /*
    * 初始化点击事件监听
    */
    private void initListener() {
        centerPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayControl.isPlaying()){
                    mPlayControl.pause();
                    centerPlayButton.setBackgroundResource(R.drawable.play);
                    isPause = true;
                }else{
                    mPlayControl.start();
                    centerPlayButton.setBackgroundResource(R.drawable.pause);
                    isPause = false;
                }
            }
        });
    }

    /*
    * 初始化播放器
    */
    private void initPlayer() {
        contentUri = Uri.parse(mAudiourl);
        if (player == null) {
            preparePlayer(false);
        } else {
            player.setBackgrounded(false);
        }
    }

    /*
    * 播放器准备中
    */
    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            mPlayControl = player.getPlayerControl();
            player.addListener(this);
            player.setCaptionListener(this);
            player.setMetadataListener(this);
            mPlayControl.pause();
            centerPlayButton.setBackgroundResource(R.drawable.play);
            playerNeedsPrepare = true;
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setPlayWhenReady(playWhenReady);
    }
    //释放资源
    public void releasePlayer() {
        new Thread(){
            @Override
            public void run() {
                if (player != null) {
                    player.release();
                    player = null;
                }
            }
        }.start();
    }

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(mContext, "ExoPlayer");
        return new HlsRendererBuilder(mContext, userAgent, contentUri.toString());
    }

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {

    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {

    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onScreenOn() {

    }

    @Override
    public void onScreenOff() {
        if (player!=null&&!isPause&&mPlayControl!=null) {
            log.i("onScreenoff","onScreenoff");
            mPlayControl.pause();
            centerPlayButton.setBackgroundResource(R.drawable.play);
        }
    }

    @Override
    public void onUserPresent() {
    }
}
