package com.example.musicplayerdemo;

import android.app.Service;
import android.content.Intent;
import android.icu.util.Measure;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    int duration;
    int currentPosition;
    private static final String TAG = "MusicService";
    //Android提供了一个播放器，实现了音乐的播放、暂停等功能
    public static MediaPlayer mMediaPlayer;
    //定时器
    public Timer mTimer;
    public LrcProcess mLrcProcess;
    public static Boolean isRun = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }

    //提供的功能是给Activity调用使用的
    class MusicControl extends Binder {
        // 创建对象
        private List<LrcProcess.LrcContent> lrcList = new ArrayList<LrcProcess.LrcContent>();
        // 初始化歌词检索值
        private int index = 0;

        //设置文件的播放路径
        public void init(String musicPath) {
            Log.d(TAG, "init()");
            try {
                mHandler.post(mRunnable);
                mLrcProcess = new LrcProcess();
                mLrcProcess.readLRC(musicPath);
                lrcList = mLrcProcess.getLrcContent();
                MusicPlayerActivity.lrc_view.setSentenceEntities(lrcList);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.reset();
                //设置要播放的路径
                mMediaPlayer.setDataSource(musicPath);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        //开启定时器，刷新播放进度
                        addTimer();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Handler mHandler = new Handler();
        // 歌词滚动线程
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                MusicPlayerActivity.lrc_view.SetIndex(LrcIndex());
                MusicPlayerActivity.lrc_view.invalidate();
                mHandler.postDelayed(mRunnable, 100);
            }
        };

        public int LrcIndex() {
            if (currentPosition < duration) {
                for (int i = 0; i < lrcList.size(); i++) {
                    if ( currentPosition >lrcList.get(i).getLrc_time()) {
                        index = i;
                    }
                }
            }
            return index;
        }

        //播放功能
        public void play() {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }

        //暂停功能
        public void pause() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }


        //返回当前的播放状态
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }


        // 设置音乐播放位置
        public void seekTo(int progress) {
            mMediaPlayer.seekTo(progress);
        }

    }

    // 添加定时器，用于更新SeekBar的播放进度
    public void addTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            // 定时任务
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 如果已经播放结束，直接退出
                    if (mMediaPlayer == null) {
                        return;
                    }
                    // 获取歌曲的总长度
                    duration = mMediaPlayer.getDuration();
                    // 获取当前的播放进度
                    currentPosition = mMediaPlayer.getCurrentPosition();
                    Message message = MusicPlayerActivity.handler.obtainMessage();
                    // 将音乐的总时长和已经播放的时长放到Message
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    message.setData(bundle);
                    MusicPlayerActivity.handler.sendMessage(message);
                }
            };
            // 开启任务后延迟5毫秒执行第一次任务，以后每100毫秒再执行task任务
            mTimer.schedule(task, 5, 100);
        }
    }


    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("MusicService.onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {

        System.out.println("MusicService.onDestroy()");
        // 如果已经关闭，就不需要做处理
        if (mMediaPlayer == null) {
            return;
        }

        // 如果正在循环播放，首先停止这个音乐
        if (mMediaPlayer.isLooping()) {
            mMediaPlayer.stop();
        }

        // 释放资源
        mMediaPlayer.release();
        mMediaPlayer = null;
        super.onDestroy();
    }

}

