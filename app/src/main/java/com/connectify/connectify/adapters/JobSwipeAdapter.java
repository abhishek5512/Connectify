package com.connectify.connectify.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.connectify.connectify.CompanyProfileActivity;
import com.connectify.connectify.R;
import com.connectify.connectify.models.Job;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class JobSwipeAdapter extends RecyclerView.Adapter<JobSwipeAdapter.JobViewHolder> {

    private final List<Job> jobList;

    public JobSwipeAdapter(List<Job> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        if (job == null) return;

        holder.tvJobTitle.setText(job.getTitle());
        holder.tvCompanyName.setText(job.getCompany());
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText("Salary: " + job.getSalary());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employers").document(job.getEmployerEmail()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String logoUrl = documentSnapshot.getString("logoUrl");
                        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                    .load(logoUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.company_avatar)
                                    .error(R.drawable.company_avatar)
                                    .into(holder.ivCompanyLogo);
                        } else {
                            holder.ivCompanyLogo.setImageResource(R.drawable.company_avatar);
                        }
                    }
                });

        // View Company button logic
        holder.btnViewCompany.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, CompanyProfileActivity.class);
            intent.putExtra("employerEmail", job.getEmployerEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void removeJob(int position) {
        jobList.remove(position);
        notifyItemRemoved(position);
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvCompanyName, tvLocation, tvSalary;
        ImageView ivCompanyLogo;
        Button btnViewCompany;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            ivCompanyLogo = itemView.findViewById(R.id.companyLogo);
            btnViewCompany = itemView.findViewById(R.id.btnViewCompany);
        }
    }
}