package com.example.in.ui.main;


import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.in.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//control ui, listener
public class ClockTimerHelper {
    //textview, textclock, tooglerbtn, numberpicker
    private NumberPicker npHour;
    private NumberPicker npMinute;
    private NumberPicker npSecond;
    private ClockTimerViewModel viewModel;

    private TextView tvTimer;
    private TextClock tcTimer;

    private LinearLayout llTimer;
    private RecyclerView rvTask;

    private ImageButton btnStartPause;

    private TextView tvStart, tvEnd;

    private static final int  MAX_HOUR = 3;
    private static final int  MAX_MINUTE_SECOND = 59;
    private static final int MIN = 0;
    private final List<NumberPicker> numberPicker = new ArrayList<>();

    public <T extends LifecycleOwner & ViewModelStoreOwner> ClockTimerHelper(View rootView, T owner){

        viewModel = new ViewModelProvider(owner).get(ClockTimerViewModel.class);
        npHour = (NumberPicker) rootView.findViewById(R.id.NPHour);
        npMinute = (NumberPicker) rootView.findViewById(R.id.NPMinute);
        npSecond = (NumberPicker) rootView.findViewById(R.id.NPSecond);
        tvTimer = (TextView) rootView.findViewById(R.id.TVTimer);
        tcTimer = (TextClock) rootView.findViewById(R.id.TCTimer);
        btnStartPause = (ImageButton) rootView.findViewById(R.id.IBtoggler);
        tvStart = (TextView) rootView.findViewById(R.id.TVStart);
        tvEnd = (TextView) rootView.findViewById(R.id.TVEnd);

        numberPicker.addAll(List.of(npHour, npMinute, npSecond));

        this.llTimer = (LinearLayout) rootView.findViewById(R.id.LLTimer);
        this.rvTask = (RecyclerView) rootView.findViewById(R.id.RVTask);

        setBtnUi(btnStartPause.isActivated());
        setNumberPicker();
        setInterface(false, btnStartPause.isActivated());

        viewModel.getTimerText().observe(owner, timerText -> {
            String text = timerText.isEmpty() ? "00:00:00" : timerText;
            tvTimer.setText(text);
        });

        viewModel.getIsTimerStart().observe(owner, isStarted -> {
            setBtnUi(isStarted);
            setInterface(false, isStarted);
            if(!isStarted)
                resetNumberPickers();
        });

        btnStartPause.setOnClickListener(v -> {
            boolean isToStart = !v.isActivated();
            if(isToStart){
                if(tvTimer.getText().toString().equals("00:00:00")){
                    Toast.makeText(v.getContext(), "Please set timer", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.startTimer();
            }else{
                viewModel.stopTimer();
            }
        });

        tcTimer.setOnClickListener(v -> {
            setInterface(true, btnStartPause.isActivated());
        });

        tvTimer.setOnClickListener(v -> {
            setInterface(false, btnStartPause.isActivated());
        });
    }
    public LiveData<Boolean> getIsTimerStart() {
        return viewModel.getIsTimerStart();
    }

    private void setNumberPicker(){
        for(NumberPicker np : numberPicker){
            np.setMinValue(MIN);
            np.setMaxValue(Objects.equals(np,npHour)? MAX_HOUR: MAX_MINUTE_SECOND);
            np.setFormatter(value -> String.format(Locale.getDefault(),"%02d", value));
            np.setValue(MIN);
            np.setWrapSelectorWheel(true);
            np.setOnValueChangedListener((picker, oldVal, newVal) -> {
                viewModel.setTimer(npHour.getValue(), npMinute.getValue(), npSecond.getValue());
            });
        }
    }
    private void resetNumberPickers() {
        for (NumberPicker np : numberPicker) {
            np.setValue(0);
        }
    }

    private void setInterface(boolean isTimer, boolean isStart){
        if(isStart){
            setUi(View.GONE, View.VISIBLE, View.INVISIBLE, View.VISIBLE, false, false);
        }else{
            if(isTimer){
                setUi(View.VISIBLE, View.GONE, View.INVISIBLE, View.VISIBLE, false, true);
            }else{
                setUi(View.GONE, View.VISIBLE, View.VISIBLE, View.INVISIBLE, true, false);
            }
        }
    }

    private void setUi(int timerVisibility, int taskVisibility, int clockVisibility, int textVisibility, boolean clockClickable, boolean textClickable){
        llTimer.setVisibility(timerVisibility);
        rvTask.setVisibility(taskVisibility);
        tvTimer.setVisibility(textVisibility);
        tcTimer.setVisibility(clockVisibility);
        tcTimer.setClickable(clockClickable);
        tvTimer.setClickable(textClickable);
    }
    public void setBtnUi(boolean isActivated){
        btnStartPause.setActivated(isActivated);
        ObjectAnimator animator = ObjectAnimator.ofFloat(btnStartPause, "rotation", isActivated? 0f: -90f, isActivated? -90f: 0f);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        tvStart.setActivated(isActivated);
        tvEnd.setActivated(!isActivated);
    }

}
