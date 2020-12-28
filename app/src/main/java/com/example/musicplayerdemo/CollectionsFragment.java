package com.example.musicplayerdemo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;


public class CollectionsFragment extends Fragment {

    private View mFragmentView;
    private TextView mTvTitlePage;
    private FrameLayout mFlTitleContentPage;

    public static CollectedMusicAdapter musicAdapter;
    public ListView mListView;
    public static ArrayList list;

    @SuppressLint("CutPasteId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.base_top_title_page, container, false);   //通用布局(图片 充值)
        mTvTitlePage = mFragmentView.findViewById(R.id.tv_title_page);
        mFlTitleContentPage = mFragmentView.findViewById(R.id.fl_title_content_page);
        View view = initView();
        mFlTitleContentPage.addView(view);
        mListView = mFragmentView.findViewById(R.id.lv_collectedMusic);


        MyOpenHelper myOpenHelper = new MyOpenHelper(getContext());
        SQLiteDatabase db = myOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select collectedmusicpath from collectedmusic", null);
        if (cursor.getCount() == 0) {
            AlertDialog dialog;
            dialog = new AlertDialog.Builder(getContext())
                    .setTitle("提示")
                    .setMessage("未收藏任何歌曲")
                    .setPositiveButton("确定", null)
                    .create();
            dialog.show();

        }
        list = new ArrayList();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {// 每遍历一次就是拿出这一行里面的数据
                String path = cursor.getString(0);
                list.add(path);
            }
        }
        musicAdapter = new CollectedMusicAdapter(list);
        mListView.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
        db.close();
        cursor.close();
        return mFragmentView;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    protected View initView() {
        setTitleIcon("我的收藏", true);
        View matchFragment = View.inflate(getContext(), R.layout.fg_collectionspage, null);
        return matchFragment;
    }

    public void setTitleIcon(String msg, boolean show) {    //设置标题和图标
        mTvTitlePage.setText(msg);  //设置标题
        mTvTitlePage.setVisibility(show ? View.VISIBLE : View.GONE);     //设置图片显示  true就是显示  false就是不显示
    }


    class CollectedMusicAdapter extends BaseAdapter {

        List<String> list;

        public CollectedMusicAdapter(List list) {
            this.list = list;
        }

        public void setData(List list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CollectionsFragment.ViewHolder collectedMusicHolder;
            if (convertView == null) {
                collectedMusicHolder = new CollectionsFragment.ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item, parent, false);
                collectedMusicHolder.fileIconImageView = convertView.findViewById(R.id.iv_file_icon);
                collectedMusicHolder.fileNameTextView = convertView.findViewById(R.id.tv_file_name);
                convertView.setTag(collectedMusicHolder);

            } else {
                collectedMusicHolder = (CollectionsFragment.ViewHolder) convertView.getTag();
            }
            String collectedMusic = list.get(position);
            String SubFileName = collectedMusic.substring(0, collectedMusic.length() - 4);
            String[] split = SubFileName.split("[-/]", -1);
            collectedMusicHolder.fileNameTextView.setText(split[5]);

            collectedMusicHolder.fileIconImageView.setBackgroundResource(R.drawable.audiofile);



            return convertView;
        }
    }

    private class ViewHolder {
        public ImageView fileIconImageView;
        public TextView fileNameTextView;
    }
}
