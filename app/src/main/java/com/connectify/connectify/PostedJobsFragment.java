package com.connectify.connectify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.JobAdapter;
import com.connectify.connectify.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostedJobsFragment extends Fragment {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView noJobsText;

    public PostedJobsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posted_jobs, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPostedJobs);
        progressBar = view.findViewById(R.id.progressBar);
        noJobsText = view.findViewById(R.id.noJobsText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList, true);
        recyclerView.setAdapter(jobAdapter);

        loadPostedJobs();

        return view;
    }

    private void loadPostedJobs() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("PostedJobsFragment", "User not logged in!");
            Toast.makeText(getContext(), "Authentication failed. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String employerEmail = user.getEmail();
        if (employerEmail == null) {
            Log.e("PostedJobsFragment", "User email is null!");
            Toast.makeText(getContext(), "Failed to get user email. Try logging out and logging in again.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        noJobsText.setVisibility(View.GONE);

        CollectionReference jobRef = db.collection("jobs").document(employerEmail).collection("userJobs");

        jobRef.get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        jobList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Job job = doc.toObject(Job.class);
                            job.setJobId(doc.getId());
                            jobList.add(job);
                        }

                        if (jobList.isEmpty()) {
                            noJobsText.setVisibility(View.VISIBLE);
                            noJobsText.setText("No jobs posted yet.");
                        } else {
                            noJobsText.setVisibility(View.GONE);
                        }

                        jobAdapter.notifyDataSetChanged();
                    } else {
                        noJobsText.setText("Failed to load jobs");
                        noJobsText.setVisibility(View.VISIBLE);
                        Log.e("Firestore Error", "Error loading jobs: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("Firestore Error", "Permission Denied: ", e);
                    Toast.makeText(getContext(), "Permission Denied: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}

