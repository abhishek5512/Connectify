package com.connectify.connectify.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.connectify.connectify.CompanyProfileActivity;
import com.connectify.connectify.R;
import com.connectify.connectify.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private final List<Job> jobList;
    private final boolean isEmployerView;

    public JobAdapter(List<Job> jobList, boolean isEmployerView) {
        this.jobList = jobList;
        this.isEmployerView = isEmployerView;
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
        holder.tvCompanyName.setText(
                job.getCompany() != null && !job.getCompany().isEmpty() ? job.getCompany() : "Unknown Company"
        );
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText("₹ " + job.getSalary());

        if (isEmployerView) {
            // Employer View: Hide logo & View Company, show delete
            holder.companyLogo.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnViewCompany.setVisibility(View.GONE);
            holder.btnDelete.setOnClickListener(v -> deleteJob(job, holder));
        } else {
            // Seeker View: Show logo & View Company, hide delete
            holder.companyLogo.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnViewCompany.setVisibility(View.VISIBLE);
            loadCompanyLogoFromJobData(job, holder);

            holder.btnViewCompany.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), CompanyProfileActivity.class);
                intent.putExtra("employerEmail", job.getPostedBy());
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }

    private void loadCompanyLogoFromJobData(Job job, JobViewHolder holder) {
        String logoUrl = job.getCompanyLogoUrl();

        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(logoUrl)
                    .placeholder(R.drawable.company_avatar)
                    .error(R.drawable.company_avatar)
                    .circleCrop()
                    .into(holder.companyLogo);
        } else {
            holder.companyLogo.setImageResource(R.drawable.company_avatar);
        }
    }

    private void deleteJob(Job job, JobViewHolder holder) {
        String employerEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (employerEmail == null || job.getJobId() == null) {
            Toast.makeText(holder.itemView.getContext(), "Error: Missing job ID or employer email!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("jobs").document(employerEmail)
                .collection("userJobs").document(job.getJobId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    jobList.remove(job);
                    notifyDataSetChanged();
                    Toast.makeText(holder.itemView.getContext(), "Job deleted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Failed to delete job!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvCompanyName, tvLocation, tvSalary;
        Button btnDelete, btnViewCompany;
        ImageView companyLogo;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewCompany = itemView.findViewById(R.id.btnViewCompany);
            companyLogo = itemView.findViewById(R.id.companyLogo);
        }
    }
}
