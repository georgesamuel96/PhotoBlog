package com.example.georgesamuel.photoblog;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostAdapter extends RecyclerView.Adapter<BlogPostAdapter.MyViewHolder> {

    private ArrayList<BlogPost> list = new ArrayList<>();
    private Context context;
    private Long timestamp;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    public BlogPostAdapter(ArrayList<BlogPost> list){
        this.list = list;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {

        myViewHolder.setIsRecyclable(false);
        final String blogPostId = list.get(i).blogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();
        String image_url = list.get(i).getImage_url();
        timestamp = list.get(i).getTimestamp();
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("MMM dd, yyyy");
        String dateTime = sdfDateTime.format(timestamp);
        String user_id = list.get(i).getUser_id();
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.post_placeholder);

        myViewHolder.desc.setText(list.get(i).getDesc());
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(image_url).into(myViewHolder.postImage);
        myViewHolder.time.setText(dateTime);

        // Get username and profile image
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String profileImage_url, userName;
                    userName = task.getResult().getString("name");
                    profileImage_url = task.getResult().getString("image");
                    myViewHolder.userName.setText(userName);
                    Glide.with(context).applyDefaultRequestOptions(requestOptions).load(profileImage_url).into(myViewHolder.profileImage);
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Check if user like this post or not
        firebaseFirestore.collection("posts").document(blogPostId).collection("Likes")
                .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){
                    myViewHolder.likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.action_like_accent));
                }
                else{
                    myViewHolder.likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.action_like_gray));
                }
            }
        });

        // Get likes count
        firebaseFirestore.collection("posts").document(blogPostId).collection("Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(!queryDocumentSnapshots.isEmpty()){
                            int count = queryDocumentSnapshots.size();
                            myViewHolder.likeCount.setText(count + " Likes");
                        }
                        else {
                            myViewHolder.likeCount.setText("0 Likes");
                        }
                    }
                });

        // User like post
        myViewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("posts").document(blogPostId).collection("Likes")
                        .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", System.currentTimeMillis());

                            firebaseFirestore.collection("posts").document(blogPostId).collection("Likes")
                                    .document(currentUserId).delete();
                        }
                        else{
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", System.currentTimeMillis());

                            firebaseFirestore.collection("posts").document(blogPostId).collection("Likes")
                                    .document(currentUserId).set(likesMap);
                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView desc, userName, time;
        ImageView postImage;
        CircleImageView profileImage;
        ImageView likeBtn;
        TextView likeCount;

        public MyViewHolder(View view){
            super(view);

            desc = (TextView) view.findViewById(R.id.blog_desc);
            userName = (TextView) view.findViewById(R.id.blog_user_name);
            time = (TextView) view.findViewById(R.id.blog_date);
            postImage = (ImageView) view.findViewById(R.id.blog_image);
            profileImage = (CircleImageView) view.findViewById(R.id.blog_user_image);
            likeBtn = (ImageView) view.findViewById(R.id.like_btn);
            likeCount = (TextView) view.findViewById(R.id.like_count);
        }
    }
}
