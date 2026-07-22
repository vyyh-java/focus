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
import com.example.in.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//control ui, listener
public class ClockTimerHelper {
    private ClockTimerViewModel timerViewModel;

    private AmbientViewModel ambientViewModel;
    private ActivityMainBinding binding;
    private int previousId = -1;
    private static final int  MAX_HOUR = 3;
    private static final int  MAX_MINUTE_SECOND = 59;
    private static final int MIN = 0;

    public <T extends LifecycleOwner & ViewModelStoreOwner> ClockTimerHelper(T owner, ActivityMainBinding binding){

        this.timerViewModel = new ViewModelProvider(owner).get(ClockTimerViewModel.class);
        this.ambientViewModel = new ViewModelProvider(owner).get(AmbientViewModel.class);
        this.binding = binding;

        setBtnUi(binding.IBtoggler.isActivated());
        setNumberPicker(binding.NPHour, binding.NPMinute, binding.NPSecond);
        setInterface(false, binding.IBtoggler.isActivated());

        setListener(binding.IBRain, R.raw.rain);
        setListener(binding.IBForest, R.raw.forest);
        setListener(binding.IBCamp, R.raw.camp);

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
            binding.TVTimer.setText(text);
        });

        timerViewModel.getIsTimerStart().observe(owner, isStarted -> {
            setBtnUi(isStarted);
            setInterface(false, isStarted);
            if(!isStarted){
                resetNumberPickers(binding.NPHour, binding.NPMinute, binding.NPSecond);
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

        binding.IBtoggler.setOnClickListener(v -> {
            boolean isToStart = !v.isActivated();
            if(isToStart){
                if(binding.TVTimer.getText().toString().equals("00:00:00")){
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

        binding.TCTimer.setOnClickListener(v -> {
            setInterface(true, binding.IBtoggler.isActivated());
        });

        binding.TVTimer.setOnClickListener(v -> {
            setInterface(false, binding.IBtoggler.isActivated());
        });
    }

    private void setNumberPicker(NumberPicker ...numberPicker){
        for(NumberPicker np : numberPicker){
            np.setMinValue(MIN);
            np.setMaxValue(Objects.equals(np,binding.NPHour)? MAX_HOUR: MAX_MINUTE_SECOND);
            np.setFormatter(value -> String.format(Locale.getDefault(),"%02d", value));
            np.setValue(MIN);
            np.setWrapSelectorWheel(true);
            np.setOnValueChangedListener((picker, oldVal, newVal) -> {
                timerViewModel.setTimer(binding.NPHour.getValue(), binding.NPMinute.getValue(), binding.NPSecond.getValue());
            });
        }
    }
    private void resetNumberPickers(NumberPicker ...numberPicker) {
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
        binding.LLTimer.setVisibility(timerVisibility);
        binding.RVTask.setVisibility(taskVisibility);
        binding.TVTimer.setVisibility(textVisibility);
        binding.TCTimer.setVisibility(clockVisibility);
        binding.TCTimer.setClickable(clockClickable);
        binding.TVTimer.setClickable(textClickable);
    }
    public void setBtnUi(boolean isActivated){
        binding.IBtoggler.setActivated(isActivated);
        LayerDrawable ld = (LayerDrawable) binding.IBtoggler.getDrawable();
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
        binding.TVStart.setActivated(isActivated);
        binding.TVEnd.setActivated(!isActivated);
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
        if (id == R.raw.rain) return (TransitionDrawable) binding.IVRain.getDrawable();
        if (id == R.raw.forest) return (TransitionDrawable) binding.IVForest.getDrawable();
        if (id == R.raw.camp) return (TransitionDrawable) binding.IVCamp.getDrawable();
        return null;
    }
    private void setButtonSelected(int resId, boolean selected) {
        if (resId == R.raw.rain) binding.IBRain.setSelected(selected);
        else if (resId == R.raw.forest) binding.IBForest.setSelected(selected);
        else if (resId == R.raw.camp) binding.IBCamp.setSelected(selected);
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

    public void release(){
        binding = null;
        timerViewModel = null;
        ambientViewModel = null;
    }
}
