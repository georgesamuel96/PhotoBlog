package com.example.georgesamuel.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class NewItemActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private ImageView image;
    private EditText description;
    private Button submitBtn;
    private Uri itemImageURI = null, downloadUri;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Bitmap compressedImageFile;
    private SaveHomeInstance homeInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        homeInstance = new SaveHomeInstance();

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Item");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = (ImageView) findViewById(R.id.item_image);
        description = (EditText) findViewById(R.id.desc);
        submitBtn = (Button) findViewById(R.id.item_btn);
        progressBar = (ProgressBar) findViewById(R.id.new_item_progress);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(NewItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(NewItemActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = description.getText().toString().trim();
                if(!desc.equals("") && itemImageURI != null){

                    progressBar.setVisibility(View.VISIBLE);

                    final Long timeStamp = System.currentTimeMillis();
                    final String randomName = Long.toString(timeStamp);

                    final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    UploadTask uploadTask = filePath.putFile(itemImageURI);
                    Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()) {
                                String error = task.getException().getMessage();
                                Toast.makeText(NewItemActivity.this, error, Toast.LENGTH_LONG).show();
                                return null;
                            }
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull final Task<Uri> task) {
                            if(task.isSuccessful()){

                                /*File newImageFile = new File(itemImageURI.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewItemActivity.this)
                                            .compressToBitmap(newImageFile);
                                }
                                catch (IOException e){
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg")
                                        .putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });*/

                                downloadUri = task.getResult();
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("image_url", downloadUri.toString());
                                itemMap.put("desc", desc);
                                itemMap.put("user_id", currentUserID);
                                itemMap.put("timestamp", timeStamp);
                                firebaseFirestore.collection("posts").add(itemMap)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){
                                                    Toast.makeText(NewItemActivity.this, "Post was added",
                                                            Toast.LENGTH_LONG).show();

                                                    BlogPost post = new BlogPost();
                                                    post.setImage_url(downloadUri.toString());
                                                    post.setDesc(desc);
                                                    post.setTimestamp(timeStamp);
                                                    post.setUser_id(currentUserID);
                                                    post.withId(task.getResult().getId());
                                                    homeInstance.getList().add(0, post);
                                                    System.out.println(task.getResult().getId());

                                                    startActivity(new Intent(NewItemActivity.this, MainActivity.class));
                                                    finish();
                                                }
                                                else{
                                                    Toast.makeText(NewItemActivity.this, "Post in Error",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });
                            }
                            else {
                                String error = task.getException().getMessage();
                                Toast.makeText(NewItemActivity.this, error, Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
                else{

                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                itemImageURI = result.getUri();
                image.setImageURI(itemImageURI);
                //isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void imagePiker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(NewItemActivity.this);
    }

}
