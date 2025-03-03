package com.example.petmatchbeta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

// Zeigt die Profilseite des angeklickten Hundes an, über den Dokumenten-Pfad werden die nötigen Daten aus Firestore geladen.

public class OtherPetProfileActivity extends AppCompatActivity {

    // UI-Komponenten
    private ImageView ivProfilbildOther;
    private EditText etHaustierNameOther, etGeburtsdatumOther, etRasseOther,
            etDasMagIchOther, etDasMagIchNichtOther;
    // Textfelder (als EditText genutzt, nur zur Anzeige)
    private EditText tvGeschlechtOther, tvKastriertOther;

    private FirebaseFirestore firestore;
    private String petDocPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (Exception e) {
            // Fehlermeldung anzeigen (Sicherheitsprovider)
            Toast.makeText(this, "Provider Installer fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        setContentView(R.layout.activity_other_pet_profile);

        // Initialisieren der Views aus dem Layout
        ivProfilbildOther = findViewById(R.id.ivProfilbildOther);
        etHaustierNameOther = findViewById(R.id.etHaustierNameOther);
        etGeburtsdatumOther = findViewById(R.id.edit_tier_geburtsdatumOther);
        etRasseOther = findViewById(R.id.edit_tier_rasseOther);
        etDasMagIchOther = findViewById(R.id.edit_das_mag_ichOther);
        etDasMagIchNichtOther = findViewById(R.id.edit_das_mag_ich_nichtOther);
        tvGeschlechtOther = findViewById(R.id.tvGeschlechtOther);
        tvKastriertOther = findViewById(R.id.tvKastriertOther);

        firestore = FirebaseFirestore.getInstance();

        // Holt den Dokumentpfad des Haustiers aus dem Intent
        petDocPath = getIntent().getStringExtra("petDocPath");

        if (petDocPath == null || petDocPath.isEmpty()) {
            Toast.makeText(this, "Kein Pet-Dokument angegeben", Toast.LENGTH_SHORT).show();
            finish(); // Beendet die Activity, wenn kein Dokumentpfad vorhanden ist
            return;
        }

        loadPetData();
    }

    // Lädt die Daten des Hundes aus Firestore und zeigt diese in den Views an
    private void loadPetData() {

        DocumentReference petRef = firestore.document(petDocPath);
        petRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Liest die einzelnen Felder aus dem Dokument
                String name = doc.getString("name");
                String geburtsdatum = doc.getString("geburtsdatum");
                String rasse = doc.getString("rasse");
                String geschlecht = doc.getString("geschlecht");
                Boolean kastriertBool = doc.getBoolean("kastriert");
                String magIch = doc.getString("dasMagIch");
                String magIchNicht = doc.getString("dasMagIchNicht");

                // Setze die ausgelesenen Daten in die entsprechenden Felder, falls sie nicht null sind
                if (name != null) etHaustierNameOther.setText(name);
                if (geburtsdatum != null) etGeburtsdatumOther.setText(geburtsdatum);
                if (rasse != null) etRasseOther.setText(rasse);
                if (magIch != null) etDasMagIchOther.setText(magIch);
                if (magIchNicht != null) etDasMagIchNichtOther.setText(magIchNicht);
                if (geschlecht != null) {
                    tvGeschlechtOther.setText(geschlecht);
                }
                if (kastriertBool != null) {
                    tvKastriertOther.setText(kastriertBool ? "Ja" : "Nein");
                }

                // Profilbild setzen über die Owner-Reference (Halter des Haustiers)
                DocumentReference ownerRef = doc.getDocumentReference("halterId");
                if (ownerRef != null) {
                    ownerRef.get().addOnSuccessListener(ownerDoc -> {
                        if (ownerDoc.exists()){
                            // ermittelt das Bild über ImageHelper
                            String ownerEmail = ownerDoc.getString("email");
                            int resId = ImageHelper.getProfileImageResource(ownerEmail);
                            ivProfilbildOther.setImageResource(resId);
                        } else {
                            // Falls keine Zurodnung, Standardbild
                            ivProfilbildOther.setImageResource(R.drawable.ic_default_profile);
                        }
                    }).addOnFailureListener(e -> {
                        // Bei Fehlern ebenfalls das Standardprofilbild setzen
                        ivProfilbildOther.setImageResource(R.drawable.ic_default_profile);
                    });
                } else {
                    // Falls keine Owner-Reference, Standardprofilbild setzen
                    ivProfilbildOther.setImageResource(R.drawable.ic_default_profile);
                }
            } else {
                // Fehlermeldung, wenn das Haustier-Dokument nicht gefunden wurde, und beendet die Activity
                Toast.makeText(this, "Haustier-Dokument nicht gefunden", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
