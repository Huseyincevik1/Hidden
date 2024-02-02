package com.hbcevik.hiddenproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.UUID;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbcevik.hiddenproject.databinding.ActivityMainBinding;
import com.hbcevik.hiddenproject.databinding.ActivityUploadBinding;

public class UploadActivity extends AppCompatActivity {
    private ActivityUploadBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    BottomNavigationView nav;

    private double latitude;
    private double longitude;

    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        nav = findViewById(R.id.nav);


        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(UploadActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(UploadActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(UploadActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(UploadActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Toast.makeText(UploadActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(UploadActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(UploadActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
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

                                Intent e = new Intent(UploadActivity.this, LoginActivity.class);
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

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                latitude = intent.getDoubleExtra("latitude", 0.0);
                longitude = intent.getDoubleExtra("longitude", 0.0);

            }
        }
    }
    public void shareClicked(View view){
        String title = binding.txtTitle.getText().toString();
        String content = binding.txtContent.getText().toString();
        if (title.equals("") || content.equals("") || latitude == 0.0 || longitude == 0.0 || imageData == null) {
            Toast.makeText(this, "Do not leave empty space", Toast.LENGTH_LONG).show();
        } else {
        user = auth.getCurrentUser();
        String postId = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child("post_images").child(postId + ".jpg");
        imageRef.putFile(imageData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Post post = new Post();
                                post.setPostId(postId);
                                post.setUserId(user.getUid());
                                post.setTitle(title);
                                post.setContent(content);
                                post.setImageUrl(uri.toString());
                                post.setLatitude(latitude);
                                post.setLongitude(longitude);
                                post.setTimestamp(new Date());

                                db.collection("posts")
                                        .document(postId)
                                        .set(post)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(UploadActivity.this, "Post shared successfully", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(UploadActivity.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    public void locationClicked(View view){
        Intent intent = new Intent(UploadActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    public void uploadImage(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
                    }
                }).show();
            }else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        }
    }

    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent a = result.getData();
                    if (a != null){
                        imageData = a.getData();
                        binding.uploadimage.setImageURI(imageData);
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intent);
                } else {
                    Toast.makeText(UploadActivity.this,"Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}