package com.example.musicplayerdemo;


import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private String mPath;
    private static final String TAG = "MusicPlayerActivity";
    private ImageView mIvPlay;
    private ImageView mIvNext;
    private ImageView mIvPrevious;
    public ImageView mIvPlayModel;
    public ImageView mIvNotCollected;
    public static ArrayList<String> mList;
    public static int mCurrentMusicIndex;
    int mPrevMusicIndex;
    int mNextMusicIndex;
    public MyOpenHelper myOpenHelper;
    public static MusicService.MusicControl mMusicControl;
    public static LrcView lrc_view;
    private static SeekBar mSeekBar;
    private static TextView mTvProgress;
    private static TextView mTvTotal;
    public static TextView mTvMusicTitle;
    public static TextView mTvMusicArtist;

    // 用于判断当前的播放顺序，0:单曲循环 1：随机播放  2:顺序播放
    public static int playStatus = 0;
    String[] split;
    String fileName;
    Cursor cursor;

    public static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Bundle bundle = msg.getData();
            // 总时长
            int duration = bundle.getInt("duration");
            // 当前播放时长
            int currentPosition = bundle.getInt("currentPosition");
            mSeekBar.setMax(duration);
            mSeekBar.setProgress(currentPosition);

            // 显示总时长
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            mTvTotal.setText(minute + ":" + second);

            // 显示播放时长
            int currentMinute = currentPosition / 1000 / 60;
            int currentSecond = currentPosition / 1000 % 60;
            mTvProgress.setText(currentMinute + ":" + currentSecond);
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        mIvPlay = findViewById(R.id.iv_play);
        mIvNext = findViewById(R.id.iv_next);
        mIvPrevious = findViewById(R.id.iv_previous);

        mIvPlayModel = findViewById(R.id.iv_playModel);
        mIvNotCollected = findViewById(R.id.iv_not_collected);

        mSeekBar = findViewById(R.id.seekBar);
        mTvProgress = findViewById(R.id.tv_progress);
        mTvTotal = findViewById(R.id.tv_total);

        mTvMusicTitle = findViewById(R.id.tv_musicTitle);
        mTvMusicArtist = findViewById(R.id.tv_musicArtist);
        lrc_view = findViewById(R.id.LyricShow);
        myOpenHelper = new MyOpenHelper(getApplicationContext());


        mIvPlay.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPrevious.setOnClickListener(this);
        mIvPlayModel.setOnClickListener(this);
        mIvNotCollected.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // 停止滑动时候处理
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 得到当前拖动停止的位置
                int progress = seekBar.getProgress();
                // 调用服务的seekTo方法改变音乐的进度
                mMusicControl.seekTo(progress);
            }

            // 开始滑动时候处理
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            // 滑动条变化时候处理
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

            }
        });
        Intent intent = getIntent();
        mPath = intent.getStringExtra("path");
        String string = intent.getStringExtra("fileName");
        String SubFileName = string.substring(0, string.length() - 4);
        String musicPath = intent.getStringExtra("path");



        split = SubFileName.split("-");
        mTvMusicTitle.setText(split[0]);
        mTvMusicArtist.setText(split[1]);

        Log.d(TAG, "mPath" + mPath);
        mList = (ArrayList<String>) intent.getSerializableExtra("mp3List");
        //获得当前播放文件在List中的索引
        mCurrentMusicIndex = mList.indexOf(mPath);
        collectedMusicPrevNextIndex(mCurrentMusicIndex);
        //绑定Service
        intent = new Intent(this, MusicService.class);
        bindService(intent, new MyConnection(), BIND_AUTO_CREATE);
        setMediaPlayerListener();

    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase db;
        switch (v.getId()) {
            case R.id.iv_play:
                if (mMusicControl.isPlaying()) {
                    mMusicControl.pause();
                    mIvPlay.setImageDrawable(getResources().getDrawable(R.drawable.playbar_pause));
                } else {
                    mMusicControl.play();
                    mIvPlay.setImageDrawable(getResources().getDrawable(R.drawable.playbar_play));
                }
                break;
            case R.id.iv_previous:
                Log.d(TAG, "previous");
                mPrevMusicIndex = mCurrentMusicIndex - 1;
                collectedMusicPrevNextIndex(mPrevMusicIndex);
                if (mPrevMusicIndex < 0) {
                    Toast.makeText(this, "这已经是第一首歌了！", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileName = mList.get(mPrevMusicIndex);
                String SubCurrentIndexPrev = fileName.substring(0, fileName.length() - 4);
                split = SubCurrentIndexPrev.split("[-/]", -1);
                mTvMusicTitle.setText(split[5]);
                mTvMusicArtist.setText(split[6]);
                if (playStatus == 1) {
                    randomPlaying();
                } else {
                    if (mCurrentMusicIndex == 0) {
                        Toast.makeText(this, "这已经是第一首歌了！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mCurrentMusicIndex--;
                    mMusicControl.init(mList.get(mCurrentMusicIndex));
                }
                break;
            case R.id.iv_next:
                Log.d(TAG, "next");
                mNextMusicIndex = mCurrentMusicIndex + 1;
                collectedMusicPrevNextIndex(mNextMusicIndex);
                System.out.println(mNextMusicIndex);
                if (mNextMusicIndex == mList.size()) {
                    Toast.makeText(this, "这已经是最后一首歌了！", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileName = mList.get(mNextMusicIndex);
                String SubCurrentIndex = fileName.substring(0, fileName.length() - 4);
                split = SubCurrentIndex.split("[-/]", -1);
                mTvMusicTitle.setText(split[5]);
                mTvMusicArtist.setText(split[6]);
                if (playStatus == 1) {
                    randomPlaying();
                } else {
                    if (mCurrentMusicIndex == mList.size() - 1) {
                        Toast.makeText(this, "这已经是最后一首歌了！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mCurrentMusicIndex++;
                    mMusicControl.init(mList.get(mCurrentMusicIndex));

                }
                break;
            case R.id.iv_playModel:
                ++playStatus;
                if (playStatus > 2) {
                    playStatus = 0;
                }
                switch (playStatus) {
                    case 0:
                        mIvPlayModel.setImageDrawable(getResources().getDrawable(R.drawable.playbar_circle));
                        Toast.makeText(MusicPlayerActivity.this, "单曲循环",
                                Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        mIvPlayModel.setImageDrawable(getResources().getDrawable(R.drawable.playbar_random));
                        Toast.makeText(MusicPlayerActivity.this, "随机播放",
                                Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        mIvPlayModel.setImageDrawable(getResources().getDrawable(R.drawable.playbar_order));
                        Toast.makeText(MusicPlayerActivity.this, "顺序播放",
                                Toast.LENGTH_LONG).show();
                        break;
                }
                break;
            case R.id.iv_not_collected:




                MyOpenHelper myOpenHelper = new MyOpenHelper(this);
                SQLiteDatabase database = myOpenHelper.getReadableDatabase();
                cursor = database.rawQuery("select collectedmusicpath from collectedmusic where id=?", new String[]{String.valueOf(mCurrentMusicIndex)});
                if (cursor.getCount() == 0) {
                    mIvNotCollected.setImageDrawable(getResources().getDrawable(R.drawable.collected));
                    database = myOpenHelper.getWritableDatabase();
                    String collectedmusicpath = mList.get(mCurrentMusicIndex);
                    int id = mCurrentMusicIndex;
                    database.execSQL("insert into collectedmusic(id,collectedmusicpath) values(?,?)", new Object[]{id, collectedmusicpath});
                    cursor.close();
                    database.close();
                    Toast.makeText(this, "已收藏该歌曲", Toast.LENGTH_LONG).show();
                } else {
                    mIvNotCollected.setImageDrawable(getResources().getDrawable(R.drawable.notcolleted));
                    database = myOpenHelper.getWritableDatabase();
                    database.execSQL("delete from collectedmusic where id = ?", new String[]{String.valueOf(mCurrentMusicIndex)});
                    Toast.makeText(this, "取消收藏", Toast.LENGTH_LONG).show();
                    cursor.close();
                    database.close();
                }

                SQLiteDatabase dbmusic = myOpenHelper.getReadableDatabase();
                Cursor cursor = dbmusic.rawQuery("select collectedmusicpath from collectedmusic", null);
                ArrayList<String> musiclist = new ArrayList<>();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {// 每遍历一次就是拿出这一行里面的数据
                        String path = cursor.getString(0);
                        musiclist.add(path);
                    }
                    //更新Adapter展示的数据
                    CollectionsFragment.musicAdapter.setData(musiclist);
                    //对于Adapter改动的数据需要调用刷新操作
                   // CollectionsFragment.musicAdapter.notifyDataSetChanged();

                }


                break;
            default:
                break;
        }

    }


    public void collectedMusicPrevNextIndex(int index) {
        MyOpenHelper myOpenHelper = new MyOpenHelper(this);
        SQLiteDatabase db = myOpenHelper.getReadableDatabase();
        Cursor IndexCursor = db.rawQuery("select collectedmusicpath from collectedmusic where id=?", new String[]{String.valueOf(index)});
        if (IndexCursor.getCount() == 0) {
            mIvNotCollected.setImageDrawable(getResources().getDrawable(R.drawable.notcolleted));
        } else {
            mIvNotCollected.setImageDrawable(getResources().getDrawable(R.drawable.collected));
        }
        db.close();
        IndexCursor.close();
    }

    public static void randomPlaying() {
        Random random = new Random();
        mCurrentMusicIndex = mCurrentMusicIndex + random.nextInt(mList.size() - 1);
        mCurrentMusicIndex %= mList.size();
        mMusicControl.init(mList.get(mCurrentMusicIndex));
    }

    private void setMediaPlayerListener() {
        // 监听mediaPlayer
        MusicService.mMediaPlayer = new MediaPlayer();
        MusicService.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        MusicService.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                switch (playStatus) {
                    //单曲循环
                    case 0:
                        mMusicControl.init(mList.get(mCurrentMusicIndex));
                        break;
                    case 1: //随机播放
                        randomPlaying();
                        break;
                    case 2:  //顺序播放
                        if (mCurrentMusicIndex == mList.size() - 1) {
                            Toast.makeText(MusicPlayerActivity.this, "这已经是最后一首歌了！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mCurrentMusicIndex++;
                        mMusicControl.init(mList.get(mCurrentMusicIndex));
                        break;
                    default:
                        break;
                }
            }
        });
        // 设置发生错误时调用
        MusicService.mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                if(mp!=null){
                    mp.release();
                }
                mp = new MediaPlayer();
                mp.reset();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mp.setDataSource(getApplicationContext(), Uri.parse(mList.get(mCurrentMusicIndex)));
                    mp.prepare();
                } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mp.start();
                return false;

            }
        });
    }


    //ServiceConnection:用来绑定客户端和服务器，就是用来绑定Service
    private class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定上Service
            mMusicControl = (MusicService.MusicControl) service;
            mMusicControl.init(mList.get(mCurrentMusicIndex));

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
