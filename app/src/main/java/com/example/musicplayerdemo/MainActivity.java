package com.example.musicplayerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.jaeger.library.StatusBarUtil;


public class MainActivity extends AppCompatActivity{

    private RelativeLayout mRlFolderLayout;
    private RelativeLayout mRlCollectionsLayout;
    private RelativeLayout mRlAccountLayout;

    private ImageView mIvFolder;
    private TextView mTvFolder;
    private ImageView mIvCollections;
    private TextView mTvCollections ;
    private ImageView mIvAccount;
    private TextView mTvAccount;


    private FragmentSwitchTool tool;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_CODE = 1;


    // 要获取的读写SD卡的权限
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.orange), 0);
        mRlFolderLayout=findViewById(R.id.rl_folder_layout);
        mRlCollectionsLayout=findViewById(R.id.rl_collections_layout);
        mRlAccountLayout=findViewById(R.id.rl_account_layout);

        mTvFolder= findViewById(R.id.tv_folder);
        mTvCollections= findViewById(R.id.tv_collections);
        mTvAccount= findViewById(R.id.tv_account);

        mIvFolder= findViewById(R.id.iv_folder);
        mIvCollections= findViewById(R.id.iv_collections);
        mIvAccount= findViewById(R.id.iv_account);

        mRlFolderLayout.setSelected(true);

        tool=new FragmentSwitchTool(getFragmentManager(),R.id.fl_fragment_content);
        tool.setClickableViews(mRlFolderLayout,mRlCollectionsLayout,mRlAccountLayout);
        tool.addSelectedViews(mIvFolder,mTvFolder)
                .addSelectedViews(mIvCollections,mTvCollections)
                .addSelectedViews(mIvAccount,mTvAccount);
        tool.setFragments(FolderFragment.class,CollectionsFragment.class,AccountFragment.class);
        tool.changeTag(mRlFolderLayout);
        getPermission();




    }

    //获取权限
    private void getPermission() {

        //当前SDK的版本号大于21
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }


}