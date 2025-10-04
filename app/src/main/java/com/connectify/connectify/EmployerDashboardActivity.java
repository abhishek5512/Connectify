package com.connectify.connectify;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EmployerDashboardActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment currentFragment = new MatchesFragment(); // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_dashboard);

        bottomNavigationView = findViewById(R.id.employerBottomNavigation);

        if (savedInstanceState == null) {
            View rootLayout = findViewById(R.id.employerFragmentContainer);
            rootLayout.post(() -> playLottieThenLoad(new MatchesFragment(), "anim_matches.json"));
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_matches) {
                playLottieThenLoad(new MatchesFragment(), "anim_matches.json");
                return true;
            } else if (itemId == R.id.nav_post) {
                playLottieThenLoad(new PostJobFragment(), "anim_post.json");
                return true;
            } else if (itemId == R.id.nav_chat) {
                playLottieThenLoad(new ChatsFragment(), "anim_chat.json");
                return true;
            } else if (itemId == R.id.nav_profile) {
                playLottieThenLoad(new EmployerProfileFragment(), "anim_profile.json");
                return true;
            }

            return false;
        });
    }


    private void playLottieThenLoad(Fragment fragment, String animationFile) {
        RelativeLayout lottieOverlay = findViewById(R.id.lottieOverlay);
        LottieAnimationView lottieView = findViewById(R.id.lottieLoaderMatches); // Shared view
        View fragmentContainer = findViewById(R.id.employerFragmentContainer);

        fragmentContainer.setVisibility(View.INVISIBLE);
        lottieOverlay.setVisibility(View.VISIBLE);
        lottieView.setAnimation(animationFile);
        lottieView.setRepeatCount(com.airbnb.lottie.LottieDrawable.INFINITE); // ✅ Enable looping
        lottieView.playAnimation();

        new Handler().postDelayed(() -> {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.employerFragmentContainer, fragment)
                    .commit();

            new Handler().postDelayed(() -> {
                lottieView.cancelAnimation(); // ✅ Stop loop
                lottieOverlay.setVisibility(View.GONE);
                fragmentContainer.setVisibility(View.VISIBLE);
                currentFragment = fragment;
            }, 100);
        }, 1000); // ⏱ Keep this or change to 2000 if needed
    }


    private void animateFragmentSwitch(Fragment selectedFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (getFragmentIndex(selectedFragment) > getFragmentIndex(currentFragment)) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        currentFragment = selectedFragment;
        transaction.replace(R.id.employerFragmentContainer, selectedFragment).commit();
    }

    private int getFragmentIndex(Fragment fragment) {
        if (fragment instanceof MatchesFragment) return 0;
        if (fragment instanceof PostJobFragment) return 1;
        if (fragment instanceof ChatsFragment) return 2;
        if (fragment instanceof EmployerProfileFragment) return 3;
        return -1;
    }
}
