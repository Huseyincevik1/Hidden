package com.hbcevik.hiddenproject;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.hbcevik.hiddenproject.databinding.RecyclerRow2Binding;
import com.hbcevik.hiddenproject.databinding.RecyclerRowBinding;
import com.hbcevik.hiddenproject.databinding.RecyclerRow3Binding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    private FirebaseAuth auth;

    private List<Message> messageList;
    private String currentUserId;


    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRow3Binding recyclerRow3Binding = RecyclerRow3Binding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        auth = FirebaseAuth.getInstance();
        return new MessageHolder(recyclerRow3Binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder,@SuppressLint("RecyclerView") int position) {
        Message message = messageList.get(position);
        holder.recyclerRow3Binding.rightChatTextview.setText(message.getContent());

        if (message.getSenderId().equals(currentUserId)) {
            holder.recyclerRow3Binding.leftChatLayout.setVisibility(View.GONE);
            holder.recyclerRow3Binding.rightChatLayout.setVisibility(View.VISIBLE);
        } else {
            holder.recyclerRow3Binding.rightChatLayout.setVisibility(View.GONE);

            if (message.getReceiverId().equals(currentUserId)) {
                holder.recyclerRow3Binding.leftChatLayout.setVisibility(View.VISIBLE);
                holder.recyclerRow3Binding.leftChatTextview.setText(message.getContent());
            } else {
                holder.recyclerRow3Binding.leftChatLayout.setVisibility(View.GONE);
            }
        }


        FirebaseFirestore.getInstance().collection("Users").document(message.getSenderId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
    class MessageHolder extends RecyclerView.ViewHolder{
        RecyclerRow3Binding recyclerRow3Binding;
        public MessageHolder( RecyclerRow3Binding recyclerRow3Binding) {
            super(recyclerRow3Binding.getRoot());
            this.recyclerRow3Binding = recyclerRow3Binding;
        }
    }
}
