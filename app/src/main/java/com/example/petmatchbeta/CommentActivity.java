package com.example.petmatchbeta;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Diese KLasse ermöglicht dem Nutzer Kommentare zu den Beiträgen zu erfassen und in Echtzeit (Snapshot-Listener) anzuzeigen.

public class CommentActivity extends AppCompatActivity {

    // UI-Komponenten
    private TextView tvNachrichtenTitel;
    private EditText etNachricht;
    private Button btnSenden;
    private RecyclerView rvNachrichten;

    // Firestore und Variablen
    private FirebaseFirestore firestore;
    private List<CommentItem> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String currentUserId;
    private String feedId;
    private String feedTitle;
    private String currentUserPetName = "";

    // Referenzen für den aktuellen Nutzer und den Feed
    private DocumentReference currentUserRef;
    private DocumentReference feedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // UI-Elemente
        tvNachrichtenTitel = findViewById(R.id.tvNachrichtenTitel);
        tvNachrichtenTitel.setText("Kommentiere den Beitrag");
        etNachricht = findViewById(R.id.etNachricht);
        etNachricht.setHint("Dein Kommentar");
        btnSenden = findViewById(R.id.btnSenden);
        rvNachrichten = findViewById(R.id.rvNachrichten);

        // Firestore Daten
        firestore = FirebaseFirestore.getInstance();
        currentUserId = getIntent().getStringExtra("userId");
        feedId = getIntent().getStringExtra("feedId");
        feedTitle = getIntent().getStringExtra("feedTitle");

        // Datenüberprüfung
        if(currentUserId == null || feedId == null) {
            Toast.makeText(this, "Fehlende Informationen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Titel
        if(feedTitle != null && !feedTitle.isEmpty()){
            tvNachrichtenTitel.setText("Kommentiere: " + feedTitle);
        }

        // RecyclerView für die Anzeige der Kommentare
        rvNachrichten.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList, this, currentUserId);
        rvNachrichten.setAdapter(commentAdapter);

        // Firestore Referenzen für den aktuellen Nutzer und den Feed
        currentUserRef = firestore.collection("users").document(currentUserId);
        feedRef = firestore.collection("feed").document(feedId);

        // Zuordnung des Hundes
        firestore.collection("pets")
                .whereEqualTo("halterId", currentUserRef)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()){
                        DocumentSnapshot petDoc = querySnapshot.getDocuments().get(0);
                        currentUserPetName = petDoc.getString("name");
                    }
                });


        btnSenden.setOnClickListener(v -> sendComment());
        listenForComments();
    }

    // Speicherung Kommentar (mit Überprufung ob leer)
    private void sendComment() {
        String text = etNachricht.getText().toString().trim();
        if(text.isEmpty()){
            Toast.makeText(this, "Bitte Kommentar eingeben", Toast.LENGTH_SHORT).show();
            return;
        }
        // Erstelle ein Map-Objekt mit den Kommentardaten
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("text", text);
        commentData.put("createdAt", FieldValue.serverTimestamp());
        commentData.put("authorId", currentUserRef);
        commentData.put("feedRef", feedRef);
        commentData.put("authorPetName", currentUserPetName);

        // Dokument-ID basierend auf Zeit und Speicherung des Kommentars in comments
        String commentDocId = "comment_" + System.currentTimeMillis();
        firestore.collection("comments").document(commentDocId)
                .set(commentData)
                .addOnSuccessListener(aVoid -> {
                    etNachricht.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(CommentActivity.this, "Fehler beim Senden: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Snapshot-Listener um Kommentare in Echtzeit zu laden und anzuzeigen
    private void listenForComments() {
        firestore.collection("comments")
                .whereEqualTo("feedRef", feedRef)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if(e != null){
                        Toast.makeText(CommentActivity.this, "Fehler beim Laden der Kommentare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Aktualisierung Kommentar-Liste, falls Daten vorhanden sind
                    if(queryDocumentSnapshots != null){
                        commentList.clear();
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            CommentItem comment = new CommentItem();
                            comment.setText(doc.getString("text"));
                            Timestamp ts = doc.getTimestamp("createdAt");
                            comment.setCreatedAt(ts != null ? ts.toDate().getTime() : 0L);
                            comment.setAuthorRef(doc.getDocumentReference("authorId"));
                            comment.setAuthorPetName(doc.getString("authorPetName"));
                            commentList.add(comment);
                        }
                        // Aktualisiere den Adapter und scrolle bei neuen Kommentaren zum letzten Element
                        commentAdapter.notifyDataSetChanged();
                        if(commentList.size() > 0)
                            rvNachrichten.scrollToPosition(commentList.size() - 1);
                    }
                });
    }
}
