package com.hbcevik.hiddenproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.hbcevik.hiddenproject.databinding.ActivityChatListBinding;
import com.hbcevik.hiddenproject.databinding.ActivityCommentBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatListActivity extends AppCompatActivity {
    private ActivityChatListBinding binding;
    private FirebaseAuth auth;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public String postId;
    BottomNavigationView nav;
    java.util.ArrayList<Message> messageArrayList;
    ChatListAdapter chatListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        messageArrayList = new ArrayList<>();

        binding.ChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListAdapter = new ChatListAdapter(messageArrayList);
        binding.ChatRecyclerView.setAdapter(chatListAdapter);

        nav = findViewById(R.id.nav);

        postId = getIntent().getStringExtra("postId");


        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(ChatListActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(ChatListActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Toast.makeText(ChatListActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(ChatListActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(ChatListActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(ChatListActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(ChatListActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatListActivity.this);
                        builder.setTitle("Log out");
                        builder.setMessage("Are you sure you want to log out?");


                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("remember", "false");
                                editor.apply();

                                auth.signOut();

                                Intent e = new Intent(ChatListActivity.this, LoginActivity.class);
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
    private void getData() {
        db.collection("messages")
                .whereEqualTo("senderId", auth.getCurrentUser().getUid())
                .whereEqualTo("senderId", auth.getCurrentUser().getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatListActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value != null) {
                        messageArrayList.clear();

                        Map<String, Message> lastMessagesMap = new HashMap<>();

                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            String receiverId = snapshot.getString("receiverId");
                            String messageId = snapshot.getId();
                            String senderId = snapshot.getString("senderId");
                            String content = snapshot.getString("content");
                            Date timestamp = snapshot.getDate("timestamp");

                            Message message = new Message();
                            message.setMessageId(messageId);
                            message.setSenderId(senderId);
                            message.setReceiverId(receiverId);
                            message.setContent(content);
                            message.setTimestamp(timestamp);


                            if (!lastMessagesMap.containsKey(receiverId) || timestamp.after(lastMessagesMap.get(receiverId).getTimestamp())) {
                                lastMessagesMap.put(receiverId, message);
                            }
                        }

                        messageArrayList.addAll(lastMessagesMap.values());

                        chatListAdapter.notifyDataSetChanged();
                    }
                });

    }

}