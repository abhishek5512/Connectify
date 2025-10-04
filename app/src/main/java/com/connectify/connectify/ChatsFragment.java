package com.connectify.connectify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.ChatListAdapter;
import com.connectify.connectify.models.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ChatRoom> chatRooms, filteredList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userEmail, userRole;
    private EditText searchBar;
    private ImageView btnLogout;
    private ListenerRegistration chatListener;

    public ChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatListRecyclerView = view.findViewById(R.id.chatListRecyclerView);
        searchBar = view.findViewById(R.id.searchInput);
        btnLogout = view.findViewById(R.id.btnLogout);

        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRooms = new ArrayList<>();
        filteredList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(getContext(), filteredList);
        chatListRecyclerView.setAdapter(chatListAdapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            db.collection("users").document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userRole = documentSnapshot.getString("role");
                            loadChatRooms(userEmail);
                        }
                    });
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnLogout.setOnClickListener(v -> {
            if (chatListener != null) {
                chatListener.remove();
                chatListener = null;
            }
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }

    private void filterChats(String keyword) {
        filteredList.clear();
        for (ChatRoom room : chatRooms) {
            if (room.getChatPartnerName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(room);
            }
        }
        chatListAdapter.notifyDataSetChanged();
    }

    private void loadChatRooms(String userEmail) {
        if (getContext() == null || userEmail == null || auth.getCurrentUser() == null) return;

        chatListener = db.collection("chatList").document(userEmail).collection("chats")
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || getContext() == null || auth.getCurrentUser() == null) return;
                    if (e != null || snapshots == null) return;

                    chatRooms.clear();
                    for (QueryDocumentSnapshot document : snapshots) {
                        ChatRoom chatRoom = document.toObject(ChatRoom.class);
                        chatRoom.setChatPartnerId(document.getString("chatPartnerEmail"));
                        chatRooms.add(chatRoom);
                    }

                    filterChats(searchBar.getText().toString());
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
        }
    }
}
