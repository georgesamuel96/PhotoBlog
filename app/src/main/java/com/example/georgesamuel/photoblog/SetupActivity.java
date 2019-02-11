package com.example.georgesamuel.photoblog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yalantis.ucrop.UCrop;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private FirebaseAuth mAut;
    private CircleImageView profileImage;
    private Uri mainImageURI = null, downloadUri;
    private EditText setupName;
    private Button submitBtn;
    private StorageReference storageReference;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private String userName, userID;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Setting");
        profileImage = (CircleImageView) findViewById(R.id.profileImage);
        setupName = (EditText) findViewById(R.id.userName);
        submitBtn = (Button) findViewById(R.id.submit);
        progressBar = (ProgressBar) findViewById(R.id.setup_progress);


        mAut = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userID = mAut.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        submitBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        setupName.setText(name);
                        mainImageURI = Uri.parse(image);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);
                    }
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
                submitBtn.setEnabled(true);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userName = setupName.getText().toString().trim();

                if (!userName.equals("") && !(profileImage == null)) {

                    progressBar.setVisibility(View.VISIBLE);

                    if(isChanged) {
                        final StorageReference image_path = storageReference.child("profile_images").child(userID + ".jpg");
                        UploadTask uploadTask = image_path.putFile(mainImageURI);
                        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
                                    return null;
                                }
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    storeFirebase(task);
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        storeFirebase(null);
                    }
                }
                else{
                    Toast.makeText(SetupActivity.this, "Fields(Image, Your name) mustn't be emty", Toast.LENGTH_LONG).show();
                    mainImageURI = null;
                    isChanged = false;
                    profileImage.setImageResource(R.drawable.default_image);
                    setupName.setText("");
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else{
                        imagePiker();
                    }
                }
                else{
                    imagePiker();
                }
            }
        });
    }

    private void storeFirebase(Task<Uri> task) {

        if(task != null) {
            downloadUri = task.getResult();
        }
        else{
            downloadUri = mainImageURI;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", downloadUri.toString());


        firebaseFirestore.collection("Users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "The user setting is uploaded", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SetupActivity.this, MainActivity.class));
                    finish();
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void imagePiker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                profileImage.setImageURI(mainImageURI);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
