package com.connectify.connectify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.MatchAdapter;
import com.connectify.connectify.models.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchesFragment extends Fragment {

    private RecyclerView matchesRecyclerView;
    private MatchAdapter matchAdapter;
    private List<Match> matchList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String employerEmail;

    public MatchesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        matchesRecyclerView = view.findViewById(R.id.recyclerViewMatches);
        matchesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        matchList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        employerEmail = auth.getCurrentUser().getEmail();
        matchAdapter = new MatchAdapter(matchList, firestore, getContext());
        matchesRecyclerView.setAdapter(matchAdapter);

        loadMatches();
        return view;
    }

    private void loadMatches() {
        firestore.collection("matches")
                .document(employerEmail)
                .collection("matchedSeekers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    matchList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Match match = doc.toObject(Match.class);
                        matchList.add(match);
                    }
                    matchAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load matches", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ Triggered when Employer Accepts Seeker
    private void onAcceptClicked(Match match) {
        String seekerEmail = match.getSeekerEmail();
        String chatId = getChatId(employerEmail, seekerEmail);

        // 1. Create Chat Room with participants if not exist
        DocumentReference chatRef = firestore.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Map<String, Object> chatRoomData = new HashMap<>();
                chatRoomData.put("participants", Arrays.asList(employerEmail, seekerEmail));
                chatRoomData.put("lastMessage", "Chat started...");
                chatRoomData.put("timestamp", System.currentTimeMillis());

                chatRef.set(chatRoomData);
            }
        });

        // 2. Create chatList preview for employer and seeker
        createChatPreview(employerEmail, seekerEmail);
        createChatPreview(seekerEmail, employerEmail);

        // 3. Navigate to ChatActivity
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("receiverEmail", seekerEmail);
        startActivity(intent);
    }

    private void createChatPreview(String userEmail, String partnerEmail) {
        firestore.collection("users").document(partnerEmail).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.getString("name");
                String profileImage = snapshot.getString("profileImage");

                Map<String, Object> chatPreview = new HashMap<>();
                chatPreview.put("chatPartnerEmail", partnerEmail);
                chatPreview.put("chatPartnerName", name != null ? name : "Unknown");
                chatPreview.put("chatPartnerProfile", profileImage);
                chatPreview.put("lastMessage", "Chat started...");
                chatPreview.put("timestamp", System.currentTimeMillis());

                firestore.collection("chatList")
                        .document(userEmail)
                        .collection("chats")
                        .document(partnerEmail)
                        .set(chatPreview);
            }
        });
    }

    private String getChatId(String email1, String email2) {
        return email1.compareTo(email2) < 0 ? email1 + "_" + email2 : email2 + "_" + email1;
    }
}
