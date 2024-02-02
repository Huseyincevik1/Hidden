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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbcevik.hiddenproject.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    private ActivityMainBinding binding;
    BottomNavigationView nav;
    ArrayList<Post> ArrayList;
    PostAdapter postAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        nav = findViewById(R.id.nav);

        ArrayList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        getData();
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(ArrayList);
        postAdapter.showOptionsIcon(false);
        binding.RecyclerView.setAdapter(postAdapter);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Toast.makeText(MainActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(MainActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(MainActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(MainActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(MainActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(MainActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Log out");
                        builder.setMessage("Are you sure you want to log out?");

                        // Evet butonu
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("remember", "false");
                                editor.apply();

                                auth.signOut();

                                Intent e = new Intent(MainActivity.this, LoginActivity.class);
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

    private void getData(){
        firebaseFirestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(MainActivity.this,error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(value != null){
                     for (DocumentSnapshot snapshot : value.getDocuments()){
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
                         ArrayList.add(post);
                     }
                     postAdapter.notifyDataSetChanged();
                }
            }
        });
    }

}