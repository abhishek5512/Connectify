package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class LottieTransitionActivity extends AppCompatActivity {

    private LottieAnimationView lottieView;
    public static final String EXTRA_NEXT = "next";
    public static final String EXTRA_DATA = "data"; // Optional

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie_transition);

        lottieView = findViewById(R.id.lottieLoader);
        String next = getIntent().getStringExtra(EXTRA_NEXT);

        // Select animation based on target
        switch (next) {
            case "chat":
                lottieView.setAnimation("anim_chat.json");
                break;
            case "matches":
                lottieView.setAnimation("anim_matches.json");
                break;
            case "profile":
                lottieView.setAnimation("anim_profile.json");
                break;
            case "post":
                lottieView.setAnimation("anim_post.json");
                break;
            default:
                lottieView.setAnimation("anim_default.json");
        }

        lottieView.playAnimation();

        new Handler().postDelayed(() -> {
            Intent intent;
            switch (next) {
                case "chat":
                    intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("receiverEmail", getIntent().getStringExtra(EXTRA_DATA));
                    break;
                case "matches":
                    intent = new Intent(this, MatchesFragment.class);
                    break;
                case "profile":
                    intent = new Intent(this, EmployerProfileFragment.class);
                    break;
                case "post":
                    intent = new Intent(this, PostJobActivity.class);
                    break;
                default:
                    intent = new Intent(this, HomeActivity.class);
            }

            startActivity(intent);
            finish();
        }, 1500);
    }
}
