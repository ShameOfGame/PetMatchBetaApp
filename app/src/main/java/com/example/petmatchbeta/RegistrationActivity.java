package com.example.petmatchbeta;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// Registrierung für neue Nutzer mit vielen Voraussetzungen (Passwort) Der Textwatcher zeigt den Fortschritt in Echtzeit an. Erst wenn alles ausgefüllt ist, ist eine Registrierung und Speicherung der Daten möglich.

public class RegistrationActivity extends AppCompatActivity {

    // UI-Elemente
    private EditText etBenutzername, etEmail, etEmailWiederholen, etKennwort, etKennwortWiederholen;
    private CheckBox checkboxZustimmung;
    private Button btnRegistrieren;

    private FirebaseFirestore firestore;

    private TextView tvKennwortZeichen, tvKennwortSonderzeichen, tvKennwortZahl, tvKennwortGrossbuchstabe;

    // dunkles Olivgrün für erfolgreiche Validierungen
    private static final int DARK_GREEN = Color.parseColor("#556B2F");

    // Validierungsstatus-Variablen
    private boolean isUsernameValid = false;
    private boolean isEmailValid = false;
    private boolean isEmailMatching = false;
    private boolean isPasswordValid = false;
    private boolean isPasswordMatching = false;
    private boolean arePasswordRequirementsMet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrierung);

        // Views aus dem Layout
        etBenutzername = findViewById(R.id.benutzername);
        etEmail = findViewById(R.id.email_adresse);
        etEmailWiederholen = findViewById(R.id.email_wiederholen);
        etKennwort = findViewById(R.id.kennwort);
        etKennwortWiederholen = findViewById(R.id.kennwort_wiederholen);
        checkboxZustimmung = findViewById(R.id.checkbox_zustimmung);
        btnRegistrieren = findViewById(R.id.btn_registrieren);

        // TextViews für Passwortvoraussetzungen
        tvKennwortZeichen = findViewById(R.id.kennwort_bedingung_zeichen);
        tvKennwortSonderzeichen = findViewById(R.id.kennwort_bedingung_sonderzeichen);
        tvKennwortZahl = findViewById(R.id.kennwort_bedingung_zahl);
        tvKennwortGrossbuchstabe = findViewById(R.id.kennwort_bedingung_grossbuchstabe);

        firestore = FirebaseFirestore.getInstance();

        // TextWatcher für den Benutzernamen, überwacht Eingabeänderungen und prüft Verfügbarkeit
        etBenutzername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Prüfung Benutzername bereits vergeben
                checkUsernameAvailability(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Gemeinsamer TextWatcher für die beiden E-Mails
        TextWatcher emailWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  übereinstimmung beider Email felder
                validateEmails();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        etEmail.addTextChangedListener(emailWatcher);
        etEmailWiederholen.addTextChangedListener(emailWatcher);

        // Passwortfelder
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Validiert das Passwort anhand von Anforderungen und ob beide Passwortfelder übereinstimmen
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        etKennwort.addTextChangedListener(passwordWatcher);
        etKennwortWiederholen.addTextChangedListener(passwordWatcher);

        btnRegistrieren.setOnClickListener(v -> {
            // Überprüfe, ob alle Übereisntimmungen erfolgreich waren
            if (validateFields()) {
                // Speichert den neuen Benutzer in Firestore
                saveUserToFirestore();
            } else {
                // Fehlermeldungen
                if (!isUsernameValid) {
                    Toast.makeText(RegistrationActivity.this, "Benutzername schon vergeben, bitte neuen eingeben", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Bitte alle Anforderungen erfüllen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Prüft, ob der Benutzername bereits in der Datenbank existiert
    private void checkUsernameAvailability(String username) {
        if (username.isEmpty()) {
            // Standardfarbe, wenn der Benutzername leer ist
            etBenutzername.setTextColor(Color.BLACK);
            isUsernameValid = false;
            return;
        }
        firestore.collection("users")
                .whereEqualTo("benutzername", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Benutzername gefunden, setze die Textfarbe auf Rot
                        etBenutzername.setTextColor(Color.RED);
                        isUsernameValid = false;
                    } else {
                        //wenn gültig, dann Schrift Grün
                        etBenutzername.setTextColor(DARK_GREEN);
                        isUsernameValid = true;
                    }
                })
                .addOnFailureListener(e -> {
                    // Bei Fehlern Text Rot
                    etBenutzername.setTextColor(Color.RED);
                    isUsernameValid = false;
                });
    }

    // Übereinstimmung beider E-Mail Felder
    private void validateEmails() {
        String email = etEmail.getText().toString().trim();
        String emailRepeat = etEmailWiederholen.getText().toString().trim();
        // Überprüft, ob die E-Mail im korrekten Format vorliegt
        boolean validFormat = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (!validFormat) {
            etEmail.setTextColor(Color.RED);
            isEmailValid = false;
        } else {
            etEmail.setTextColor(DARK_GREEN);
            isEmailValid = true;
        }

        // Überprüft, ob beide E-Mail-Felder übereinstimmen und nicht leer sind
        if (!email.equals(emailRepeat) || emailRepeat.isEmpty()) {
            etEmailWiederholen.setTextColor(Color.RED);
            isEmailMatching = false;
        } else {
            etEmailWiederholen.setTextColor(DARK_GREEN);
            isEmailMatching = true;
        }
    }

    // das gleiche wie bei E-Mail, nur mit Passwort
    private void validatePasswords() {
        String pwd = etKennwort.getText().toString();
        String pwdRepeat = etKennwortWiederholen.getText().toString();

        // Mindestlänge von 8 Zeichen
        boolean lengthValid = pwd.length() >= 8;
        if (lengthValid) {
            tvKennwortZeichen.setTextColor(DARK_GREEN);
        } else {
            tvKennwortZeichen.setTextColor(Color.RED);
        }

        // Sonderzeichen
        boolean specialValid = Pattern.compile("[^a-zA-Z0-9]").matcher(pwd).find();
        if (specialValid) {
            tvKennwortSonderzeichen.setTextColor(DARK_GREEN);
        } else {
            tvKennwortSonderzeichen.setTextColor(Color.RED);
        }

        // Mindestens eine Zahl
        boolean digitValid = Pattern.compile("\\d").matcher(pwd).find();
        if (digitValid) {
            tvKennwortZahl.setTextColor(DARK_GREEN);
        } else {
            tvKennwortZahl.setTextColor(Color.RED);
        }

        // Mindestens ein Großbuchstabe
        boolean uppercaseValid = Pattern.compile("[A-Z]").matcher(pwd).find();
        if (uppercaseValid) {
            tvKennwortGrossbuchstabe.setTextColor(DARK_GREEN);
        } else {
            tvKennwortGrossbuchstabe.setTextColor(Color.RED);
        }

        // Speichert, ob alle Passwortvoraussetzungen erfüllt sind
        arePasswordRequirementsMet = lengthValid && specialValid && digitValid && uppercaseValid;

        if (!pwd.equals(pwdRepeat) || pwdRepeat.isEmpty()) {
            etKennwortWiederholen.setTextColor(Color.RED);
            isPasswordMatching = false;
        } else {
            etKennwortWiederholen.setTextColor(DARK_GREEN);
            isPasswordMatching = true;
        }

        isPasswordValid = !pwd.isEmpty() && arePasswordRequirementsMet;
    }

    // Überprüft, ob alle Validierungen (Benutzername, E-Mail, Passwort und Zustimmung) erfüllt sind
    private boolean validateFields() {
        return isUsernameValid && isEmailValid && isEmailMatching
                && isPasswordValid && isPasswordMatching && checkboxZustimmung.isChecked();
    }

    // Speichert den neuen Benutzer in Firestore
    private void saveUserToFirestore() {

        String userId = "user_" + System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("benutzername", etBenutzername.getText().toString());
        user.put("email", etEmail.getText().toString());
        user.put("passwordHash", etKennwort.getText().toString());
        user.put("createdAt", System.currentTimeMillis());

        // Speichert die Benutzerdaten in der "users"-Collection in Firestore
        firestore.collection("users").document(userId).set(user).addOnSuccessListener(aVoid -> {
            Toast.makeText(RegistrationActivity.this, "Registrierung erfolgreich", Toast.LENGTH_SHORT).show();

            // Weiterleitung zur FeedActivity und Übergabe von userId, username und E-Mail
            Intent intent = new Intent(RegistrationActivity.this, FeedActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("username", etBenutzername.getText().toString());
            intent.putExtra("email", etEmail.getText().toString());
            startActivity(intent);

            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(RegistrationActivity.this, "Fehler bei der Registrierung", Toast.LENGTH_SHORT).show();
        });
    }
}
