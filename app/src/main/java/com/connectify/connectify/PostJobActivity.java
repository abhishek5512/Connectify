package com.connectify.connectify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PostJobActivity extends AppCompatActivity {
    private EditText etJobTitle, etJobDescription, etSalary, etLocation, etJobType;
    private Button btnPostJob;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_job);

        etJobTitle = findViewById(R.id.etJobTitle);
        etJobDescription = findViewById(R.id.etJobDescription);
        etSalary = findViewById(R.id.etSalary);
        etLocation = findViewById(R.id.etLocation);
        etJobType = findViewById(R.id.etJobType);
        btnPostJob = findViewById(R.id.btnPostJob);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        btnPostJob.setOnClickListener(v -> postJob());
    }

    private void postJob() {
        String title = etJobTitle.getText().toString().trim();
        String description = etJobDescription.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String jobType = etJobType.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(salary) || TextUtils.isEmpty(location) || TextUtils.isEmpty(jobType)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userEmail = currentUser.getEmail();
        String jobId = db.collection("jobs").document(userEmail).collection("userJobs").document().getId();

        db.collection("employers").document(userEmail).get().addOnSuccessListener(doc -> {
            String logoUrl = "";
            String companyName = "";
            if (doc.exists()) {
                logoUrl = doc.getString("logoUrl");
                companyName = doc.getString("companyName");
            }

            Map<String, Object> job = new HashMap<>();
            job.put("jobId", jobId);
            job.put("title", title);
            job.put("company", companyName);
            job.put("description", description);
            job.put("salary", salary);
            job.put("location", location);
            job.put("jobType", jobType);
            job.put("employerEmail", userEmail);
            job.put("companyLogoUrl", logoUrl != null ? logoUrl.trim() : "");

            db.collection("jobs").document(userEmail).collection("userJobs").document(jobId)
                    .set(job)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(PostJobActivity.this, "Job Posted Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PostJobActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));

        }).addOnFailureListener(e -> {
            Toast.makeText(PostJobActivity.this, "Failed to fetch company details!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }
}