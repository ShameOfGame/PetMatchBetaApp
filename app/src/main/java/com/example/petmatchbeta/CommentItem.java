package com.example.petmatchbeta;

import com.google.firebase.firestore.DocumentReference;

// Diese Klasse dient als Datenmodell für Kommentare. Sie speichert das unten genannte. Getter und Setter und die Daten ggf. zu modifizieren.

public class CommentItem {

    private String text;
    private long createdAt;
    private DocumentReference authorRef;
    private String authorPetName;

    // Standard-Konstruktor (wichtig für Firestore-Datenabruf)
    public CommentItem() {}

    // Konstruktor mit Parametern zur Initialisierung aller Attribute
    public CommentItem(String text, long createdAt, DocumentReference authorRef, String authorPetName) {
        this.text = text;
        this.createdAt = createdAt;
        this.authorRef = authorRef;
        this.authorPetName = authorPetName;
    }

    // Getter-Methode für den Kommentartext
    public String getText() {
        return text;
    }

    // Setter-Methode für den Kommentartext
    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public DocumentReference getAuthorRef() {
        return authorRef;
    }

    public void setAuthorRef(DocumentReference authorRef) {
        this.authorRef = authorRef;
    }

    public String getAuthorPetName() {
        return authorPetName;
    }

    public void setAuthorPetName(String authorPetName) {
        this.authorPetName = authorPetName;
    }
}
