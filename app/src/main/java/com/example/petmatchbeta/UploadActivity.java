package com.example.petmatchbeta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

// Nutzer kann einen Beitrag (Feed-Item) erstellen und hochladen, dieser wird gespeichert und mit den nötigen Referenzen versehen um die Zuordnung eindeutig zu machen.

public class UploadActivity extends AppCompatActivity {

    // UI-Elemente
    private Button btnBildAuswaehlen, btnBeitragHochladen;
    private EditText etBeitragTitel, etBeitragInhalt;

    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btnBildAuswaehlen = findViewById(R.id.btnBildAuswaehlen);
        btnBeitragHochladen = findViewById(R.id.btnBeitragHochladen);
        etBeitragTitel = findViewById(R.id.etBeitragTitel);
        etBeitragInhalt = findViewById(R.id.etBeitragInhalt);

        firestore = FirebaseFirestore.getInstance();

        // Holt die userId aus dem Intent, der von der vorherigen Activity
        userId = getIntent().getStringExtra("userId");
        // Überprüft, ob die userId gültig ist
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Kein Benutzer gefunden", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Klicklistener für den Button "Bild auswählen"
        btnBildAuswaehlen.setOnClickListener(v ->
                Toast.makeText(this,
                        "Diese Funktion befindet sich noch in der Entwicklung und wird im Laufe der Beta Phase integriert",
                        Toast.LENGTH_LONG).show());

        btnBeitragHochladen.setOnClickListener(v -> saveFeedItem());
    }


    private void saveFeedItem() {
        // Erzeugt eine Referenz zum Benutzer-Dokument in der "users"-Collection
        DocumentReference userRef = firestore.collection("users").document(userId);
        // Sucht in der "pets"-Collection nach einem Dokument, dessen "halterId" gleich userRef ist
        firestore.collection("pets")
                .whereEqualTo("halterId", userRef)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentReference petRef = querySnapshot.getDocuments().get(0).getReference();
                        petRef.get().addOnSuccessListener(petDoc -> {
                            String petName = petDoc.getString("name");
                            // Ruft die Methode zum Hochladen des Feed-Items auf und übergebe petRef und petName
                            uploadFeedItem(petRef, petName);
                        }).addOnFailureListener(e ->
                                Toast.makeText(this, "Fehler beim Abrufen des Haustiernamen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        // Fehlermeldung
                        Toast.makeText(this, "Kein Pet-Dokument gefunden. Bitte erst ein Haustier anlegen.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Fehler beim Suchen nach Pet-Dokument: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadFeedItem(DocumentReference petRef, String petName) {
        String title = etBeitragTitel.getText().toString().trim();
        String inhalt = etBeitragInhalt.getText().toString().trim();

        // Überprüft, ob Titel oder Inhalt leer sind
        if (title.isEmpty() || inhalt.isEmpty()) {
            Toast.makeText(this, "Bitte Titel und Inhalt ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Map, für die Feed-Daten
        Map<String, Object> feedData = new HashMap<>();
        feedData.put("titel", title);
        feedData.put("inhalt", inhalt);
        feedData.put("createdAt", FieldValue.serverTimestamp());
        feedData.put("petId", petRef);
        feedData.put("petName", petName);
        // Speichert die "halterId" als DocumentReference
        feedData.put("halterId", firestore.collection("users").document(userId));
        feedData.put("imageUrl", "");

        // Feed-Dokument-ID mit der aktuellen Zeit
        String feedDocId = "feed_" + System.currentTimeMillis();
        // Speichert das Feed-Dokument in der "feed"-Collection
        firestore.collection("feed").document(feedDocId)
                .set(feedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Beitrag erfolgreich hochgeladen!", Toast.LENGTH_SHORT).show();
                    // Weiterleitung FeedActivity und übergebe die userId
                    Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        // Fehlermeldung, falls das Hochladen fehlschlägt
                        Toast.makeText(this, "Fehler beim Hochladen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
