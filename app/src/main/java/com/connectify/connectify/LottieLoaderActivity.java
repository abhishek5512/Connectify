package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class LottieLoaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Request full screen, hide status bar and title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getSupportActionBar().hide(); // Hides action bar if present

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie_loader);

        LottieAnimationView lottieView = findViewById(R.id.lottieLoader);
        lottieView.setAnimation("anim_chat.json");
        lottieView.playAnimation();

        new Handler().postDelayed(() -> {
            String receiverEmail = getIntent().getStringExtra("receiverEmail");

            Intent intent = new Intent(LottieLoaderActivity.this, ChatActivity.class);
            intent.putExtra("receiverEmail", receiverEmail);
            startActivity(intent);
            finish();
        }, 1500); // 1.5 seconds
    }
}
