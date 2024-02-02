package com.hbcevik.hiddenproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.hbcevik.hiddenproject.databinding.RecyclerRow4Binding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListHolder> {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ArrayList<Message> messagesArrayList;
    private RecyclerView.ViewHolder holder;

    public ChatListAdapter(ArrayList<Message>messagesArrayList){
        this.messagesArrayList = messagesArrayList;
    }


    @NonNull
    @Override
    public ChatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRow4Binding recyclerRow4Binding = RecyclerRow4Binding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        auth = FirebaseAuth.getInstance();
        holder = new ChatListHolder(recyclerRow4Binding);
        return new ChatListHolder(recyclerRow4Binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListHolder holder,@SuppressLint("RecyclerView") int position) {
        holder.recyclerRow4Binding.textViewChatContent.setText(messagesArrayList.get(position).getContent());

        user = auth.getCurrentUser();
        String userId = messagesArrayList.get(position).getReceiverId();


        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            String username = documentSnapshot.getString("username");
                            String profileImageUrl = documentSnapshot.getString("profileImage");


                            holder.recyclerRow4Binding.usernameTextView.setText(username);
                            Picasso.get().load(profileImageUrl).into(holder.recyclerRow4Binding.imageView);

                        }
                    }
                });
        holder.itemView.setOnClickListener(v -> {
            openChatWithUser(messagesArrayList.get(position).getReceiverId());
        });


    }
    private void openChatWithUser(String userId) {
        Intent intent = new Intent(holder.itemView.getContext(), MessageActivity.class);
        intent.putExtra("receiverId", userId);
        intent.putExtra("senderId",auth.getCurrentUser().getUid());
        holder.itemView.getContext().startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    class ChatListHolder extends RecyclerView.ViewHolder{

        RecyclerRow4Binding recyclerRow4Binding;

        public ChatListHolder( RecyclerRow4Binding recyclerRow4Binding) {
            super(recyclerRow4Binding.getRoot());
            this.recyclerRow4Binding = recyclerRow4Binding;
        }
    }

}
