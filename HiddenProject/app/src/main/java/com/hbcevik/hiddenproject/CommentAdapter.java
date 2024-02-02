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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ArrayList<Comment> commentArrayList;

    public CommentAdapter(ArrayList<Comment>commentArrayList){
        this.commentArrayList = commentArrayList;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRow2Binding recyclerRow2Binding = RecyclerRow2Binding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        auth = FirebaseAuth.getInstance();
        return new CommentHolder(recyclerRow2Binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder,@SuppressLint("RecyclerView") int position) {
        holder.recyclerRow2Binding.textViewCommentContent.setText(commentArrayList.get(position).content1);

        user = auth.getCurrentUser();
        String userId = commentArrayList.get(position).getUserId();


        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            String username = documentSnapshot.getString("username");
                            String profileImageUrl = documentSnapshot.getString("profileImage");


                            holder.recyclerRow2Binding.usernameTextView.setText(username);
                            Picasso.get().load(profileImageUrl).into(holder.recyclerRow2Binding.imageView);

                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    class CommentHolder extends RecyclerView.ViewHolder{

        RecyclerRow2Binding recyclerRow2Binding;

        public CommentHolder( RecyclerRow2Binding recyclerRow2Binding) {
            super(recyclerRow2Binding.getRoot());
            this.recyclerRow2Binding = recyclerRow2Binding;
        }
    }
}
