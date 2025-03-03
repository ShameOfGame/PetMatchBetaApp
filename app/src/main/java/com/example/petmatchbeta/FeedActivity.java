package com.example.petmatchbeta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// Die Klasse enthält zwei Klicklistener Profil-Button, der zur Profilansicht wechselt, Button „Beitrag erstellen“, der zur Upload-Activity führt. Activity lädt zudem das Profilbild des Nutzers aus Firestore. Jedes erneute Aufrufen der Activity lädt den Feed neu

public class FeedActivity extends AppCompatActivity {

    // UI-Elemente
    private CircleImageView btnProfil;
    private Button btnBildHochladen; // Zeigt "Beitrag erstellen"
    private RecyclerView rvFeedListe;

    // Variablen zur Speicherung
    private String userId;
    private FirebaseFirestore firestore;

    // Liste und Adapter
    private List<FeedItem> feedList = new ArrayList<>();
    private FeedAdapter feedAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Holt die userId aus dem Intent
        userId = getIntent().getStringExtra("userId");


        btnProfil = findViewById(R.id.btnProfil);
        btnBildHochladen = findViewById(R.id.btnBildHochladen);
        rvFeedListe = findViewById(R.id.rvFeedListe);

        // Weiterleitung ProfileActivity mit der userId
        btnProfil.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // "Beitrag erstellen"-Button, um UploadActivity mit der userId zu starten
        btnBildHochladen.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, UploadActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // Konfiguriere den RecyclerView mit einem LinearLayoutManager und setze den FeedAdapter
        rvFeedListe.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(feedList, this, userId);
        rvFeedListe.setAdapter(feedAdapter);


        firestore = FirebaseFirestore.getInstance();

        // Profilbild laden anhand der E-Mail des Nutzers aus der "users"-Sammlung
        firestore.collection("users").document(userId)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("email");
                        int resId = ImageHelper.getProfileImageResource(email);
                        btnProfil.setImageResource(resId);
                    }
                });

        // Laden von Feed-Items aus Firestore
        loadFeedItems();
    }

    // Aufruf, wenn die Activity wieder in den Vordergrund tritt, lädt die Feed-Items erneut
    @Override
    protected void onResume() {
        super.onResume();
        loadFeedItems();
    }

    // Lädt alle Feed-Beiträge aus Firestore, sortiert nach Erstellungszeit (neueste zuerst)
    private void loadFeedItems() {
        firestore.collection("feed")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Leere die aktuelle Liste, bevor neue Daten hinzugefügt werden
                    feedList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        FeedItem item = new FeedItem();
                        item.setFeedId(doc.getId());
                        item.setTitle(doc.getString("titel"));
                        item.setInhalt(doc.getString("inhalt"));
                        Timestamp ts = doc.getTimestamp("createdAt");
                        long createdAt = (ts != null) ? ts.toDate().getTime() : 0L;
                        item.setCreatedAt(createdAt);
                        item.setPetRef(doc.getDocumentReference("petId"));
                        item.setOwnerRef(doc.getDocumentReference("halterId"));
                        item.setPetName(doc.getString("petName"));
                        feedList.add(item);
                    }
                    // Information an Adapter, dass sich die Daten geändert haben
                    feedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FeedActivity.this, "Fehler beim Laden der Beiträge: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
