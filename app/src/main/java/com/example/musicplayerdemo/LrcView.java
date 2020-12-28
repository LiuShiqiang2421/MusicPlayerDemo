package com.example.musicplayerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class LrcView extends androidx.appcompat.widget.AppCompatTextView {
    private float width;
    private float height;
    private Paint CurrentPaint;   //高亮部分   当前正在播放的歌词为高亮状态
    private Paint NotCurrentPaint;

    private int Index = 0;

    private List<LrcProcess.LrcContent> mSentenceEntities = new ArrayList<>();

    public void setSentenceEntities(List<LrcProcess.LrcContent> mSentenceEntities) {
        this.mSentenceEntities = mSentenceEntities;
    }

    public LrcView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }

    private void init() {
        // TODO Auto-generated method stub
        setFocusable(true);

        // 高亮部分
        CurrentPaint = new Paint();
        CurrentPaint.setTextAlign(Paint.Align.CENTER);    //文字居中对齐

        // 非高亮部分
        NotCurrentPaint = new Paint();
        NotCurrentPaint.setTextAlign(Paint.Align.CENTER);   //文字居中对齐
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas --> 画布
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }

        CurrentPaint.setColor(Color.argb(255, 255, 167, 38));
        NotCurrentPaint.setColor(Color.argb(150, 255, 255, 255));
        CurrentPaint.setTextSize(48);
        float textSize = 36;
        NotCurrentPaint.setTextSize(textSize);

        try {
            setText("");
            float NotCurrentPaintTempY = height / 2, CurrentPaintTempY = height / 2;
            float textHigh = 58;
            canvas.drawText(mSentenceEntities.get(Index).getLrc(), width / 2, NotCurrentPaintTempY, CurrentPaint);
            // 画出本句之前的句子
            for (int i = Index - 1; i >= 0; i--) {
                // 向上推移
                NotCurrentPaintTempY -= textHigh;
                canvas.drawText(mSentenceEntities.get(i).getLrc(), width / 2, NotCurrentPaintTempY, NotCurrentPaint);
            }
            // 画出本句之后的句子
            for (int i = Index + 1; i < mSentenceEntities.size(); i++) {
                // 往下推移
                CurrentPaintTempY += textHigh;
                canvas.drawText(mSentenceEntities.get(i).getLrc(), width / 2, CurrentPaintTempY, NotCurrentPaint);
            }

        } catch (Exception e) {
            setText("未找到歌词文件");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    public void SetIndex(int index) {
        this.Index = index;
    }
}
