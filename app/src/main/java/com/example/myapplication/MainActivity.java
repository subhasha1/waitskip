package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.waitskip.WaitSkipView;

public class MainActivity extends AppCompatActivity {

    private WaitSkipView waitSkipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        waitSkipView = (WaitSkipView) findViewById(R.id.waitSkipView);
        waitSkipView.setCompletionText("Earn $500", "Earned $500");
    }

    @Override
    protected void onResume() {
        super.onResume();
        waitSkipView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        waitSkipView.cancel();
    }

    public void restart(View view) {
        waitSkipView.start();
    }
}
