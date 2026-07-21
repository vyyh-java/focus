package com.example.in.ui.main;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private ClockTimerViewModel timerViewModel;

    private TextView tvTimer;
    private TextClock tcTimer;

    private LinearLayout llTimer;
    private RecyclerView rvTask;

    private ImageButton btnStartPause;

    private TextView tvStart, tvEnd;

    private final Context context;
    private final TransitionDrawable tdRain, tdForest, tdCamp;
    private final ImageButton ibRain, ibForest, ibCamp;
    private AmbientViewModel ambientViewModel;

    private int previousId = -1;

    private static final int  MAX_HOUR = 3;
    private static final int  MAX_MINUTE_SECOND = 59;
    private static final int MIN = 0;
    private final List<NumberPicker> numberPicker = new ArrayList<>();

    public <T extends LifecycleOwner & ViewModelStoreOwner> ClockTimerHelper(View rootView, T owner, Context context){

        this.timerViewModel = new ViewModelProvider(owner).get(ClockTimerViewModel.class);
        this.ambientViewModel = new ViewModelProvider(owner).get(AmbientViewModel.class);
        npHour = (NumberPicker) rootView.findViewById(R.id.NPHour);
        npMinute = (NumberPicker) rootView.findViewById(R.id.NPMinute);
        npSecond = (NumberPicker) rootView.findViewById(R.id.NPSecond);
        tvTimer = (TextView) rootView.findViewById(R.id.TVTimer);
        tcTimer = (TextClock) rootView.findViewById(R.id.TCTimer);
        btnStartPause = (ImageButton) rootView.findViewById(R.id.IBtoggler);
        tvStart = (TextView) rootView.findViewById(R.id.TVStart);
        tvEnd = (TextView) rootView.findViewById(R.id.TVEnd);
        this.context = context;
        this.tdRain = (TransitionDrawable) ((ImageView) rootView.findViewById(R.id.IVRain)).getDrawable();
        this.tdForest = (TransitionDrawable) ((ImageView) rootView.findViewById(R.id.IVForest)).getDrawable();
        this.tdCamp = (TransitionDrawable) ((ImageView) rootView.findViewById(R.id.IVCamp)).getDrawable();
        this.ibRain = rootView.findViewById(R.id.IBRain);
        this.ibForest = rootView.findViewById(R.id.IBForest);
        this.ibCamp = rootView.findViewById(R.id.IBCamp);
        numberPicker.addAll(List.of(npHour, npMinute, npSecond));

        this.llTimer = (LinearLayout) rootView.findViewById(R.id.LLTimer);
        this.rvTask = (RecyclerView) rootView.findViewById(R.id.RVTask);

        setBtnUi(btnStartPause.isActivated());
        setNumberPicker();
        setInterface(false, btnStartPause.isActivated());

        setListener(ibRain, R.raw.rain);
        setListener(ibForest, R.raw.forest);
        setListener(ibCamp, R.raw.camp);

        //control transition?
        ambientViewModel.getCurrentPlayingResId().observe(owner, resId -> {
            //has previous song?
            if(previousId != -1){
                animateUnGlow(previousId);
                setButtonSelected(previousId, false);
            }
            //has new song?
            if(resId != -1){
                setButtonSelected(resId, true);
                animateGlow(resId);
            }else{
                setButtonSelected(resId, true);
                animateUnGlow(resId);
            }
            previousId = resId;
        });

        timerViewModel.getTimerText().observe(owner, timerText -> {
            String text = timerText.isEmpty() ? "00:00:00" : timerText;
            tvTimer.setText(text);
        });

        timerViewModel.getIsTimerStart().observe(owner, isStarted -> {
            setBtnUi(isStarted);
            setInterface(false, isStarted);
            if(!isStarted){
                resetNumberPickers();
                stopAmbient();
            }
        });

        ambientViewModel.getRemainingTimer().observe(owner, millis -> {
            if(millis != null){
                if(millis <= 0){
                    timerViewModel.onFinished();
                    return;
                }
                timerViewModel.onTicked(millis);
            }
        });

        btnStartPause.setOnClickListener(v -> {
            boolean isToStart = !v.isActivated();
            if(isToStart){
                if(tvTimer.getText().toString().equals("00:00:00")){
                    Toast.makeText(v.getContext(), "Please set timer", Toast.LENGTH_SHORT).show();
                    return;
                }
                //send to service using contoller
                if(timerViewModel.getTimerValue().getValue() != null){
                    ambientViewModel.startTimer(timerViewModel.getTimerValue().getValue());
                }
            }else{
                //send to service using controller
                timerViewModel.onCanceled();
                ambientViewModel.stopTimer();
            }
        });

        tcTimer.setOnClickListener(v -> {
            setInterface(true, btnStartPause.isActivated());
        });

        tvTimer.setOnClickListener(v -> {
            setInterface(false, btnStartPause.isActivated());
        });
    }

    private void setNumberPicker(){
        for(NumberPicker np : numberPicker){
            np.setMinValue(MIN);
            np.setMaxValue(Objects.equals(np,npHour)? MAX_HOUR: MAX_MINUTE_SECOND);
            np.setFormatter(value -> String.format(Locale.getDefault(),"%02d", value));
            np.setValue(MIN);
            np.setWrapSelectorWheel(true);
            np.setOnValueChangedListener((picker, oldVal, newVal) -> {
                timerViewModel.setTimer(npHour.getValue(), npMinute.getValue(), npSecond.getValue());
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
        LayerDrawable ld = (LayerDrawable) btnStartPause.getDrawable();
        RotateDrawable rot = (RotateDrawable) ld.findDrawableByLayerId(R.id.LineTimer);
        if (rot != null) {
            int targetLevel = isActivated ? 8750 : 1250;
            rot.setAlpha(isActivated ? 255 : 180);
            rot.setLevel(targetLevel);
        }
        /*
        ObjectAnimator animator = ObjectAnimator.ofFloat(btnStartPause, "rotation", isActivated? 0f: -90f, isActivated? -90f: 0f);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();*/
        tvStart.setActivated(isActivated);
        tvEnd.setActivated(!isActivated);
    }

    public void stopAmbient(){
        Integer currentId = ambientViewModel.getCurrentPlayingResId().getValue();
        if (currentId != null && currentId != -1) {
            ambientViewModel.stop();
        } else {
            //if alarm
        }
    }

    private void animateGlow(int id) {
        TransitionDrawable td = getTransitionDrawableById(id);
        if (td != null) {
            td.startTransition(500);
        }
    }

    private void animateUnGlow(int id) {
        TransitionDrawable td = getTransitionDrawableById(id);
        if (td != null) {
            td.reverseTransition(500);
        }
    }

    private TransitionDrawable getTransitionDrawableById(int id) {
        if (id == R.raw.rain) return tdRain;
        if (id == R.raw.forest) return tdForest;
        if (id == R.raw.camp) return tdCamp;
        return null;
    }
    private void setButtonSelected(int resId, boolean selected) {
        if (resId == R.raw.rain) ibRain.setSelected(selected);
        else if (resId == R.raw.forest) ibForest.setSelected(selected);
        else if (resId == R.raw.camp) ibCamp.setSelected(selected);
    }

    //control on off
    private void setListener(ImageButton ib, int resId){
        ib.setOnClickListener(v -> {
            boolean isSelected = ib.isSelected();
            if(!isSelected){
                ambientViewModel.play(resId);
            }else{
                ambientViewModel.stop();
            }
        });
    }
}
