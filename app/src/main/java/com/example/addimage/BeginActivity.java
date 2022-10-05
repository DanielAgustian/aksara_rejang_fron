package com.example.addimage;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class BeginActivity extends YouTubeBaseActivity {
    private Button btnYoutube;
    private YouTubePlayerView ypv;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.begin_activity);
        ypv= findViewById(R.id.ypv);
        btnYoutube = findViewById(R.id.btnYoutube);
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo("y68V5vBFPnc");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        btnYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ypv.initialize(PlayerConfig.API_KEY, onInitializedListener);
            }
        });
    }
    public void back(View v){
        startActivity(new Intent(this,MainActivity.class));
    }
}
