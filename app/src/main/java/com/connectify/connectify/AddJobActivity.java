package com.connectify.connectify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.JobAdapter;
import com.connectify.connectify.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddJobActivity extends AppCompatActivity {

    private EditText etJobTitle, etSalary, etJobDescription, etLocation, etJobType;
    private Button btnPostJob, btnViewPostedJobs;
    private ProgressBar progressBar;
    private RecyclerView recyclerPostedJobs;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_job);

        etJobTitle = findViewById(R.id.etJobTitle);
        etSalary = findViewById(R.id.etSalary);
        etJobDescription = findViewById(R.id.etJobDescription);
        etLocation = findViewById(R.id.etLocation);
        etJobType = findViewById(R.id.etJobType);
        btnPostJob = findViewById(R.id.btnPostJob);
        btnViewPostedJobs = findViewById(R.id.btnViewPostedJobs);
        progressBar = findViewById(R.id.progressBar);
        recyclerPostedJobs = findViewById(R.id.recyclerPostedJobs);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList, true);
        recyclerPostedJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerPostedJobs.setAdapter(jobAdapter);

        btnPostJob.setOnClickListener(v -> fetchLogoAndPostJob());
        btnViewPostedJobs.setOnClickListener(v -> loadPostedJobs());
    }

    private void fetchLogoAndPostJob() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String employerEmail = user.getEmail();
        if (employerEmail == null) {
            Toast.makeText(this, "Invalid user email!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("employers").document(employerEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String logoUrl = "";
                    if (documentSnapshot.exists()) {
                        logoUrl = documentSnapshot.getString("logoUrl");
                        if (logoUrl == null) logoUrl = "";
                    }
                    postJob(logoUrl.trim());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch logo!", Toast.LENGTH_SHORT).show();
                });
    }

    private void postJob(String companyLogoUrl) {
        String jobTitle = etJobTitle.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String jobDescription = etJobDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String jobType = etJobType.getText().toString().trim();

        if (TextUtils.isEmpty(jobTitle) ||
                TextUtils.isEmpty(salary) || TextUtils.isEmpty(jobDescription) ||
                TextUtils.isEmpty(location) || TextUtils.isEmpty(jobType)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        String employerEmail = user.getEmail();
        String jobId = db.collection("jobs").document(employerEmail)
                .collection("userJobs").document().getId();

        db.collection("employers").document(employerEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String companyName = "";
                    if (documentSnapshot.exists()) {
                        companyName = documentSnapshot.getString("companyName");
                    }

                    Job job = new Job(jobTitle, companyName, salary, jobDescription, location, jobType, employerEmail, companyLogoUrl);
                    job.setJobId(jobId);

                    db.collection("jobs").document(employerEmail)
                            .collection("userJobs")
                            .document(jobId)
                            .set(job)
                            .addOnSuccessListener(documentReference -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddJobActivity.this, "Job Posted Successfully!", Toast.LENGTH_SHORT).show();
                                clearFields();
                                loadPostedJobs();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddJobActivity.this, "Failed to post job", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch company details!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPostedJobs() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String employerEmail = user.getEmail();
        if (employerEmail == null) return;

        progressBar.setVisibility(View.VISIBLE);

        db.collection("jobs").document(employerEmail).collection("userJobs")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        jobList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Job job = doc.toObject(Job.class);
                            job.setJobId(doc.getId());
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();

                        if (jobList.isEmpty()) {
                            Toast.makeText(this, "No jobs found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        etJobTitle.setText("");
        etSalary.setText("");
        etJobDescription.setText("");
        etLocation.setText("");
        etJobType.setText("");
    }
}