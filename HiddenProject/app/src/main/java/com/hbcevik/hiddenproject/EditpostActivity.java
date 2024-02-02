package com.hbcevik.hiddenproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbcevik.hiddenproject.databinding.ActivityEditpostBinding;
import com.hbcevik.hiddenproject.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EditpostActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    private FirebaseStorage storage;

    private StorageReference storageReference;

    BottomNavigationView nav;
    private ActivityEditpostBinding binding;
    String postId;
    double latitude;
    double longitude;
    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditpostBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        nav = findViewById(R.id.nav);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        registerLauncher();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);
        getPost(postId);


        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(EditpostActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(EditpostActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(EditpostActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(EditpostActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(EditpostActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(EditpostActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(EditpostActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(EditpostActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditpostActivity.this);
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

                                Intent e = new Intent(EditpostActivity.this, LoginActivity.class);
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

    public void getPost(String postId) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            String title = documentSnapshot.getString("title");
                            String content = documentSnapshot.getString("content");
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            binding.txtTitle.setText(title);
                            binding.txtContent.setText(content);
                            Picasso.get().load(imageUrl).into(binding.changeimage);
                            double latitude = documentSnapshot.getDouble("latitude");
                            double longitude = documentSnapshot.getDouble("longitude");



                        } else {
                            Toast.makeText(EditpostActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void deleteClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePost(postId);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void deletePost(String postId) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            deleteImageFromStorage(imageUrl);
                            deleteCommentsForPost(postId);

                            FirebaseFirestore.getInstance().collection("posts").document(postId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(EditpostActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(EditpostActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditpostActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {

                            Toast.makeText(EditpostActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteImageFromStorage(String imageUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditpostActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditpostActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCommentsForPost(String postId) {

        FirebaseFirestore.getInstance().collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String commentId = documentSnapshot.getId();
                            deleteComment(commentId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(EditpostActivity.this, "Failed to delete comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteComment(String commentId) {
        FirebaseFirestore.getInstance().collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(EditpostActivity.this, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditpostActivity.this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateClicked(View view) {
        String newTitle = binding.txtTitle.getText().toString();
        String newContent = binding.txtContent.getText().toString();

        if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newContent)) {
            Toast.makeText(EditpostActivity.this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .update("title", newTitle, "content", newContent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditpostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditpostActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show();
                    }
                });

        if (imageData != null) {
            updateImage(postId, imageData);
        }

        Intent intent = new Intent(EditpostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void changeImage(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        imageData = data.getData();
                        binding.changeimage.setImageURI(imageData);
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    openGallery();
                } else {
                    Toast.makeText(EditpostActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void updateImage(String postId, Uri newImageUri) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String oldImageUrl = documentSnapshot.getString("imageUrl");
                            deleteImageFromStorage(oldImageUrl);
                            uploadImage(postId, newImageUri);
                        } else {
                            Toast.makeText(EditpostActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadImage(String postId, Uri newImageUri) {
        StorageReference imageRef = storageReference.child("post_images/" + postId + ".jpg");

        imageRef.putFile(newImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        FirebaseFirestore.getInstance().collection("posts").document(postId)
                                                .update("imageUrl", uri.toString())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(EditpostActivity.this, "Image updated successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(EditpostActivity.this, "Failed to update image URL", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditpostActivity.this, "Failed to upload new image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}