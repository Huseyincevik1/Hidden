package com.hbcevik.hiddenproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbcevik.hiddenproject.databinding.ActivityCommentBinding;
import com.hbcevik.hiddenproject.databinding.ActivityMessageBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public String postId;
    public String receiverId;
    public String senderId;
    BottomNavigationView nav;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        nav = findViewById(R.id.nav);

        postId = getIntent().getStringExtra("postId");
        receiverId = getIntent().getStringExtra("receiverId");
        senderId = getIntent().getStringExtra("senderId");
        getProfileData();


        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(MessageActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(MessageActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(MessageActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(MessageActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(MessageActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(MessageActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(MessageActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(MessageActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
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

                                    Intent e = new Intent(MessageActivity.this, LoginActivity.class);
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(messageList, senderId);
        binding.chatRecyclerView.setAdapter(messageAdapter);

        binding.buttonAddMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        getMessages();
    }

    private void getMessages() {
        FirebaseFirestore.getInstance()
                .collection("messages")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        return;
                    }


                    messageList.clear();

                    for (DocumentSnapshot document : value.getDocuments()) {
                        Message message = document.toObject(Message.class);
                        messageList.add(message);
                    }

                    groupAndSortMessages();
                    messageAdapter.notifyDataSetChanged();
                    binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                });

        FirebaseFirestore.getInstance()
                .collection("messages")
                .whereEqualTo("senderId", receiverId)
                .whereEqualTo("receiverId", senderId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }


                    for (DocumentSnapshot document : value.getDocuments()) {
                        Message message = document.toObject(Message.class);

                        if (!messageList.contains(message)) {
                            messageList.add(message);
                        }
                    }
                    groupAndSortMessages();
                    messageAdapter.notifyDataSetChanged();
                    binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                });
    }
    private void groupAndSortMessages() {
        List<Message> senderMessages = new ArrayList<>();
        List<Message> receiverMessages = new ArrayList<>();

        for (Message message : messageList) {
            if (message.getSenderId().equals(senderId)) {
                senderMessages.add(message);
            } else {
                receiverMessages.add(message);
            }
        }

        Collections.sort(senderMessages, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        Collections.sort(receiverMessages, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

        messageList.clear();
        messageList.addAll(senderMessages);
        messageList.addAll(receiverMessages);

        Collections.sort(messageList, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
    }

    private void sendMessage() {
        String content = binding.editTextMessage.getText().toString().trim();
        if (!content.isEmpty()) {
            String messageId = UUID.randomUUID().toString();

            db.collection("Users").document(receiverId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {

                            String profileImageUrl = documentSnapshot.getString("profileImage");
                            String username = documentSnapshot.getString("username");


                            if (profileImageUrl == null || username == null || username.equals("Hidden Kullanıcısı")) {
                                Toast.makeText(MessageActivity.this, "Mesaj gönderilen kullanıcı aktif değil veya gizli.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Message message = new Message(messageId, senderId, receiverId, content, new Date());

                            db.collection("messages").document(messageId).set(message)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MessageActivity.this, "Mesaj gönderildi", Toast.LENGTH_SHORT).show();
                                        messageList.clear();
                                        getMessages();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MessageActivity.this, "Mesaj gönderilemedi: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            binding.editTextMessage.setText("");
                        } else {

                            Toast.makeText(MessageActivity.this, "Kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MessageActivity.this, "Firestore sorgusu hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void getProfileData() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(receiverId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MessageActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String profileImageUrl = documentSnapshot.getString("profileImage");

                    Picasso.get().load(profileImageUrl).into(binding.imageView);
                    binding.otherUsername.setText(username);
                }
            }
        });
    }


    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "";
    }


    private String getUserId() {
        return getIntent().getStringExtra("userId");
    }
}