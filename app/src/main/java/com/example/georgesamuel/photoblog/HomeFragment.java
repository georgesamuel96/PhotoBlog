package com.example.georgesamuel.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<BlogPost> post_list = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private BlogPostAdapter adapter;
    private DocumentSnapshot lastVisible;
    private FirebaseAuth mAuth;
    private boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.blog_list_view);

        adapter = new BlogPostAdapter(post_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){

                        loadMorePost();
                    }
                }
            });

            Query query = firebaseFirestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if(isFirstPageFirstLoad) {

                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            if(isFirstPageFirstLoad) {
                                post_list.add(blogPost);
                            }
                            else{
                                post_list.add(0, blogPost);
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageFirstLoad = false;
                }
            });
        }

        return view;
    }

    private void loadMorePost(){

        Query query = firebaseFirestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            post_list.add(blogPost);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

}
