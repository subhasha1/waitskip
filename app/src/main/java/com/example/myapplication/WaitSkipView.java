package com.example.myapplication;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Braindigit
 * Created on 1/4/17.
 */

public class WaitSkipView extends RelativeLayout {
    private static final String COLOR_GREEN = "#FF3FB54F";
    private static final long DEFAULT_SKIP_AFTER_MILLIS = 5000;
    private static final long DEFAULT_TIMER_INTERVAL = 500;

    private TextView timerTextView;
    private TextView action;
    private CountDownTimer timer;
    private OnSkipActionListener skipActionListener;
    private TimerFinishListener timerFinishListener;
    private String initialText;
    private String completionText;
    private boolean isComplete;

    public WaitSkipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_wait_skip, this);
        setLayoutTransition(new LayoutTransition());
        timerTextView = (TextView) findViewById(R.id.timer);
        action = (TextView) findViewById(R.id.completeAction);
        action.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (skipActionListener != null)
                    skipActionListener.onSkip();
            }
        });

        ShapeDrawable timerCircle = drawOval(true, timerTextView.getWidth(), timerTextView.getHeight(),
                Color.parseColor(COLOR_GREEN));
        GradientDrawable actionOval = drawRoundedRectangle(false, action.getWidth(), action.getHeight(),
                Color.parseColor(COLOR_GREEN));


        if (Build.VERSION.SDK_INT >= 16) {
            timerTextView.setBackground(timerCircle);
            action.setBackground(actionOval);
        } else {
            timerTextView.setBackgroundDrawable(timerCircle);
            action.setBackgroundDrawable(actionOval);
        }
    }

    public void setCompletionText(@StringRes int initial, @StringRes int onCompletion) {
        this.initialText = getContext().getString(initial);
        this.completionText = getContext().getString(onCompletion);
        setActionText();
    }

    public void setCompletionText(String initialText, String completionText) {
        this.initialText = initialText;
        this.completionText = completionText;
        setActionText();
    }

    private void setActionText() {
        String text = isComplete ? completionText : initialText;
        action.setText(text == null ? "" : text);
    }

    public void start() {
        start(DEFAULT_SKIP_AFTER_MILLIS, DEFAULT_TIMER_INTERVAL);
    }

    public void start(long duration) {
        start(duration, DEFAULT_TIMER_INTERVAL);
    }

    public void start(long duration, long interval) {
        animateAction(-1000, 0);
        action.setVisibility(VISIBLE);
        timer = new CountDownTimer(duration, interval) {
            long currentSecs;

            @Override
            public void onTick(long millisUntilFinished) {
                long remainingSecs = millisUntilFinished / 1000L;
                if (currentSecs == remainingSecs) return;
                currentSecs = remainingSecs;
                timerTextView.setText(currentSecs + " s");
            }

            @Override
            public void onFinish() {
                timerTextView.setVisibility(GONE);
                LayoutTransition.TransitionListener transitionListener = new LayoutTransition.TransitionListener() {
                    @Override
                    public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                        if(view==timerTextView){
                            timerTextView.setVisibility(VISIBLE);
                            getLayoutTransition().removeTransitionListener(this);
                        }
                    }

                    @Override
                    public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {

                    }
                };
                getLayoutTransition().addTransitionListener(transitionListener);
                action.setVisibility(GONE);
                if (timerFinishListener != null)
                    timerFinishListener.onFinish();
            }

        }.start();
    }

    public void cancel() {
        if (timer != null) timer.cancel();
    }

    public void setSkipActionListener(OnSkipActionListener listener) {
        this.skipActionListener = listener;
    }

    public void setTimerFinishListener(TimerFinishListener listener) {
        this.timerFinishListener = listener;
    }

    public void setSimpleWaitlSkipListener(SimpleWaitSkipListener listener) {
        this.timerFinishListener = listener;
        this.skipActionListener = listener;
    }

    public interface OnSkipActionListener {
        void onSkip();
    }

    public interface TimerFinishListener {
        void onFinish();
    }

    public static abstract class SimpleWaitSkipListener implements OnSkipActionListener, TimerFinishListener {
        @Override
        public void onFinish() {
//            Ignored
        }

        @Override
        public void onSkip() {
//            Ignored
        }
    }

    private void animateAction(float fromXDelta, float toXDelta) {
        TranslateAnimation animation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
        animation.setFillAfter(true);
        animation.setDuration(500);
        action.startAnimation(animation);
    }

    public static ShapeDrawable drawOval(boolean fill, int width, int height, int color) {
        ShapeDrawable oval = new ShapeDrawable(new OvalShape());
        oval.setIntrinsicHeight(height);
        oval.setIntrinsicWidth(width);
        oval.getPaint().setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
        oval.getPaint().setColor(color);
        return oval;
    }

    public static GradientDrawable drawRoundedRectangle(boolean fill, int width, int height, int color) {
        GradientDrawable gdDefault = new GradientDrawable();
        gdDefault.setColor(color);
        gdDefault.setCornerRadius(16);
        return gdDefault;
    }
}
