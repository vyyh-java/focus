package com.example.in.ui.main;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionError;
import androidx.media3.session.SessionResult;
import androidx.media3.session.SessionToken;

import com.example.in.data.entity.Ambient;
import com.example.in.repository.AmbientRepository;
import com.example.in.service.PlaybackService;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class AmbientViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> currentPlayingResId = new MutableLiveData<>(-1);
    private final List<Ambient> ambients;
    private final AmbientRepository repository = new AmbientRepository();
    private final MutableLiveData<Long> remainingTimer = new MutableLiveData<>(0L);

    private MediaController mediaController = null;
    private ListenableFuture<MediaController> controllerFuture = null;
    private Player.Listener playerListener;

    public AmbientViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();
        ambients = repository.getAmbient(context);
        createController(context);
    }

    public LiveData<Long> getRemainingTimer() {
        return remainingTimer;
    }

    public MutableLiveData<Integer> getCurrentPlayingResId() {
        return currentPlayingResId;
    }

    private MediaItem getAmbient(int restId) {
        for (Ambient ambient : ambients) {
            if (ambient.getResId() == restId) {
                return repository.convertIntoMediaItem(ambient);
            }
        }
        return null;
    }

    public void play(int restId) {
        MediaItem mediaItem = getAmbient(restId);
        if (mediaController != null && mediaItem != null) {
            mediaController.setMediaItem(mediaItem);
            mediaController.prepare();
            mediaController.play();
        }
    }

    public void stop() {
        if (mediaController != null) {
            mediaController.stop();
            mediaController.clearMediaItems();
            currentPlayingResId.setValue(-1);
        }
    }

    private void updateCurrentPlayingId() {
        if (mediaController != null && mediaController.getCurrentMediaItem() != null) {
            try {
                String mediaId = mediaController.getCurrentMediaItem().mediaId;
                currentPlayingResId.setValue(Integer.parseInt(mediaId));
            } catch (NumberFormatException e) {
                currentPlayingResId.setValue(-1);
            }
        }
    }

    public void startTimer(long millis) {
        Bundle bundle = new Bundle();
        bundle.putLong("KEY_TOTAL_TIME", millis);
        SessionCommand startCommand = new SessionCommand("COMMAND_START_TIMER", Bundle.EMPTY);
        if (mediaController != null) {
            mediaController.sendCustomCommand(startCommand, bundle);
        }
    }

    public void stopTimer() {
        SessionCommand stopCommand = new SessionCommand("COMMAND_STOP_TIMER", Bundle.EMPTY);
        if (mediaController != null) {
            mediaController.sendCustomCommand(stopCommand, Bundle.EMPTY);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void createController(Context context) {
        SessionToken sessionToken = new SessionToken(context, new ComponentName(context, PlaybackService.class));
        controllerFuture = new MediaController.Builder(context, sessionToken)
                .setListener(new MediaController.Listener() {
                    @OptIn(markerClass = UnstableApi.class)
                    @Override
                    public ListenableFuture<SessionResult> onCustomCommand(@NonNull MediaController controller, @NonNull SessionCommand command, @NonNull Bundle args) {
                        if ("COMMAND_UPDATE_TIMER_UI".equals(command.customAction)) {
                            long remainingMillis = args.getLong("KEY_REMAINING_TIME", 0L);
                            remainingTimer.postValue(remainingMillis);
                            return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_SUCCESS));
                        }
                        return Futures.immediateFuture(new SessionResult(SessionError.ERROR_UNKNOWN));
                    }
                })
                .buildAsync();

        controllerFuture.addListener(() -> {
            try {
                if (controllerFuture == null || controllerFuture.isCancelled()) {
                    return;
                }
                mediaController = controllerFuture.get();
                mediaController.setRepeatMode(Player.REPEAT_MODE_ONE);
                updateCurrentPlayingId();
                playerListener = new Player.Listener() {
                    @Override
                    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                        if (mediaItem != null) {
                            try {
                                currentPlayingResId.setValue(Integer.parseInt(mediaItem.mediaId));
                            } catch (NumberFormatException e) {
                                currentPlayingResId.setValue(-1);
                            }
                        } else {
                            currentPlayingResId.setValue(-1);
                        }
                    }
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                            currentPlayingResId.setValue(-1);
                        }
                    }
                };
                mediaController.addListener(playerListener);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getApplication()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mediaController != null) {
            if (playerListener != null) {
                mediaController.removeListener(playerListener);
                playerListener = null;
            }
            mediaController = null;
        }
        if (controllerFuture != null) {
            if (!controllerFuture.isDone()) {
                controllerFuture.cancel(true);
            }
            MediaController.releaseFuture(controllerFuture);
            controllerFuture = null;
        }
    }
}
