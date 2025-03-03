package com.example.petmatchbeta;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

// Die Klasse beschreibt die Anmelde-Logik. Es sind verschachtelte If else Abfragen um mehrer Möglichkeiten der Anmeldung anzubieten und ggf. Fehler anzuzeigen.

public class LoginActivity extends AppCompatActivity {

    // UI-Elemente
    private EditText etLoginEmail, etLoginPasswort;
    private Button btnLogin;
    private TextView tvPasswortVergessen;

    private FirebaseFirestore firestore;

    // bindet die Views und richtet die Klicklistener ein
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anmelde);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPasswort = findViewById(R.id.etLoginPasswort);
        btnLogin = findViewById(R.id.btnLogin);
        tvPasswortVergessen = findViewById(R.id.tvPasswortVergessen);

        // Firestore initialisieren
        firestore = FirebaseFirestore.getInstance();

        // Klicklistener für den Login-Button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // E-Mail und Trim
                String emailOrUsername = etLoginEmail.getText().toString().trim();
                String password = etLoginPasswort.getText().toString().trim();

                // Felderüberprüfung
                if (TextUtils.isEmpty(emailOrUsername) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(emailOrUsername, password);
            }
        });

        // Klicklistener für "Passwort vergessen"
        tvPasswortVergessen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Wir haben eine E-Mail an Ihre hinterlegte E-Mail-Adresse gesendet mit einem Link, um Ihr Passwort neu zu vergeben.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Nutzer anhand  E-Mail oder Benutzernamens finden
    private void loginUser(String emailOrUsername, String password) {
        // Suche "users"-Collection nach einem "email" Feld
        firestore.collection("users")
                .whereEqualTo("email", emailOrUsername)
                .get()
                .addOnCompleteListener(task -> {
                    // Abfrage erfolgreich, validiere das Passwort
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        validatePassword(task.getResult().getDocuments().get(0).getId(), password);
                    } else {
                        // keine Übereinstimmung dann nach dem Benutzernamen suchen
                        firestore.collection("users")
                                .whereEqualTo("benutzername", emailOrUsername)
                                .get()
                                .addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful() && !innerTask.getResult().isEmpty()) {
                                        validatePassword(innerTask.getResult().getDocuments().get(0).getId(), password);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Benutzer nicht gefunden", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Fehler beim Suchen nach dem Benutzernamen
                                    Toast.makeText(LoginActivity.this, "Fehler bei der Anmeldung: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Fehler beim Suchen nach der E-Mail
                    Toast.makeText(LoginActivity.this, "Fehler bei der Anmeldung: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Überprüfung Passwortübereinstimmung
    private void validatePassword(String userId, String password) {
        firestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String storedPassword = task.getResult().getString("passwordHash");
                String username = task.getResult().getString("benutzername");

                if (password.equals(storedPassword)) {
                    Toast.makeText(LoginActivity.this, "Erfolgreich eingeloggt", Toast.LENGTH_SHORT).show();

                    // Weiterleitung zur FeedActivity, mit userId und username
                    Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Falsches Passwort", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Benutzer nicht gefunden", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(LoginActivity.this, "Fehler beim Abrufen der Benutzerdaten: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
