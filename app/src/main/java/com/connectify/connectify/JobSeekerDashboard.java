package com.connectify.connectify;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class JobSeekerDashboard extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment = new JobsFragment(); // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_seeker_dashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // ✅ Wait until layout is ready, then play animation
        if (savedInstanceState == null) {
            View rootLayout = findViewById(R.id.fragment_container);
            rootLayout.post(() -> playLottieThenLoad(new JobsFragment(), "anim_jobs.json"));
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_jobs) {
                playLottieThenLoad(new JobsFragment(), "anim_jobs.json");
                return true;
            } else if (itemId == R.id.nav_chat) {
                playLottieThenLoad(new ChatsFragment(), "anim_chat.json");
                return true;
            } else if (itemId == R.id.nav_profile) {
                playLottieThenLoad(new ProfileFragment(), "anim_profile.json");
                return true;
            }
            return false;
        });
    }


    private void playLottieThenLoad(Fragment fragment, String animationName) {
        RelativeLayout lottieOverlay = findViewById(R.id.lottieOverlaySeeker);
        LottieAnimationView lottieView = findViewById(R.id.lottieLoaderSeeker);
        View fragmentContainer = findViewById(R.id.fragment_container);

        fragmentContainer.setVisibility(View.INVISIBLE);
        lottieOverlay.setVisibility(View.VISIBLE);
        lottieView.setAnimation(animationName);
        lottieView.setRepeatCount(com.airbnb.lottie.LottieDrawable.INFINITE); // ✅ LOOP the animation
        lottieView.playAnimation();

        new Handler().postDelayed(() -> {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            new Handler().postDelayed(() -> {
                lottieOverlay.setVisibility(View.GONE);
                fragmentContainer.setVisibility(View.VISIBLE);
                lottieView.cancelAnimation(); // ✅ Stop animation after transition
                currentFragment = fragment;
            }, 100);
        }, 1000); // Adjust delay if needed
    }


}
