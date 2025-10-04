package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectify.connectify.adapters.ChatListAdapter;
import com.connectify.connectify.models.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ChatRoom> chatRooms;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String currentUserEmail;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRooms = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatRooms);
        chatListRecyclerView.setAdapter(chatListAdapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserEmail = auth.getCurrentUser().getEmail();
            listenForChatListUpdates();
        } else {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void listenForChatListUpdates() {
        progressBar.setVisibility(View.VISIBLE);
        listenerRegistration = db.collection("chatList")
                .document(currentUserEmail)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    chatRooms.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatRoom room = doc.toObject(ChatRoom.class);
                        if (room != null) {
                            room.setChatPartnerId(doc.getId());
                            chatRooms.add(room);
                        }
                    }
                    chatListAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
