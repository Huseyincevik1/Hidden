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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.play.core.integrity.v;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbcevik.hiddenproject.databinding.ActivityMainBinding;
import com.hbcevik.hiddenproject.databinding.ActivityProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    BottomNavigationView nav;

    ArrayList<Post> userPosts;
    PostAdapter postAdapter;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        nav = findViewById(R.id.nav);
        userPosts = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        getProfileData();
        getUserPosts();
        binding.RecyclerView1.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(userPosts);
        postAdapter.showOptionsIcon(true);
        binding.RecyclerView1.setAdapter(postAdapter);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(ProfileActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(ProfileActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(ProfileActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(ProfileActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(ProfileActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Toast.makeText(ProfileActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
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

                                Intent e = new Intent(ProfileActivity.this, LoginActivity.class);
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.user_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.updateUser){
            String userId = auth.getCurrentUser().getUid();
            Intent intent = new Intent(ProfileActivity.this, EditUserActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;

        }else if(item.getItemId() == R.id.updatePassword){
            String userId = auth.getCurrentUser().getUid();
            Intent intent = new Intent(ProfileActivity.this, EditPasswordActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;

        } else if (item.getItemId() == R.id.deleteAccount) {
            String userId = auth.getCurrentUser().getUid();
            Intent intent = new Intent(ProfileActivity.this, DeleteAccountActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;

        }else if (item.getItemId() == R.id.aboutApp) {
            Intent intent = new Intent(ProfileActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getProfileData() {
        String uid = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ProfileActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String profileImageUrl = documentSnapshot.getString("profileImage");

                    Picasso.get().load(profileImageUrl).into(binding.imageView);
                    binding.editTextProfile.setText(username);
                }
            }
        });
    }

    private void getUserPosts() {
        String uid = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("posts")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ProfileActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            userPosts.clear();
                            for (DocumentSnapshot snapshot : value.getDocuments()) {
                                String postId = snapshot.getId();
                                String userId = snapshot.getString("userId");
                                String title = snapshot.getString("title");
                                String content = snapshot.getString("content");
                                String imageUrl = snapshot.getString("imageUrl");
                                double latitude = snapshot.getDouble("latitude");
                                double longitude = snapshot.getDouble("longitude");
                                Date timestamp = snapshot.getDate("timestamp");

                                Post post = new Post();
                                post.setPostId(postId);
                                post.setUserId(userId);
                                post.setTitle(title);
                                post.setContent(content);
                                post.setImageUrl(imageUrl);
                                post.setLatitude(latitude);
                                post.setLongitude(longitude);
                                post.setTimestamp(timestamp);
                                userPosts.add(post);
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
