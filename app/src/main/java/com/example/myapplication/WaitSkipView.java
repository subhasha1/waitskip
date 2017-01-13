package com.example.myapplication;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Braindigit
 * Created on 1/4/17.
 */

public class WaitSkipView extends LinearLayout {
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
    }

    public void setCompletionText(@StringRes int initial, @StringRes int onCompletion) {
        this.initialText = getContext().getString(initial);
        this.completionText = getContext().getString(onCompletion);
        setActionText(isComplete ? completionText : initialText);
    }

    public void setCompletionText(String initialText, String completionText) {
        this.initialText = initialText;
        this.completionText = completionText;
        setActionText(isComplete ? completionText : initialText);
    }

    private void setActionText(String text) {
        action.setText(text == null ? "" : text);
    }

    public void start() {
        start(DEFAULT_SKIP_AFTER_MILLIS, DEFAULT_TIMER_INTERVAL);
    }

    public void start(long duration) {
        start(duration, DEFAULT_TIMER_INTERVAL);
    }

    public void start(long duration, long interval) {
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
                animateAction(0, -1000);
                action.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setActionText(completionText);
                        animateAction(-1000, 0);
                    }
                }, 2000);
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
}
