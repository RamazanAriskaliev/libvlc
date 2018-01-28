/*****************************************************************************
 * MediaPlayer.java
 *****************************************************************************
 * Copyright © 2015 VLC authors and VideoLAN
 *
 * Authors  Jean-Baptiste Kempf <jb@videolan.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.libvlc.media;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.R;
import org.videolan.libvlc.util.Constants;

import java.util.ArrayList;

import exo.AudioPlayerlayout;

public class MainPlayer
{
    private static LibVLC mLibVLC;
    private static org.videolan.libvlc.MediaPlayer mMediaPlayer;
    static Context mContext;
    private Activity mActivity;
    private  VideoViewlayout mVideoViewlayout;
    private FrameLayout layout;
    private Application application;
    private AudioPlayerlayout mAudioPlayerlayout;
    private VideoViewLivelayout mVideoViewLivelayout;
    private static boolean isLive;
    private float scrollY;
    private ViewTreeObserver.OnGlobalLayoutListener listener;
    private Handler SHandler;
    private ViewTreeObserver vto;

    public MainPlayer(Context context,Application application) {
        this.mContext = context;
        this.application = application;
        this.mActivity = (Activity)context;
        //options.add(":live-caching=1500");//直播缓存
        //mLibVLC = new LibVLC(options);
    }

    public void initMainPlayer(final ViewGroup viewgroup,final ScrollView scrollView,final ArrayList<View> listimg,int[] array_position,String videourl,String  videoTitle,boolean isVip,boolean isLive,String slotid){

        Log.i("slotid",slotid+"123");
        this.isLive = isLive;
        mLibVLC = new LibVLC();
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mLibVLC);
        layout = new FrameLayout(mContext);
        if (isLive){
            mVideoViewLivelayout = new VideoViewLivelayout(mContext,application,mMediaPlayer,mLibVLC,videourl,videoTitle,array_position,isVip,slotid);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(layoutParams);
            layout.addView(mVideoViewLivelayout);
            viewgroup.addView(layout);
            mVideoViewLivelayout.setFullClick(new VideoViewLivelayout.CallBackScreenLive() {
                @Override
                public void fullClick() {
                    scrollY = scrollView.getScrollY();
                    for (int i = 0; i < listimg.size(); i++) {
                        listimg.get(i).setVisibility(View.GONE);
                    }
                }

                @Override
                public void smallClick() {
                    for (int i = 0; i < listimg.size(); i++) {
                        listimg.get(i).setVisibility(View.VISIBLE);
                    }
                    vto = scrollView.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(listener);
                    SHandler.sendEmptyMessageDelayed(0, 1000);
                }
            });
        }else{
            mVideoViewlayout = new VideoViewlayout(mContext,application,mMediaPlayer,mLibVLC,videourl,videoTitle,array_position,isVip,slotid);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(layoutParams);
            layout.addView(mVideoViewlayout);
            viewgroup.addView(layout);
            mVideoViewlayout.setFullClick(new VideoViewlayout.CallBackScreen() {
                @Override
                public void fullClick() {
                        scrollY = scrollView.getScrollY();
                        for (int i = 0; i < listimg.size(); i++) {
                            listimg.get(i).setVisibility(View.GONE);
                        }
            }
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void smallClick() {
                    for (int i = 0; i < listimg.size(); i++) {
                        listimg.get(i).setVisibility(View.VISIBLE);
                    }
                    vto = scrollView.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(listener);
                    SHandler.sendEmptyMessageDelayed(0, 1000);
        }
    });
        }

        SHandler = new Handler(new Handler.Callback() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean handleMessage(Message msg) {
                vto.removeOnGlobalLayoutListener(listener);
                return false;
            }
        });


        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                scrollView.scrollTo(0,(int)scrollY);
            }
        };
    }
    public void initAudioPlayer(ViewGroup viewgroup,int[] array_position,String audiourl,Drawable drawable){

        mAudioPlayerlayout = new AudioPlayerlayout(mContext,application,array_position,audiourl,drawable);
        viewgroup.addView(mAudioPlayerlayout);
    }
    public void audioReleasePlayer(){
        if (mAudioPlayerlayout!=null){
            mAudioPlayerlayout.releasePlayer();
        }
    }

    public long getPlayTime() {
        if (mVideoViewlayout!=null&&mVideoViewlayout.mMediaPlayer!=null) {
            return mVideoViewlayout.mMediaPlayer.getTime();
        }
        return 0;
    }
    public  void setTime(long time) {
        VideoViewlayout.setLastTime(time);
    }
    public void releasePlayer(){
        if (!isLive&&null!=mVideoViewlayout){
                mVideoViewlayout.releasePlayer();
        }else{
                mVideoViewLivelayout.releasePlayer();
        }
    }
    public void initVideoView(){
        if (!isLive&&null!=mVideoViewlayout){
            mVideoViewlayout.reinitVideoView();
        }else{
            mVideoViewLivelayout.reinitVideoView();
        }

    }
    public boolean isScreenLock(){
        if (!isLive&&null!=mVideoViewlayout){
            return mVideoViewlayout.isScreenLock;
        }else{
            return mVideoViewLivelayout.isScreenLock;
        }
    }
    public void observerDestory(){
        if (!isLive&&null!=mVideoViewlayout){
            mVideoViewlayout.shutdownObserver();
        }else{
            mVideoViewLivelayout.shutdownObserver();
        }
    }
    public void toPlay(){
        if (null!=mVideoViewlayout&&!isLive) {
            mVideoViewlayout.toPlay();
        }else if (isLive){
            mVideoViewLivelayout.toPlayLive();
        }
    }
    public void audioPause(){
        if (null!=mAudioPlayerlayout.mPlayControl)
            mAudioPlayerlayout.mPlayControl.pause();
        mAudioPlayerlayout.centerPlayButton.setBackgroundResource(R.drawable.play);
    }
    public void backPress(){
        if (!isLive&&null!=mVideoViewlayout){
                if (mVideoViewlayout.isFullScreen) {
                    if (!mVideoViewlayout.isInZhuan){
                    mVideoViewlayout.mCurrentOrient = true;
                    if (mVideoViewlayout.mCurrentOrient != mVideoViewlayout.mScreenProtrait) {
                        mVideoViewlayout.mScreenProtrait = mVideoViewlayout.mCurrentOrient;
                    }
                    mVideoViewlayout.isInZhuan = true;
                    mVideoViewlayout.mHandler.sendEmptyMessage(Constants.SCREENSMALL);
                }
            }else{
                    Log.i("finfish","finish");
                    releasePlayer();
                mActivity.finish();
            }

        }else{
                if (mVideoViewLivelayout.isFullScreen) {
                    if (!mVideoViewLivelayout.isInZhuan){
                    mVideoViewLivelayout.mCurrentOrient = true;
                    if (mVideoViewLivelayout.mCurrentOrient != mVideoViewLivelayout.mScreenProtrait) {
                        mVideoViewLivelayout.mScreenProtrait = mVideoViewLivelayout.mCurrentOrient;
                    }
                    mVideoViewLivelayout.isInZhuan = true;
                    mVideoViewLivelayout.mHandler.sendEmptyMessage(Constants.SCREENSMALL);
                }
            }else{
                mActivity.finish();
            }
        }
    }
}
