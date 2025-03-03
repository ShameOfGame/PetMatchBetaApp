package com.example.petmatchbeta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

// only god and i know the code... now only god knows. kleiner Spaß Herr Zimmermann :)

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setzt das Layout der Activity auf activity_erster_screen.xml
        setContentView(R.layout.activity_erster_screen);

        //navigiert zur Anmeldung
        findViewById(R.id.btnAnmelden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Erstellt einen Intent, um zur LoginActivity zu wechseln
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // navigiert zur Registrierung
        findViewById(R.id.btnRegistrieren).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Erstellt einen Intent, um zur RegistrationActivity zu wechseln
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }
}
