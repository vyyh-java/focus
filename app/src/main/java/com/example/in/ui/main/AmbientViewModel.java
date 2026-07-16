package com.example.in.ui.main;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.example.in.data.entity.Ambient;
import com.example.in.repository.AmbientRepository;
import com.example.in.service.PlaybackService;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AmbientViewModel extends AndroidViewModel {

    private MutableLiveData<Integer> currentPlayingResId = new MutableLiveData<>(-1);
    private List<Ambient> ambients;
    private AmbientRepository repository = new AmbientRepository();
    private MediaController mediaController = null;
    private ListenableFuture<MediaController> controllerFuture = null;
    public AmbientViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();
        ambients = repository.getAmbient(context);
        createController(context);
    }
    public MutableLiveData<Integer> getCurrentPlayingResId() {
        return currentPlayingResId;
    }
    private MediaItem getAmbient(int restId) {
        for(Ambient ambient: ambients){
            if(ambient.getResId() == restId){
                return repository.convertIntoMediaItem(ambient);
            }
        }
        return null;
    }

    public void play(int restId){
        MediaItem mediaItem = getAmbient(restId);
        if (mediaController != null) {
            mediaController.setMediaItem(mediaItem);
            mediaController.prepare();
            mediaController.play();
        }
    }

    public void stop(){
        if (mediaController != null) {
            mediaController.stop();
            mediaController.clearMediaItems();
            currentPlayingResId.setValue(-1);
        }
    }
    private void updateCurrentPlayingId() {
        if (mediaController != null && mediaController.getCurrentMediaItem() != null) {
            String mediaId = mediaController.getCurrentMediaItem().mediaId;
            currentPlayingResId.setValue(Integer.parseInt(mediaId));
        }
    }

    private void createController(Context context){
        SessionToken sessionToken = new SessionToken(context, new ComponentName(context, PlaybackService.class));
        controllerFuture = new MediaController.Builder(context, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                updateCurrentPlayingId();
                mediaController = controllerFuture.get();
                mediaController.setRepeatMode(Player.REPEAT_MODE_ONE);
                MediaItem mediaItem = mediaController.getCurrentMediaItem();
                if (mediaController.isPlaying() && mediaItem != null) {
                    int runningResId = Integer.parseInt(mediaItem.mediaId);
                    currentPlayingResId.postValue(runningResId);
                }
                mediaController.addListener(new androidx.media3.common.Player.Listener() {
                    @Override
                    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                        if (mediaItem != null) {
                            currentPlayingResId.setValue(Integer.parseInt(mediaItem.mediaId));
                        } else {
                            currentPlayingResId.setValue(-1);
                        }
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getApplication()));
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
        }
    }
}
