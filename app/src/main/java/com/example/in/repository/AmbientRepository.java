package com.example.in.repository;

import android.content.Context;
import android.health.connect.datatypes.Metadata;
import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

import com.example.in.R;
import com.example.in.data.entity.Ambient;

import java.util.ArrayList;
import java.util.List;

public class AmbientRepository {
    String packageName;

    public List<Ambient> getAmbient(Context context) {
        List<Ambient> ambients = new ArrayList<>();
        packageName = context.getPackageName();

        //set data
        ambients.add(new Ambient(R.raw.rain, "rain", convertIntoUrl("rain", "raw", packageName)));
        ambients.add(new Ambient(R.raw.forest, "forest", convertIntoUrl("forest", "raw", packageName)));
        ambients.add(new Ambient(R.raw.camp, "night", convertIntoUrl("camp", "raw", packageName)));

        return ambients;
    }

    public MediaItem convertIntoMediaItem(Ambient ambient){
        String rawName = ambient.getTitle()+"_glow";
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(ambient.getTitle())
                .setArtist("Pocket Ambient")
                .setArtworkUri(Uri.parse(convertIntoUrl(rawName, "drawable", packageName)))
                .build();
        return new MediaItem.Builder()
                .setMediaId(String.valueOf(ambient.getResId()))
                .setUri(Uri.parse(ambient.getResUrl()))
                .setMediaMetadata(metadata)
                .build();
    }

    /*
    rain, forest, night/camp
    *
    **/

    private String convertIntoUrl(String rawName, String dir, String packageName){
        return "android.resource://" + packageName + "/"+dir+"/"+rawName;
    }

}
