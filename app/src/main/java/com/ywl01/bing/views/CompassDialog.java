package com.ywl01.bing.views;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ywl01.bing.R;
import com.ywl01.bing.events.GetAngleEvent;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ywl01 on 2017/10/9.
 */

public class CompassDialog extends Dialog implements View.OnTouchListener {

    private float downX;
    private float downY;
    private Context context;

    @BindView(R.id.hand)
    ImageView handView;

    @BindView(R.id.house)
    ImageView houseView;

    @BindView(R.id.container_znz)
    RelativeLayout znzContainer;

    private View rootView;

    public CompassDialog(Context context) {
        super(context);
        initView(context);
    }

    public CompassDialog(Context context, int themeResId) {
        super(context, themeResId);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.compass, null);
        ButterKnife.bind(this, rootView);
        setContentView(rootView);
        setTitle("请选择房屋朝向：");
        rootView.setOnTouchListener(this);
    }

    public void setInitAngle(float angle) {
        handView.setRotation(360 - angle);
        houseView.setRotation(360 - angle);
        rootView.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                System.out.println("downx:" + downX + " downY:" + downY);
                int angle = getRotationBetweenLines(znzContainer.getWidth() / 2, znzContainer.getHeight() / 2, downX, downY);
                handView.setRotation(360 - angle);
                houseView.setRotation(360 - angle);
                System.out.println("angle:" + angle);
                break;

            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                angle = getRotationBetweenLines(znzContainer.getWidth() / 2, znzContainer.getHeight() / 2, moveX, moveY);
                handView.setRotation(360 - angle);
                houseView.setRotation(360 - angle);
                System.out.println("angle:" + angle);
                break;

            case MotionEvent.ACTION_UP:
                float upX = event.getX();
                float upY = event.getY();
                angle = getRotationBetweenLines(znzContainer.getWidth() / 2, znzContainer.getHeight() / 2, upX, upY);
                if (angle > 360) {
                    angle = angle - 360;
                }
                GetAngleEvent angleEvent = new GetAngleEvent();
                angleEvent.angle = angle;
                angleEvent.dispatch();
                break;
        }
        return true;
    }

    /**
     * 获取两条线的夹角
     * 南为0度，逆时针旋转度数递增
     */
    public static int getRotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        double rotation = 0;
        double k1 = (double) (centerY - centerY) / (centerX * 2 - centerX);
        System.out.println("k1:" + k1);
        double k2 = (double) (yInView - centerY) / (xInView - centerX);
        double tmpDegree = Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180;

        if (xInView > centerX && yInView < centerY) {
            //第1象限
            rotation = 90 + tmpDegree;
        } else if (xInView > centerX && yInView > centerY) {
            rotation = 90 - tmpDegree;
        } else if (xInView < centerX && yInView > centerY) {
            rotation = 270 + tmpDegree;
        } else if (xInView < centerX && yInView < centerY) {
            rotation = 270 - tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        }

        System.out.println("du:" + tmpDegree + " rotation:" + rotation);
        return (int) rotation;
    }

}
