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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbcevik.hiddenproject.databinding.ActivityEditUserBinding;
import com.hbcevik.hiddenproject.databinding.ActivityEditpostBinding;
import com.squareup.picasso.Picasso;

public class EditUserActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    private FirebaseStorage storage;

    private StorageReference storageReference;

    private FirebaseUser user;
    BottomNavigationView nav;
    private ActivityEditUserBinding binding;
    String userId;

    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        nav = findViewById(R.id.nav);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        registerLauncher();

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        getUser(userId);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.home:
                        Intent a = new Intent(EditUserActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(EditUserActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(EditUserActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(EditUserActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(EditUserActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(EditUserActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(EditUserActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(EditUserActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserActivity.this);
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

                                Intent e = new Intent(EditUserActivity.this, LoginActivity.class);
                                startActivity(e);
                                finish();
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Kullanıcının çıkış yapmak istemediği durum
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
    public void getUser(String userId) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String surname = documentSnapshot.getString("surname");
                            String username = documentSnapshot.getString("username");
                            String imageUrl = documentSnapshot.getString("profileImage");

                            binding.txtName.setText(name);
                            binding.txtSurname.setText(surname);
                            binding.txtUsername.setText(username);
                            Picasso.get().load(imageUrl).into(binding.imageView);


                        } else {

                            Toast.makeText(EditUserActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void userUpdateClicked(View view){
        String newName = binding.txtName.getText().toString();
        String newSurname = binding.txtSurname.getText().toString();
        String newUsername = binding.txtUsername.getText().toString();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newSurname) || TextUtils.isEmpty(newUsername)) {
            Toast.makeText(EditUserActivity.this, "Name, Surname and Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update("name", newName, "surname", newSurname,"username",newUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditUserActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                    }
                });

        if (imageData != null) {
            updateImage(userId, imageData);
        }

        Intent intent = new Intent(EditUserActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void selectImage(View view){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
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
                        binding.imageView.setImageURI(imageData);
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
                    Toast.makeText(EditUserActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateImage(String userId, Uri newImageUri) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String oldImageUrl = documentSnapshot.getString("profileImage");
                            deleteImageFromStorage(oldImageUrl);
                            uploadImage(userId, newImageUri);
                        } else {
                            Toast.makeText(EditUserActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadImage(String userId, Uri newImageUri) {
        StorageReference imageRef = storageReference.child("profile_images/" + userId + ".jpg");

        imageRef.putFile(newImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        FirebaseFirestore.getInstance().collection("Users").document(userId)
                                                .update("profileImage", uri.toString())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(EditUserActivity.this, "Image updated successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(EditUserActivity.this, "Failed to update image URL", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditUserActivity.this, "Failed to upload new image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteImageFromStorage(String imageUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

        storageRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Resim başarıyla silindiğinde yapılacak işlemler
                        Toast.makeText(EditUserActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Resim silme işlemi başarısız olduğunda yapılacak işlemler
                        Toast.makeText(EditUserActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}