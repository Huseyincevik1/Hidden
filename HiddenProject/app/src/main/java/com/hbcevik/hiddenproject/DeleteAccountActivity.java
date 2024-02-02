package com.hbcevik.hiddenproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbcevik.hiddenproject.databinding.ActivityDeleteAccountBinding;
import com.hbcevik.hiddenproject.databinding.ActivityEditPasswordBinding;

public class DeleteAccountActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    private FirebaseStorage storage;

    private StorageReference storageReference;

    private FirebaseUser user;
    BottomNavigationView nav;
    private ActivityDeleteAccountBinding binding;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        nav = findViewById(R.id.nav);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(DeleteAccountActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(DeleteAccountActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(DeleteAccountActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(DeleteAccountActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(DeleteAccountActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(DeleteAccountActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(DeleteAccountActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(DeleteAccountActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteAccountActivity.this);
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

                                Intent e = new Intent(DeleteAccountActivity.this, LoginActivity.class);
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

    public void deleteAccountClicked(View view){
        String password =binding.txtPassword.getText().toString();

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        user = auth.getCurrentUser();
        if (user != null) {
            user.reauthenticate(com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), password))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                deleteUser(user.getUid());
                            } else {
                                Toast.makeText(DeleteAccountActivity.this, "Authentication failed. Please check your password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void deleteUser(String userId) {
        // Firestore'dan kullanıcı belgesini al
        DocumentReference userRef = firebaseFirestore.collection("Users").document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        deletePostsAndComments(userId);
                        deleteProfileImage();
                        deletePostsAndImages(userId);


                    } else {

                        Toast.makeText(DeleteAccountActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(DeleteAccountActivity.this, "Failed to get user document", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deletePostsAndComments(String userId) {

        firebaseFirestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot post : task.getResult()) {

                                deletePostAndComments(post.getId());
                            }
                        } else {

                            Toast.makeText(DeleteAccountActivity.this, "Failed to get user posts", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deletePostsAndImages(String userId) {

        firebaseFirestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot post : task.getResult()) {

                                deletePostAndImages(post.getId());
                            }
                        } else {

                            Toast.makeText(DeleteAccountActivity.this, "Failed to get user posts", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void deletePostAndComments(String postId) {
        // Postu sil
        firebaseFirestore.collection("posts").document(postId).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            deleteCommentsForPost(postId);
                        } else {

                            Toast.makeText(DeleteAccountActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void deletePostAndImages(String postId) {
        // Postu sil
        firebaseFirestore.collection("posts").document(postId).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            deleteCommentsForImages(postId);
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void deleteCommentsForPost(String postId) {
        firebaseFirestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot comment : task.getResult()) {
                                deleteComment(comment.getId());
                            }
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to get comments for post", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteCommentsForImages(String postId) {
        StorageReference postImageRef = storageReference.child("post_images/" + postId + ".jpg");
        postImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DeleteAccountActivity.this, "Post image deleted successfully", Toast.LENGTH_SHORT).show();
                deleteFirestoreUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DeleteAccountActivity.this, "Failed to delete post image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteComment(String commentId) {

        firebaseFirestore.collection("comments").document(commentId).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            deleteFirestoreUser();
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deletePostImage(String imageUrl) {
        StorageReference postImageRef = storageReference.child("post_images/" + imageUrl);
        postImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DeleteAccountActivity.this, "Post image deleted successfully", Toast.LENGTH_SHORT).show();
                deleteFirestoreUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DeleteAccountActivity.this, "Failed to delete post image", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteProfileImage() {
        StorageReference profileImageRef = storageReference.child("profile_images/" + userId + ".jpg");
        profileImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DeleteAccountActivity.this, "Profile image deleted successfully", Toast.LENGTH_SHORT).show();
                deletePostsAndImages();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DeleteAccountActivity.this, "Failed to delete profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePostsAndImages() {
        firebaseFirestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot post : task.getResult()) {
                                deletePostAndImages(post);
                            }
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to get user posts", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void deletePostAndImages(QueryDocumentSnapshot post) {
        String postId = post.getId();
        String imageUrl = post.getString("imageUrl");

        deletePostImage(imageUrl);


    }

    private void deleteFirestoreUser() {
        firebaseFirestore.collection("Users").document(auth.getCurrentUser().getUid()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            deleteAuthenticationUser();
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to delete user from Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteAuthenticationUser() {
        auth.getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(DeleteAccountActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("remember","false");
                            editor.apply();
                            auth.signOut();
                            Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to delete user from Authentication", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
