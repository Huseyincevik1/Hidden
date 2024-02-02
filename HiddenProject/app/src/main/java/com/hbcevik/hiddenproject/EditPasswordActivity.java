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
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbcevik.hiddenproject.databinding.ActivityEditPasswordBinding;
import com.hbcevik.hiddenproject.databinding.ActivityEditUserBinding;

public class EditPasswordActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    private FirebaseStorage storage;

    private StorageReference storageReference;

    BottomNavigationView nav;
    private ActivityEditPasswordBinding binding;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPasswordBinding.inflate(getLayoutInflater());
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
                        Intent a = new Intent(EditPasswordActivity.this, MainActivity.class);
                        startActivity(a);
                        Toast.makeText(EditPasswordActivity.this,"Home",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.message:
                        Intent b = new Intent(EditPasswordActivity.this, ChatListActivity.class);
                        startActivity(b);
                        Toast.makeText(EditPasswordActivity.this,"Message",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.add:
                        Intent c = new Intent(EditPasswordActivity.this, UploadActivity.class);
                        startActivity(c);
                        Toast.makeText(EditPasswordActivity.this,"Add",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.profile:
                        Intent d = new Intent(EditPasswordActivity.this, ProfileActivity.class);
                        startActivity(d);
                        Toast.makeText(EditPasswordActivity.this,"Profile",Toast.LENGTH_LONG).show();
                        break;

                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditPasswordActivity.this);
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

                                Intent e = new Intent(EditPasswordActivity.this, LoginActivity.class);
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

    public void passwordClicked(View view){
        String oldPassword = binding.txtOldPassword.getText().toString();
        String newPassword = binding.txtNewPassword.getText().toString();
        String confirmPassword = binding.txtNewPassword2.getText().toString();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reauthenticate(com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), oldPassword))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(EditPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                    updatePasswordInFirestore(userId,newPassword);
                                                    Intent intent = new Intent(EditPasswordActivity.this, ProfileActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(EditPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(EditPasswordActivity.this, "Authentication failed. Please check your old password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updatePasswordInFirestore(String userId, String newPassword) {
        DocumentReference userRef = firebaseFirestore.collection("Users").document(userId);

        userRef.update("password", newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditPasswordActivity.this, "Firestore password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditPasswordActivity.this, "Failed to update password in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}