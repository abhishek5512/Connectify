package com.connectify.connectify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.JobAdapter;
import com.connectify.connectify.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class PostJobFragment extends Fragment {
    private EditText etJobTitle, etJobDescription, etSalary, etLocation, etJobType;
    private Button btnPostJob, btnViewPostedJobs;
    private ProgressBar progressBar;
    private RecyclerView recyclerPostedJobs;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private JobAdapter jobAdapter;
    private List<Job> postedJobsList = new ArrayList<>();
    private boolean isJobsVisible = false; // Toggle state

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_job, container, false);

        etJobTitle = view.findViewById(R.id.etJobTitle);
        etJobDescription = view.findViewById(R.id.etJobDescription);
        etSalary = view.findViewById(R.id.etSalary);
        etLocation = view.findViewById(R.id.etLocation);
        etJobType = view.findViewById(R.id.etJobType);
        btnPostJob = view.findViewById(R.id.btnPostJob);
        btnViewPostedJobs = view.findViewById(R.id.btnViewPostedJobs);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerPostedJobs = view.findViewById(R.id.recyclerPostedJobs);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        recyclerPostedJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        jobAdapter = new JobAdapter(postedJobsList, true);
        recyclerPostedJobs.setAdapter(jobAdapter);

        btnPostJob.setOnClickListener(v -> postJob());

        btnViewPostedJobs.setOnClickListener(v -> {
            if (!isJobsVisible) {
                loadPostedJobs(); // Load and show jobs
            } else {
                recyclerPostedJobs.setVisibility(View.GONE); // Hide jobs
                btnViewPostedJobs.setText("VIEW POSTED JOBS");
                isJobsVisible = false;
            }
        });

        return view;
    }

    private void postJob() {
        String title = etJobTitle.getText().toString().trim();
        String description = etJobDescription.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String jobType = etJobType.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(salary) || TextUtils.isEmpty(location) || TextUtils.isEmpty(jobType)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Job Posted Successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();
                        if (isJobsVisible) {
                            loadPostedJobs(); // Reload if already visible
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Failed to fetch company details!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPostedJobs() {
        if (currentUser == null) return;

        String userEmail = currentUser.getEmail();
        recyclerPostedJobs.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("jobs").document(userEmail).collection("userJobs")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postedJobsList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        postedJobsList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();
                    btnViewPostedJobs.setText("HIDE POSTED JOBS");
                    isJobsVisible = true;
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load posted jobs.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
    }

    private void clearFields() {
        etJobTitle.setText("");
        etJobDescription.setText("");
        etSalary.setText("");
        etLocation.setText("");
        etJobType.setText("");
    }
}