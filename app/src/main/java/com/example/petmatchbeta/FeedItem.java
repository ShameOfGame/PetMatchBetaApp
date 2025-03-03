package com.example.petmatchbeta;

import com.google.firebase.firestore.DocumentReference;

// dient als Datenmodell für Feed-Einträg, auch Referenzen zum Haustier werden hergestellt.

public class FeedItem {

    private String feedId;
    private String title;
    private String inhalt;
    private long createdAt;
    private DocumentReference petRef;
    private DocumentReference ownerRef;
    private String petName;

    // Standard-Konstruktor (WICHTIG Firestore)
    public FeedItem() {}

    // Konstruktor mit allen Attributen für FeedItem-Objekts
    public FeedItem(String feedId, String title, String inhalt, long createdAt, DocumentReference petRef, DocumentReference ownerRef, String petName) {
        this.feedId = feedId;
        this.title = title;
        this.inhalt = inhalt;
        this.createdAt = createdAt;
        this.petRef = petRef;
        this.ownerRef = ownerRef;
        this.petName = petName;
    }

    // Getter-Methoden
    public String getFeedId() {
        return feedId;
    }

    // Setter-Methoden
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInhalt() {
        return inhalt;
    }

    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public DocumentReference getPetRef() {
        return petRef;
    }

    public void setPetRef(DocumentReference petRef) {
        this.petRef = petRef;
    }

    public DocumentReference getOwnerRef() {
        return ownerRef;
    }

    public void setOwnerRef(DocumentReference ownerRef) {
        this.ownerRef = ownerRef;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }
}
