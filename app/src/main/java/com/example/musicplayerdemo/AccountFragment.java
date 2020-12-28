package com.example.musicplayerdemo;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class AccountFragment extends Fragment {
    private View mFragmentView;
    private TextView mTvTitlePage;
    private FrameLayout mFlTitleContentPage;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.base_top_title_page, container, false);   //通用布局(图片 充值)
        mTvTitlePage = (TextView) mFragmentView.findViewById(R.id.tv_title_page);
        mFlTitleContentPage = (FrameLayout) mFragmentView.findViewById(R.id.fl_title_content_page);
        View view = initView();
        mFlTitleContentPage.addView(view);
        return mFragmentView;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected View initView() {
        setTitleIcon("账号",true);
        View matchFragment = View.inflate(getContext(), R.layout.fg_accountpage, null);
        return matchFragment;
    }
    public void setTitleIcon(String msg, boolean show) {    //设置标题和图标
        mTvTitlePage.setText(msg);  //设置标题
        mTvTitlePage.setVisibility(show ? View.VISIBLE : View.GONE);     //设置图片显示  true就是显示  false就是不显示
    }
}
