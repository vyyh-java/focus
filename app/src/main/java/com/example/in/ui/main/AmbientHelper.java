package com.example.in.ui.main;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.example.in.R;

public class AmbientHelper {
    private final Context context;
    private final TransitionDrawable tdRain, tdForest, tdCamp;
    private final ImageButton ibRain, ibForest, ibCamp;
    private AmbientViewModel viewModel;

    private int previousId = -1;

    public <T extends LifecycleOwner & ViewModelStoreOwner> AmbientHelper(View rootView, Context context, T owner) {
        this.context = context;
        this.viewModel = new ViewModelProvider(owner).get(AmbientViewModel.class);
        this.tdRain = (TransitionDrawable) ((ImageView) rootView.findViewById(R.id.IVRain)).getDrawable();
        this.tdForest = (TransitionDrawable) ((ImageView) rootView.findViewById(R.id.IVForest)).getDrawable();
        this.tdCamp = (TransitionDrawable) ((ImageView) rootView.findViewById(R.id.IVCamp)).getDrawable();
        this.ibRain = rootView.findViewById(R.id.IBRain);
        this.ibForest = rootView.findViewById(R.id.IBForest);
        this.ibCamp = rootView.findViewById(R.id.IBCamp);

        setListener(ibRain, R.raw.rain);
        setListener(ibForest, R.raw.forest);
        setListener(ibCamp, R.raw.camp);

        //control transition?
        viewModel.getCurrentPlayingResId().observe(owner, resId -> {
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
    }
    public void stopAmbient(){
        Integer currentId = viewModel.getCurrentPlayingResId().getValue();
        if (currentId != null && currentId != -1) {
            viewModel.stop();
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
                viewModel.play(resId);
            }else{
                viewModel.stop();
            }
        });
    }
}

