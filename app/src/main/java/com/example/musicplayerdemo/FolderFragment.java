package com.example.musicplayerdemo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FolderFragment extends Fragment {
    private View mFragmentView;
    private TextView mTvTitlePage;
    private FrameLayout mFlTitleContentPage;
    public ListView mListView;
    public FileAdapter mFileAdapter;
    //当前的正在展示的目录
    public File mCurrentDirectory;


    @SuppressLint("CutPasteId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.base_top_title_page, container, false);   //通用布局(图片 充值)
        mTvTitlePage = mFragmentView.findViewById(R.id.tv_title_page);
        mFlTitleContentPage = mFragmentView.findViewById(R.id.fl_title_content_page);
        View view = initView();
        mFlTitleContentPage.addView(view);
        mCurrentDirectory = Environment.getExternalStorageDirectory();
        mListView = mFragmentView.findViewById(R.id.list_view);

        mFragmentView.setFocusable(true);//这个和下面的这个命令必须要设置了，才能监听back事件。
        mFragmentView.setFocusableInTouchMode(true);
        mFragmentView.setOnKeyListener(backlistener);

        //列出当前文件夹下所有的文件
        File[] files = mCurrentDirectory.listFiles();
        Arrays.sort(files);
        mFileAdapter = new FileAdapter(files);
        mListView.setAdapter(mFileAdapter);
        return mFragmentView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected View initView() {
        setTitleIcon("本地文件", true);
        View matchFragment = View.inflate(getContext(), R.layout.fg_folderpage, null);
        return matchFragment;
    }

    public void setTitleIcon(String msg, boolean show) {    //设置标题和图标
        mTvTitlePage.setText(msg);  //设置标题
        mTvTitlePage.setVisibility(show ? View.VISIBLE : View.GONE);     //设置图片显示  true就是显示  false就是不显示
    }

    private View.OnKeyListener backlistener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                if (!Environment.getExternalStorageDirectory().equals(mCurrentDirectory)) {

                    mCurrentDirectory = mCurrentDirectory.getParentFile();
                    System.out.println(mCurrentDirectory);
                    //列出点击的目录下面所有的文件
                    File[] files = mCurrentDirectory.listFiles();
                    //更新Adapter展示的数据
                    mFileAdapter.setDate(files);
                    //对于Adapter改动的数据需要调用刷新操作
                    mFileAdapter.notifyDataSetChanged();
                    return true;
                } //后退
                return false;    //已处理
            }
            return false;
        }
    };


    private class FileAdapter extends BaseAdapter {

        private File[] files;

        //Adapter需要的数据初始可以通过构造方法传递过来
        public FileAdapter(File[] files) {
            this.files = files;
        }

        //后期数据改动，通过setData修改数据
        public void setDate(File[] files) {
            this.files = files;
        }

        @Override
        public int getCount() {
            return files.length;
        }

        @Override
        public Object getItem(int position) {
            return files[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, android.view.View convertView, ViewGroup parent) {
            FolderFragment.ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new FolderFragment.ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item, parent, false);
                viewHolder.fileIconImageView = convertView.findViewById(R.id.iv_file_icon);
                viewHolder.fileNameTextView = convertView.findViewById(R.id.tv_file_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (FolderFragment.ViewHolder) convertView.getTag();
            }
            File file = files[position];
            //获得文件的名字
            String fileName = file.getName();
            viewHolder.fileNameTextView.setText(fileName);
            if (file.isDirectory()) {
                viewHolder.fileIconImageView.setBackgroundResource(R.drawable.folder);
            } else if (file.getName().endsWith(".mp3")) {
                viewHolder.fileIconImageView.setBackgroundResource(R.drawable.audiofile);
            } else {
                viewHolder.fileIconImageView.setBackgroundResource(R.drawable.unknownfile);
            }

            //给ListView添加点击事件
            convertView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    //如果点击的是目录（文件夹）
                    if (file.isDirectory()) {
                        mCurrentDirectory = file;
                        //列出点击的目录下面所有的文件
                        File[] files = mCurrentDirectory.listFiles();
                        //更新Adapter展示的数据
                        mFileAdapter.setDate(files);
                        //对于Adapter改动的数据需要调用刷新操作
                        mFileAdapter.notifyDataSetChanged();
                        mListView.setSelection(0);
                    } else if (file.isFile() && file.getName().endsWith(".mp3")) {
                        //如果是MP3文件
                        Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                        intent.putExtra("fileName",file.getName());
                        intent.putExtra("path", file.getPath());
                        ArrayList<String> mp3List = getMp3List(mCurrentDirectory);
                        intent.putExtra("mp3List", mp3List);
                        startActivity(intent);
                    } else {
                        //非MP3文件
                        Toast.makeText(getActivity(), "不支持该文件类型", Toast.LENGTH_LONG).show();
                    }
                }
                private ArrayList<String> getMp3List(File directory) {
                    File[] files = directory.listFiles();
                    ArrayList<String> list = new ArrayList<>();
                    for (File file : files) {
                        if (file != null && file.isFile() && file.getName().endsWith(".mp3")) {
                            list.add(file.getPath());
                        }
                    }
                    return list;
                }
            });
            return convertView;
        }
    }

    private class ViewHolder {
        public ImageView fileIconImageView;
        public TextView fileNameTextView;
    }
}
