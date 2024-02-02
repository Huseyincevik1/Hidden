package com.hbcevik.hiddenproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.hbcevik.hiddenproject.databinding.RecyclerRowBinding;
import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private ArrayList<Post> arrayList;

    private boolean showOptionsIcon = false;

    public void showOptionsIcon(boolean show) {
        this.showOptionsIcon = show;
        notifyDataSetChanged();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button buttonViewLocation;
        public Button buttonComment;

        public ViewHolder(View itemView) {
            super(itemView);
            buttonViewLocation = itemView.findViewById(R.id.button1);
            buttonComment = itemView.findViewById(R.id.button2);
        }
    }

    public PostAdapter(ArrayList<Post> arrayList) {

        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        auth = FirebaseAuth.getInstance();
       return new PostHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.recyclerRowBinding.titleTextView.setText(arrayList.get(position).title);
        holder.recyclerRowBinding.contentTextView.setText(arrayList.get(position).content);
        Picasso.get().load(arrayList.get(position).imageUrl).into(holder.recyclerRowBinding.postImageView);

        user = auth.getCurrentUser();
        String userId = arrayList.get(position).getUserId();

        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String profileImageUrl = documentSnapshot.getString("profileImage");

                            holder.recyclerRowBinding.usernameTextView.setText(username);
                            Picasso.get().load(profileImageUrl).into(holder.recyclerRowBinding.imageView);

                        }
                    }
                });

        holder.recyclerRowBinding.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post post = arrayList.get(position);
                double latitude = post.getLatitude();
                double longitude = post.getLongitude();

                Intent intent = new Intent(v.getContext(), MapsActivity2.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                v.getContext().startActivity(intent);
            }
        });

        holder.recyclerRowBinding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post post = arrayList.get(position);
                String postId = post.getPostId();
                Intent intent = new Intent(v.getContext(), CommentActivity.class);
                intent.putExtra("postId",postId);
                v.getContext().startActivity(intent);
            }
        });

        holder.recyclerRowBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post post = arrayList.get(position);
                String postId = post.getPostId();
                String senderId = auth.getCurrentUser().getUid();
                String receiverId = post.getUserId();

                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                intent.putExtra("postId",postId);
                intent.putExtra("senderId",senderId);
                intent.putExtra("receiverId",receiverId);
                v.getContext().startActivity(intent);
            }
        });



        if (showOptionsIcon) {
            holder.recyclerRowBinding.optionsIcon.setVisibility(View.VISIBLE);
        } else {
            holder.recyclerRowBinding.optionsIcon.setVisibility(View.GONE);
        }

        holder.recyclerRowBinding.optionsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Post clickedPost = arrayList.get(position);

                Intent intent = new Intent(view.getContext(), EditpostActivity.class);
                intent.putExtra("postId", clickedPost.getPostId());
                intent.putExtra("latitude", clickedPost.getLatitude());
                intent.putExtra("longitude", clickedPost.getLongitude());

                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {

        return arrayList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder{
        RecyclerRowBinding recyclerRowBinding;

        public PostHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding =recyclerRowBinding;
        }
    }
}
