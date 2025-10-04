package com.connectify.connectify.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.connectify.connectify.ChatActivity;
import com.connectify.connectify.R;
import com.connectify.connectify.SeekerProfileActivity;
import com.connectify.connectify.models.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private List<Match> matchList;
    private FirebaseFirestore db;
    private Context context;

    public MatchAdapter(List<Match> matchList, FirebaseFirestore db, Context context) {
        this.matchList = matchList;
        this.db = db;
        this.context = context;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_item, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        String employerEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String seekerEmail = match.getSeekerEmail();
        String chatId = getChatId(employerEmail, seekerEmail);

        holder.tvSeekerName.setText(match.getSeekerName());
        holder.tvSeekerEmail.setText(seekerEmail);
        holder.tvSeekerAge.setText(match.getSeekerAge() != null ? "Age: " + match.getSeekerAge() : "Age: N/A");
        holder.tvSeekerQualification.setText(match.getSeekerQualification() != null ? "Qualification: " + match.getSeekerQualification() : "Qualification: N/A");
        holder.tvSeekerSkills.setText(match.getSeekerSkills() != null ? "Skills: " + String.join(", ", match.getSeekerSkills()) : "Skills: N/A");

        if (match.getSeekerProfileImage() != null) {
            Glide.with(context).load(match.getSeekerProfileImage()).circleCrop().into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.btnAccept.setOnClickListener(v -> {
            holder.btnAccept.setEnabled(false);

            Map<String, Object> matchData = new HashMap<>();
            matchData.put("seekerEmail", seekerEmail);
            matchData.put("seekerName", match.getSeekerName());
            matchData.put("seekerProfileImage", match.getSeekerProfileImage());
            matchData.put("seekerSkills", match.getSeekerSkills());
            matchData.put("seekerAge", match.getSeekerAge());
            matchData.put("seekerQualification", match.getSeekerQualification());

            db.collection("matches").document(employerEmail)
                    .collection("matchedSeekers").document(seekerEmail)
                    .set(matchData)
                    .addOnSuccessListener(aVoid -> {
                        // Remove the accepted match from the list
                        int position1 = matchList.indexOf(match);
                        if (position1 != -1) {
                            matchList.remove(position1);
                            notifyItemRemoved(position1);
                        }

                        Toast.makeText(context, "Match Accepted! Redirecting to Chat...", Toast.LENGTH_SHORT).show();

                        long now = System.currentTimeMillis();

                        Map<String, Object> chatMeta = new HashMap<>();
                        chatMeta.put("participants", Arrays.asList(employerEmail, seekerEmail));
                        chatMeta.put("lastMessage", "Chat started...");
                        chatMeta.put("timestamp", now);

                        db.collection("chats").document(chatId).set(chatMeta).addOnSuccessListener(unused -> {
                            createChatPreview(employerEmail, seekerEmail, match.getSeekerName(), match.getSeekerProfileImage(), now);
                            createChatPreview(seekerEmail, employerEmail, "Employer", null, now);

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("receiverEmail", seekerEmail);
                            context.startActivity(intent);
                        });
                    })
                    .addOnFailureListener(e -> {
                        holder.btnAccept.setEnabled(true);
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        holder.btnDelete.setOnClickListener(v -> {
            db.collection("matches").document(employerEmail)
                    .collection("matchedSeekers").document(seekerEmail)
                    .delete().addOnSuccessListener(aVoid -> {
                        matchList.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Match deleted", Toast.LENGTH_SHORT).show();
                    });
        });

        // ✅ Visit Profile Button Action
        holder.btnVisitProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, SeekerProfileActivity.class);
            intent.putExtra("seekerEmail", seekerEmail);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeekerName, tvSeekerEmail, tvSeekerAge, tvSeekerQualification, tvSeekerSkills;
        ImageView profileImage;
        Button btnAccept, btnDelete, btnVisitProfile;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvSeekerName = itemView.findViewById(R.id.tvSeekerName);
            tvSeekerEmail = itemView.findViewById(R.id.tvSeekerEmail);
            tvSeekerAge = itemView.findViewById(R.id.tvAge);
            tvSeekerQualification = itemView.findViewById(R.id.tvQualification);
            tvSeekerSkills = itemView.findViewById(R.id.tvSkills);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnVisitProfile = itemView.findViewById(R.id.btnVisitProfile);
        }
    }

    private void createChatPreview(String userEmail, String partnerEmail, String partnerName, String partnerImage, long timestamp) {
        Map<String, Object> preview = new HashMap<>();
        preview.put("chatPartnerEmail", partnerEmail);
        preview.put("chatPartnerName", partnerName);
        preview.put("chatPartnerProfile", partnerImage);
        preview.put("lastMessage", "Chat started...");
        preview.put("timestamp", timestamp);

        db.collection("chatList").document(userEmail)
                .collection("chats").document(partnerEmail)
                .set(preview)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreSuccess", "Chat preview created for: " + userEmail))
                .addOnFailureListener(e -> Log.e("FirestoreError", "Failed to create chat preview for " + userEmail + ": " + e.getMessage()));
    }

    private String getChatId(String email1, String email2) {
        return email1.compareTo(email2) < 0 ? email1 + "_" + email2 : email2 + "_" + email1;
    }
}