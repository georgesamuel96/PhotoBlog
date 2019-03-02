package com.example.georgesamuel.photoblog;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class SaveHomeInstance {

    private static Boolean isFirstLoad = true;
    private static DocumentSnapshot documentSnapshot;
    private static ArrayList<BlogPost> list = new ArrayList<>();

    public SaveHomeInstance() {
    }

    public ArrayList<BlogPost> getList() {
        return list;
    }

    public void setList(ArrayList<BlogPost> list) {
        SaveHomeInstance.list = list;
    }

    public Boolean getIsFirstLoad() {
        return isFirstLoad;
    }

    public void setIsFirstLoad(Boolean isFirstLoad) {
        SaveHomeInstance.isFirstLoad = isFirstLoad;
    }

    public DocumentSnapshot getDocumentSnapshot() {
        return documentSnapshot;
    }

    public void setDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        SaveHomeInstance.documentSnapshot = documentSnapshot;
    }
}
