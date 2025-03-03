package com.example.petmatchbeta;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Die Klasse ist dafür gedacht die Daten des Profils zu sammeln und zu speichern/anzuzeigen. Erst durch das drücken des Bearbeitenbuttons ist eine Eintragung der Daten möglich. Beim erneuten besuch der Seite, werden gespeicherte Daten geladen und angezeigt.

public class ProfileActivity extends AppCompatActivity {

    // UI-Elemente:
    private ImageView ivProfilbild;
    private EditText etHaustierName, etGeburtsdatum, etRasse, etDasMagIch, etDasMagIchNicht;
    private Spinner spinnerGeschlecht, spinnerKastriert;
    private Button btnProfilbildHochladen, btnSpeichern, btnBearbeiten, btnZurueckFeed;

    // Firestore-Instanz und Variablen für Benutzer- und Haustier-Daten
    private FirebaseFirestore firestore;
    private String userId;       // Wird aus dem Intent (Login) übergeben
    private String petDocId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (Exception e) {
            Toast.makeText(this, "Provider Installer fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        setContentView(R.layout.activity_profile);

        // Initialisiert die UI-Elemente aus dem Layout
        ivProfilbild = findViewById(R.id.ivProfilbild);
        etHaustierName = findViewById(R.id.etHaustierName);
        etGeburtsdatum = findViewById(R.id.edit_tier_geburtsdatum);
        etRasse = findViewById(R.id.edit_tier_rasse);
        etDasMagIch = findViewById(R.id.edit_das_mag_ich);
        etDasMagIchNicht = findViewById(R.id.edit_das_mag_ich_nicht);
        spinnerGeschlecht = findViewById(R.id.spinner_geschlecht);
        spinnerKastriert = findViewById(R.id.spinner_kastriert);
        btnProfilbildHochladen = findViewById(R.id.btnProfilbildHochladen);
        btnSpeichern = findViewById(R.id.btnSpeichern);
        btnBearbeiten = findViewById(R.id.btnBearbeiten);
        btnZurueckFeed = findViewById(R.id.btnZurueckFeed);

        firestore = FirebaseFirestore.getInstance();

        // UserId aus dem Intent (wird beim Login übergeben)
        userId = getIntent().getStringExtra("userId");

        // Profilbild per E-Mail des Nutzers
        firestore.collection("users").document(userId)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("email");
                        int resId = ImageHelper.getProfileImageResource(email);
                        ivProfilbild.setImageResource(resId);
                    }
                });

        // Spinner Optionen
        ArrayAdapter<CharSequence> adapterGeschlecht = ArrayAdapter.createFromResource(
                this,
                R.array.geschlecht_options,
                android.R.layout.simple_spinner_item
        );
        adapterGeschlecht.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGeschlecht.setAdapter(adapterGeschlecht);

        ArrayAdapter<CharSequence> adapterKastriert = ArrayAdapter.createFromResource(
                this,
                R.array.kastriert_options,
                android.R.layout.simple_spinner_item
        );
        adapterKastriert.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKastriert.setAdapter(adapterKastriert);

        // Felder nicht editierbar
        setFieldsEnabled(false);

        // Profilbild-Upload nur Text, da zu kompliziert (habe es mehrmals probiert und mit den Code komplett zerschossen)
        btnProfilbildHochladen.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this,
                    "Diese Version ist noch in der Beta und ein Hochladen des Bildes ist noch nicht möglich",
                    Toast.LENGTH_LONG).show();
        });

        // Schaltet die Eingabefelder in den Editiermodus
        btnBearbeiten.setOnClickListener(v -> setFieldsEnabled(true));

        // Speichert die Daten in Firestore
        btnSpeichern.setOnClickListener(v -> savePetData());

        // Navigiert zurück zur FeedActivity und übergibt die userId
        btnZurueckFeed.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FeedActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        // Ladet vorhandene Haustier-Daten (falls vorhanden) aus Firestore
        loadPetData();
    }

    private void setFieldsEnabled(boolean enabled) {
        etHaustierName.setEnabled(enabled);
        etGeburtsdatum.setEnabled(enabled);
        etRasse.setEnabled(enabled);
        etDasMagIch.setEnabled(enabled);
        etDasMagIchNicht.setEnabled(enabled);
        spinnerGeschlecht.setEnabled(enabled);
        spinnerKastriert.setEnabled(enabled);

        isEditMode = enabled; // Aktualisiere den Editiermodus-Status
    }

    private void loadPetData() {
        if (userId == null || userId.isEmpty()) {
            return; // Wenn userId fehlt, keine Daten laden
        }

        // Erzeugt eine Referenz zum aktuellen Benutzer-Dokument
        DocumentReference userRef = firestore.collection("users").document(userId);

        // Sucht in der "pets"-Collection nach "halterId" gleich userRef ist
        firestore.collection("pets")
                .whereEqualTo("halterId", userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Wenn es gefunden wird, speichere die Dokument-ID und befülle die Felder
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            petDocId = doc.getId();
                            fillFields(doc);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this,
                                "Fehler beim Laden der Daten: " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillFields(DocumentSnapshot doc) {
        if (doc == null) return;

        // Lese der Daten
        String name = doc.getString("name");
        String geburtsdatum = doc.getString("geburtsdatum");
        String rasse = doc.getString("rasse");
        String geschlecht = doc.getString("geschlecht");
        Boolean kastriertBool = doc.getBoolean("kastriert");
        String magIch = doc.getString("dasMagIch");
        String magIchNicht = doc.getString("dasMagIchNicht");

        // Angaben in die Felder übergeben
        if (name != null) etHaustierName.setText(name);
        if (geburtsdatum != null) etGeburtsdatum.setText(geburtsdatum);
        if (rasse != null) etRasse.setText(rasse);
        if (magIch != null) etDasMagIch.setText(magIch);
        if (magIchNicht != null) etDasMagIchNicht.setText(magIchNicht);

        if (geschlecht != null) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerGeschlecht.getAdapter();
            int position = adapter.getPosition(geschlecht);
            if (position >= 0) {
                spinnerGeschlecht.setSelection(position);
            }
        }

        if (kastriertBool != null) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerKastriert.getAdapter();
            int pos = kastriertBool ? adapter.getPosition("ja") : adapter.getPosition("nein");
            if (pos >= 0) {
                spinnerKastriert.setSelection(pos);
            }
        }

        // Sperrung nach ausfüllen
        setFieldsEnabled(false);
    }

    private void savePetData() {
        // Überprüft, ob sich die Activity im Editiermodus befindet
        if (!isEditMode) {
            Toast.makeText(this, "Bitte zuerst auf 'Bearbeiten' klicken, um Daten zu ändern.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Überprüft, ob eine gültige userId vorhanden ist
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Kein Benutzer gefunden, Daten können nicht gespeichert werden.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sammlet die Daten aus den Eingabefeldern
        String name = etHaustierName.getText().toString().trim();
        String geburtsdatum = etGeburtsdatum.getText().toString().trim();
        String rasse = etRasse.getText().toString().trim();
        String selectedGeschlecht = spinnerGeschlecht.getSelectedItem().toString();
        String selectedKastriert = spinnerKastriert.getSelectedItem().toString();
        String magIch = etDasMagIch.getText().toString().trim();
        String magIchNicht = etDasMagIchNicht.getText().toString().trim();

        boolean isKastriert = selectedKastriert.equalsIgnoreCase("ja");

        // Erstellt eine Map mit den gesammelten Daten
        Map<String, Object> petData = new HashMap<>();
        petData.put("name", name);
        petData.put("geburtsdatum", geburtsdatum);
        petData.put("rasse", rasse);
        petData.put("geschlecht", selectedGeschlecht);
        petData.put("kastriert", isKastriert);
        petData.put("dasMagIch", magIch);
        petData.put("dasMagIchNicht", magIchNicht);

        // Speichert "halterId" als DocumentReference in die Map
        DocumentReference userRef = firestore.collection("users").document(userId);
        petData.put("halterId", userRef);

        // Fallskein Pet-Dokument existiert, ein neues erstellen
        if (petDocId == null) {
            petDocId = "pet_" + System.currentTimeMillis();
        }

        // Speichert die Werte in Pets
        firestore.collection("pets").document(petDocId)
                .set(petData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Daten erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
                    // Sperre die Felder, nachdem die Daten gespeichert wurden
                    setFieldsEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
