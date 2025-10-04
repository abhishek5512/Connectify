package com.connectify.connectify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.JobSwipeAdapter;
import com.connectify.connectify.models.Match;
import com.connectify.connectify.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class JobsFragment extends Fragment {
    private RecyclerView recyclerView;
    private JobSwipeAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String seekerEmail;

    private Set<String> dislikedJobs = new HashSet<>();
    private Set<String> matchedJobs = new HashSet<>();

    public JobsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jobList = new ArrayList<>();
        jobAdapter = new JobSwipeAdapter(jobList);
        recyclerView.setAdapter(jobAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        seekerEmail = auth.getCurrentUser().getEmail();

        fetchDislikedJobs();

        setupSwipeGesture();

        return view;
    }

    private void fetchDislikedJobs() {
        db.collection("swipes").document(seekerEmail).collection("dislikedJobs")
                .get()
                .addOnSuccessListener(dislikedSnapshot -> {
                    for (QueryDocumentSnapshot doc : dislikedSnapshot) {
                        dislikedJobs.add(doc.getId());
                    }

                    fetchMatchedJobs();
                })
                .addOnFailureListener(e -> fetchMatchedJobs()); // Continue even if error
    }

    private void fetchMatchedJobs() {
        db.collectionGroup("matchedSeekers")
                .whereEqualTo("seekerEmail", seekerEmail)
                .get()
                .addOnSuccessListener(matchedSnapshot -> {
                    for (QueryDocumentSnapshot doc : matchedSnapshot) {
                        String jobTitle = doc.getString("jobTitle");
                        String company = doc.getString("company");
                        String key = jobTitle + "_" + company;
                        matchedJobs.add(key);
                    }

                    fetchAllJobs();
                })
                .addOnFailureListener(e -> fetchAllJobs()); // Continue even if error
    }

    private void fetchAllJobs() {
        progressBar.setVisibility(View.VISIBLE);

        db.collectionGroup("userJobs").get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        jobList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Job job = doc.toObject(Job.class);
                            job.setJobId(doc.getId());

                            // ✅ Set employer email manually if needed
                            if (doc.contains("employerEmail")) {
                                job.setEmployerEmail(doc.getString("employerEmail"));
                            } else if (doc.contains("postedBy")) {
                                job.setEmployerEmail(doc.getString("postedBy"));
                            }

                            // ✅ ✅ ✅ Set the company logo URL
                            if (doc.contains("companyLogoUrl")) {
                                job.setCompanyLogoUrl(doc.getString("companyLogoUrl"));
                            }

                            if (job.getEmployerEmail() == null || job.getEmployerEmail().isEmpty()) continue;

                            if (dislikedJobs.contains(job.getJobId())) continue;

                            String matchKey = job.getTitle() + "_" + job.getCompany();
                            if (matchedJobs.contains(matchKey)) continue;

                            jobList.add(job);
                        }

                        if (jobList.isEmpty()) {
                            Toast.makeText(getContext(), "No jobs available!", Toast.LENGTH_SHORT).show();
                        }

                        jobAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to load jobs!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupSwipeGesture() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Job swipedJob = jobList.get(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    saveMatchToFirestore(swipedJob);
                } else if (direction == ItemTouchHelper.LEFT) {
                    saveDislikeToFirestore(swipedJob);
                }

                jobAdapter.removeJob(position);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void saveDislikeToFirestore(Job job) {
        db.collection("swipes").document(seekerEmail)
                .collection("dislikedJobs").document(job.getJobId())
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> Log.d("SWIPE", "Disliked job saved"))
                .addOnFailureListener(e -> Log.e("SWIPE", "Failed to save disliked job", e));
    }

    private void saveMatchToFirestore(Job job) {
        String employerEmail = job.getEmployerEmail();

        if (employerEmail == null || employerEmail.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Invalid job match. Employer email is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(seekerEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String seekerName = documentSnapshot.exists() ? documentSnapshot.getString("name") : "Unknown Seeker";
                    List<String> seekerSkills = documentSnapshot.exists() ? (List<String>) documentSnapshot.get("skills") : new ArrayList<>();
                    String seekerAge = documentSnapshot.exists() ? documentSnapshot.getString("age") : "Not Available";
                    String seekerQualification = documentSnapshot.exists() ? documentSnapshot.getString("qualification") : "Not Available";
                    String seekerProfileImage = documentSnapshot.exists() ? documentSnapshot.getString("profileImage") : "";

                    Match match = new Match(seekerEmail, seekerName, seekerSkills, seekerAge, seekerQualification, seekerProfileImage, job.getTitle(), job.getCompany(), job.getLocation());

                    db.collection("matches").document(employerEmail)
                            .collection("matchedSeekers").document(seekerEmail)
                            .set(match)
                            .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Match Saved!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to Save Match!", Toast.LENGTH_SHORT).show());
                });
    }
}
