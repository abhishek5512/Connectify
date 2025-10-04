package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1000;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottieView = findViewById(R.id.lottieView);
        lottieView.setAnimation("connectify_splash.json");
        lottieView.setRepeatCount(0); // ✅ Don't repeat animation
        lottieView.playAnimation();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                String email = currentUser.getEmail();
                firestore.collection("users").document(email)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                if ("Seeker".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(SplashActivity.this, JobSeekerDashboard.class));
                                } else if ("Employer".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(SplashActivity.this, EmployerDashboardActivity.class));
                                } else {
                                    startActivity(new Intent(SplashActivity.this, ChooseRoleActivity.class));
                                }
                            } else {
                                startActivity(new Intent(SplashActivity.this, ChooseRoleActivity.class));
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        });
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, SPLASH_DURATION);
    }
}
