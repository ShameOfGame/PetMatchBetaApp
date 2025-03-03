package com.example.petmatchbeta;

import android.text.TextUtils;
import com.example.petmatchbeta.R;

// Ordner mit Bildern die für das Projekt benutzt werden (Profilbilder)

public class ImageHelper {

    public static int getProfileImageResource(String email) {
        // E-Mail leer oder null ist, wird das Standard-Profilbild zurückgegeben
        if (TextUtils.isEmpty(email)) {
            return R.drawable.ic_default_profile;
        }
        // Konvertiert die E-Mail in Kleinbuchstaben, um Vergleiche unabhängig von der Groß-/Kleinschreibung zu machen
        String lowerEmail = email.toLowerCase();
        // switch-Anweisung, für Bild basierend auf der E-Mail
        switch (lowerEmail) {
            case "sabrina@gmx.de":
                return R.drawable.tamaskan_wolf;
            case "admin@gmail.com":
                return R.drawable.rottweiler_susi;
            case "test@web.de":
                return R.drawable.dackel_wuffi;
            case "mila@gmail.com":
                return R.drawable.labrador_joesy;
            case "kujawski.p@web.de":
                return R.drawable.leonberger_hans;
            case "shameofgame@web.de":
                return R.drawable.golden_lana;
            default:
                // Gibt das Standard-Profilbild zurück, wenn keins passt
                return R.drawable.ic_default_profile;
        }
    }
}
