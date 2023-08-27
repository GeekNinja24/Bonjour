package com.GeekNinja24.Bonjour_v2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.GeekNinja24.Bonjour_v2.Activities.MainActivity;
import com.GeekNinja24.Bonjour_v2.Activities.SetupProfileActivity;
import com.GeekNinja24.Bonjour_v2.Models.User;
import com.GeekNinja24.Bonjour_v2.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SettingsActivity extends AppCompatActivity {
    private Uri selectedImageUri = null;

    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);


        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,26);


            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if an image has been selected
                if (selectedImageUri != null) {
                    dialog.show();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());

                    reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String img = uri.toString();
                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.txtUsername.getText().toString();

                                        User user = new User(uid, name, phone, img);

                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user);

                                        dialog.dismiss();
                                        // Optionally, show a success message here
                                    }
                                });
                            } else {
                                dialog.dismiss();
                                String uid = auth.getUid();
                                String phone = auth.getCurrentUser().getPhoneNumber();
                                String name = auth.getCurrentUser().getDisplayName();
                                User user = new User(uid, name, phone, "No Image");

                                database.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user);

                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null) {
                // Set the selected image URI to the profile_image ImageView
                selectedImageUri = data.getData();
                binding.profileImage.setImageURI(selectedImageUri);
            }
        }


    }
}


