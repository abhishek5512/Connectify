package com.connectify.connectify.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.connectify.connectify.ChatActivity;
import com.connectify.connectify.R;
import com.connectify.connectify.models.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private Context context;
    private List<ChatRoom> chatRooms;
    private String currentUserEmail;

    public ChatListAdapter(Context context, List<ChatRoom> chatRooms) {
        this.context = context;
        this.chatRooms = chatRooms;
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        sortByTimestampDesc();
    }

    public void sortByTimestampDesc() {
        Collections.sort(chatRooms, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_user_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);

        holder.tvUserName.setText(room.getChatPartnerName());
        holder.tvLastMessage.setText(room.getLastMessage());

        if (room.getChatPartnerProfile() != null && !room.getChatPartnerProfile().isEmpty()) {
            Glide.with(context)
                    .load(room.getChatPartnerProfile())
                    .placeholder(R.drawable.company_avatar)
                    .error(R.drawable.company_avatar)
                    .circleCrop()
                    .into(holder.ivProfile);
        } else {
            Glide.with(context)
                    .load(R.drawable.company_avatar)
                    .circleCrop()
                    .into(holder.ivProfile);
        }

        if (room.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(room.getUnreadCount()));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverEmail", room.getChatPartnerId());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.chat_item_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    deleteChat(room, holder.getAdapterPosition());
                    return true;
                }
                return false;
            });
            popup.show();
            return true;
        });
    }

    private void deleteChat(ChatRoom chatRoom, int position) {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String partnerId = chatRoom.getChatPartnerId();

        // Assuming chatRoomId is built as user1_user2 or a unique key:
        String chatRoomId = generateChatRoomId(currentUser, partnerId);

        FirebaseFirestore.getInstance()
                .collection("chatRooms")
                .document(chatRoomId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    chatRooms.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Chat deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete chat", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteChat", "Error: ", e);
                });
    }

    private String generateChatRoomId(String user1, String user2) {
        // Ensure consistent ID generation
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage, unreadBadge;
        ImageView ivProfile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
        }
    }
}
