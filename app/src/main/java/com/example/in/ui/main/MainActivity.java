package com.example.in.ui.main;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.in.R;

public class MainActivity extends AppCompatActivity {
    private TaskHelper taskHelper;
    private ClockTimerHelper timerHelper;
    private AmbientHelper ambientHelper;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = findViewById(android.R.id.content);
        taskHelper = new TaskHelper(rootView, this, this);
        timerHelper = new ClockTimerHelper(rootView, this);
        ambientHelper = new AmbientHelper(rootView, this, this);

        timerHelper.getIsTimerStart().observe(this, isStarted -> {
            if(!isStarted){
               ambientHelper.stopAmbient();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}

