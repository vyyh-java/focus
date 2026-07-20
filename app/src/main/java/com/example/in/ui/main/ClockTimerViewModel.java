package com.example.in.ui.main;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Locale;
import java.util.Map;

//timer logic
public class ClockTimerViewModel extends ViewModel {
    private final MutableLiveData<String> timerText = new MutableLiveData<>();
    private final MutableLiveData<Integer> timerValue = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTimerStart = new MutableLiveData<>();

    public LiveData<String> getTimerText() {
        return timerText;
    }
    public LiveData<Boolean> getIsTimerStart() {
        return isTimerStart;
    }

    public void onTicked(long l){
        if (Boolean.FALSE.equals(isTimerStart.getValue())) {
            isTimerStart.setValue(true);
        }
        timerValue.setValue((int) l);
        long hour = l / 3600000;
        long minute = (l % 3600000) / 60000;
        long second = (l % 60000) / 1000;
        timerText.setValue(format(hour, minute, second));
    }

    public void onFinished(){
        timerText.setValue("00:00:00");
        timerValue.setValue(0);
        isTimerStart.setValue(false);
    }

    public void onCanceled(){
        isTimerStart.setValue(false);
    }

    public void setTimer(int hour, int minute, int second){
        timerValue.setValue((hour * 3600000) + (minute * 60000) + (second * 1000));
        timerText.setValue(format(hour, minute, second));
    }
    private String format(long hour, long minute, long second){
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
    }

    public LiveData<Integer> getTimerValue() {
        return timerValue;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

    }

}
