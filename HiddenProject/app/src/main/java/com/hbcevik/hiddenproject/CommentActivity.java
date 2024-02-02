package com.hbcevik.hiddenproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbcevik.hiddenproject.databinding.ActivityCommentBinding;
import com.hbcevik.hiddenproject.databinding.ActivityUploadBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class CommentActivity extends AppCompatActivity {
    private ActivityCommentBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public String postId;
    BottomNavigationView nav;
    java.util.ArrayList<Comment> commentArrayList;
    CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        commentArrayList = new ArrayList<>();

        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentArrayList);
        binding.RecyclerView.setAdapter(commentAdapter);

        nav = findViewById(R.id.nav);

        postId = getIntent().getStringExtra("postId");


        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(CommentActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(CommentActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(CommentActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(CommentActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(CommentActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(CommentActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(CommentActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(CommentActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
                        builder.setTitle("Log out");
                        builder.setMessage("Are you sure you want to log out?");


                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Kullanıcının çıkış yapmak istediği durum
                                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("remember", "false");
                                editor.apply();

                                auth.signOut();

                                Intent e = new Intent(CommentActivity.this, LoginActivity.class);
                                startActivity(e);
                                finish();
                            }
                        });


                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    default:

                        break;
                }

                return true;
            }
        });
        getData();
    }

    public void addComment(View view){
        String content = binding.editTextComment.getText().toString();
        if (content.equals("")) {
            Toast.makeText(this, "Do not leave empty space", Toast.LENGTH_LONG).show();
        } else {
            user = auth.getCurrentUser();
            String commentId = UUID.randomUUID().toString();

            Comment comment = new Comment();
            comment.setCommentId(commentId);
            comment.setUserId(user.getUid());
            comment.setContent1(content);
            comment.setPostId(postId);
            comment.setTimestamp(new Date());


            db.collection("comments")
                    .document(commentId)
                    .set(comment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            getData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CommentActivity.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            binding.editTextComment.setText("");
        }
    }

    private void getData(){

        db.collection("comments").whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){

                }
                if(value != null){
                    commentArrayList.clear();
                    for (DocumentSnapshot snapshot : value.getDocuments()){
                        String commnetId = snapshot.getId();
                        String content = snapshot.getString("content1");
                        String postId = snapshot.getString("postId");
                        String userId = snapshot.getString("userId");
                        Date timestamp = snapshot.getDate("timestamp");

                        Comment comment = new Comment();
                        comment.setCommentId(commnetId);
                        comment.setContent1(content);
                        comment.setPostId(postId);
                        comment.setUserId(userId);
                        comment.setTimestamp(timestamp);
                        commentArrayList.add(comment);

                    }
                    commentAdapter.notifyDataSetChanged();


                }

            }

        });

    }

}