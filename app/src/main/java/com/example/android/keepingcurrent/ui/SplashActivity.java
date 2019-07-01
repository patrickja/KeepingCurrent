package com.example.android.keepingcurrent.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.keepingcurrent.R;

public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private final Runnable startMain =
            () -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handler.postDelayed(startMain, 1000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(startMain);
        super.onDestroy();
    }
}
