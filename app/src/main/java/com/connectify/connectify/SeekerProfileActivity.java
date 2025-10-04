package com.connectify.connectify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SeekerProfileActivity extends AppCompatActivity {

    private ImageView imgProfile, btnBack;
    private TextView tvName, tvAge, tvGender, tvQualification, tvExperience, tvSkills;
    private Button btnViewResume, btnDownloadResume;
    private ProgressBar progressBar;

    private String seekerEmail;
    private String resumeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_profile);

        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvQualification = findViewById(R.id.tvQualification);
        tvExperience = findViewById(R.id.tvExperience);
        tvSkills = findViewById(R.id.tvSkills);
        btnViewResume = findViewById(R.id.btnViewResume);
        btnDownloadResume = findViewById(R.id.btnDownloadResume);
        progressBar = findViewById(R.id.progressBar);

        seekerEmail = getIntent().getStringExtra("seekerEmail");

        if (seekerEmail == null || seekerEmail.isEmpty()) {
            Toast.makeText(this, "Profile not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("users").document(seekerEmail).get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);
                    if (document.exists()) {
                        loadProfileData(document);
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    finish();
                });

        btnBack.setOnClickListener(view -> onBackPressed());

        btnViewResume.setOnClickListener(v -> {
            if (resumeUrl != null && !resumeUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl));
                startActivity(browserIntent);

            } else {
                Toast.makeText(this, "Resume not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnDownloadResume.setOnClickListener(v -> {
            if (resumeUrl != null && !resumeUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Resume not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileData(DocumentSnapshot document) {
        tvName.setText(document.getString("name"));
        tvAge.setText("Age: " + document.getString("age"));
        tvGender.setText("Gender: " + document.getString("gender"));
        tvQualification.setText("Qualification: " + document.getString("qualification"));
        tvExperience.setText("Experience: " + document.getString("workExperience"));


        List<String> skillsList = (List<String>) document.get("skills");
        if (skillsList != null && !skillsList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String skill : skillsList) {
                builder.append("• ").append(skill).append("\n");
            }
            tvSkills.setText(builder.toString().trim());
        } else {
            tvSkills.setText("No skills listed");
        }

        resumeUrl = document.getString("resumeUrl");

        if (resumeUrl != null && !resumeUrl.isEmpty()) {
            btnViewResume.setVisibility(View.VISIBLE);
            btnDownloadResume.setVisibility(View.VISIBLE);
        }

        String imageUrl = document.getString("profileImage");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(imgProfile);
        }

    }
}
