package com.example.georgesamuel.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentsActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private EditText comment_field;
    private ImageView comment_post_btn;
    private String blogPostId;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private RecyclerView recyclerView;
    private ArrayList<Comments> comments_List;
    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        comment_field = (EditText) findViewById(R.id.comment_field);
        comment_post_btn = (ImageView) findViewById(R.id.comment_post_btn);
        recyclerView = (RecyclerView) findViewById(R.id.comment_list);

        comments_List = new ArrayList<>();
        adapter = new CommentAdapter(comments_List);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        blogPostId = getIntent().getStringExtra("blog_post_id");

        // Get Comments
        firebaseFirestore.collection("posts").document(blogPostId).collection("Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {


                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    comments_List.add(comments);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                    }
                });

        // Write Comment
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment = comment_field.getText().toString().trim();

                if(!comment.equals("")){

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment);
                    commentMap.put("user_id", currentUserId);
                    commentMap.put("timestamp", System.currentTimeMillis());

                    firebaseFirestore.collection("posts").document(blogPostId).collection("Comments")
                            .add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if(task.isSuccessful()){
                                comment_field.setText("");
                            }
                            else{
                                String error = task.getException().getMessage();
                                Toast.makeText(CommentsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                else{

                }
            }
        });
    }
}
