package com.connectify.connectify;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.connectify.connectify.adapters.ChatAdapter;
import com.connectify.connectify.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageView chatProfileImage;
    private TextView chatUserName;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String senderEmail, receiverEmail, chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ✅ Slide-in animation when this activity opens
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        chatRecyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendMessageButton);
        chatProfileImage = findViewById(R.id.chatProfileImage);
        chatUserName = findViewById(R.id.chatUserName);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        senderEmail = auth.getCurrentUser().getEmail();
        receiverEmail = getIntent().getStringExtra("receiverEmail");

        if (senderEmail == null || receiverEmail == null) {
            Toast.makeText(this, "Missing chat details!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatRoomId = getChatId(senderEmail, receiverEmail);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        loadReceiverInfo();
        ensureChatRoomExists();
        loadMessages();

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // ✅ Slide-back animation when user presses back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void loadReceiverInfo() {
        firestore.collection("users").document(receiverEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");

                    if ("Employer".equalsIgnoreCase(role)) {
                        firestore.collection("employers").document(receiverEmail)
                                .get()
                                .addOnSuccessListener(employerDoc -> {
                                    if (employerDoc.exists()) {
                                        String name = employerDoc.getString("companyName");
                                        String logoUrl = employerDoc.getString("logoUrl");

                                        chatUserName.setText(name != null ? name : "Unknown");

                                        if (logoUrl != null && !logoUrl.isEmpty()) {
                                            logoUrl = logoUrl.replace("'", "").trim();
                                            Glide.with(this)
                                                    .load(logoUrl)
                                                    .placeholder(R.drawable.company_avatar)
                                                    .error(R.drawable.company_avatar)
                                                    .circleCrop()
                                                    .into(chatProfileImage);
                                        } else {
                                            chatProfileImage.setImageResource(R.drawable.company_avatar);
                                        }
                                    }
                                });
                    } else {
                        chatUserName.setText(documentSnapshot.getString("name"));
                        String profileImage = documentSnapshot.getString("profileImage");
                        if (profileImage != null && !profileImage.isEmpty()) {
                            Glide.with(this).load(profileImage).circleCrop().into(chatProfileImage);
                        } else {
                            chatProfileImage.setImageResource(R.drawable.profile_placeholder);
                        }
                    }
                });
    }

    private void ensureChatRoomExists() {
        DocumentReference chatRef = firestore.collection("chats").document(chatRoomId);
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("participants", Arrays.asList(senderEmail, receiverEmail));
                chatData.put("lastMessage", "Chat started...");
                chatData.put("timestamp", System.currentTimeMillis());

                chatRef.set(chatData).addOnSuccessListener(unused -> {
                    long now = System.currentTimeMillis();
                    updateChatListPreview(senderEmail, receiverEmail, "Chat started...", now, false);
                    updateChatListPreview(receiverEmail, senderEmail, "Chat started...", now, true);
                });
            }
        });
    }

    private void loadMessages() {
        CollectionReference chatRef = firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages");

        chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message message = doc.toObject(Message.class);
                        if (message != null) {
                            messageList.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();

                    if (!messageList.isEmpty()) {
                        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    }

                    firestore.collection("chatList")
                            .document(senderEmail)
                            .collection("chats")
                            .document(receiverEmail)
                            .update("unreadCount", 0);
                });
    }

    private void sendMessage(String messageText) {
        if (TextUtils.isEmpty(messageText)) return;

        long timestamp = System.currentTimeMillis();

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderEmail);
        message.put("receiverId", receiverEmail);
        message.put("message", messageText);
        message.put("timestamp", timestamp);

        DocumentReference chatRef = firestore.collection("chats").document(chatRoomId);

        chatRef.update("lastMessage", messageText, "timestamp", timestamp)
                .addOnFailureListener(e -> {
                    Map<String, Object> chatRoomData = new HashMap<>();
                    chatRoomData.put("participants", Arrays.asList(senderEmail, receiverEmail));
                    chatRoomData.put("lastMessage", messageText);
                    chatRoomData.put("timestamp", timestamp);
                    chatRef.set(chatRoomData);
                });

        firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                    updateChatListPreview(senderEmail, receiverEmail, messageText, timestamp, false);
                    updateChatListPreview(receiverEmail, senderEmail, messageText, timestamp, true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChatListPreview(String userEmail, String partnerEmail, String lastMessage, long timestamp, boolean shouldIncreaseUnread) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(partnerEmail).get().addOnSuccessListener(userDoc -> {
            final String[] finalName = {userDoc.getString("name")};
            final String[] finalImage = {userDoc.getString("profileImage")};

            db.collection("users").document(userEmail).get().addOnSuccessListener(currentUserDoc -> {
                String role = currentUserDoc.getString("role");

                if ("Seeker".equalsIgnoreCase(role)) {
                    db.collection("employers").document(partnerEmail).get().addOnSuccessListener(employerDoc -> {
                        if (employerDoc.exists()) {
                            if (employerDoc.contains("companyName")) {
                                finalName[0] = employerDoc.getString("companyName");
                            }
                            if (employerDoc.contains("logoUrl")) {
                                String logoUrl = employerDoc.getString("logoUrl");
                                if (logoUrl != null && !logoUrl.trim().isEmpty()) {
                                    logoUrl = logoUrl.replace("'", "").trim();
                                    finalImage[0] = logoUrl;
                                }
                            }
                        }

                        saveChatPreview(userEmail, partnerEmail, finalName[0], finalImage[0], lastMessage, timestamp, shouldIncreaseUnread);
                    });
                } else {
                    saveChatPreview(userEmail, partnerEmail, finalName[0], finalImage[0], lastMessage, timestamp, shouldIncreaseUnread);
                }
            });
        });
    }

    private void saveChatPreview(String userEmail, String partnerEmail, String partnerName, String partnerImage,
                                 String lastMessage, long timestamp, boolean shouldIncreaseUnread) {

        DocumentReference chatPreviewRef = FirebaseFirestore.getInstance()
                .collection("chatList")
                .document(userEmail)
                .collection("chats")
                .document(partnerEmail);

        chatPreviewRef.get().addOnSuccessListener(snapshot -> {
            int unreadCount = 0;
            if (snapshot.exists() && snapshot.contains("unreadCount")) {
                unreadCount = snapshot.getLong("unreadCount").intValue();
            }

            Map<String, Object> preview = new HashMap<>();
            preview.put("chatPartnerEmail", partnerEmail);
            preview.put("chatPartnerName", partnerName != null ? partnerName : "Unknown");
            preview.put("chatPartnerProfile", partnerImage);
            preview.put("lastMessage", lastMessage);
            preview.put("timestamp", timestamp);

            preview.put("unreadCount", shouldIncreaseUnread ? unreadCount + 1 : 0);
            chatPreviewRef.set(preview);
        });
    }

    private String getChatId(String email1, String email2) {
        return email1.compareTo(email2) < 0 ? email1 + "_" + email2 : email2 + "_" + email1;
    }
}
