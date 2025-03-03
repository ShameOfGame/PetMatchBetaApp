package com.example.petmatchbeta;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

// Recycler-View Adapter für die Feed Beiträge. Es zeigt Titel, Inhalt und Profilbild des beitragserstellers an. Das Profilbild wird als Standardbild gesetzt und dann, falls vorhanden, anhand der E-Mail-Adresse aus Firestore aktualisiert. Klick auf das Profilbild um auf das Profil des Hundes zu kommen.

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {


    private List<FeedItem> feedList;
    private Context context;
    private String currentUserId;

    private FirebaseFirestore firestore;

    // Konstruktor: Initialisiert den Adapter
    public FeedAdapter(List<FeedItem> feedList, Context context, String currentUserId) {
        this.feedList = feedList;
        this.context = context;
        this.currentUserId = currentUserId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Neue ViewHolder-Instanz für ein einzelne Feed-Items
    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflatiere das Layout für ein Feed-Item (item_feed.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    // übergibt die Daten eines Feed-Items an den ViewHolder
    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = feedList.get(position);


        holder.tvFeedTitel.setText(item.getTitle());
        holder.tvFeedInhalt.setText(item.getInhalt());


        holder.feedProfileImage.setImageResource(R.drawable.ic_default_profile);
        // Profilbild Zuordnung, anhand der Mail in Firestore
        if(item.getOwnerRef() != null) {
            item.getOwnerRef().get().addOnSuccessListener(doc -> {
                if(doc.exists()){
                    String email = doc.getString("email");
                    int resId = ImageHelper.getProfileImageResource(email);
                    holder.feedProfileImage.setImageResource(resId);
                }
            });
        }

        // Kommentierbutton
        if (item.getOwnerRef() != null) {

            holder.btnKontaktieren.setEnabled(true);
            holder.btnKontaktieren.setAlpha(1.0f);
            holder.btnKontaktieren.setText("Kommentieren");
            holder.btnKontaktieren.setOnClickListener(v -> {
                // Übergabe notwendiger Daten (userId, feedId, feedTitle)
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("userId", currentUserId);
                intent.putExtra("feedId", item.getFeedId());
                intent.putExtra("feedTitle", item.getTitle());
                context.startActivity(intent);
            });
        } else {
            // Bei fehlernder referenz (Besitzer) feld ausgrauen
            holder.btnKontaktieren.setEnabled(false);
            holder.btnKontaktieren.setAlpha(0.5f);
            holder.btnKontaktieren.setText("Kein Kommentar möglich");
        }

        // Klick Profilbild, öffnet die OtherPetProfileActivity
        holder.feedProfileImage.setOnClickListener(v -> {
            DocumentReference petRef = item.getPetRef();
            if (petRef == null) {
                Toast.makeText(context, "Kein Haustier verknüpft", Toast.LENGTH_SHORT).show();
                return;
            }
            String petDocPath = petRef.getPath();
            Intent intent = new Intent(context, OtherPetProfileActivity.class);
            intent.putExtra("petDocPath", petDocPath);
            context.startActivity(intent);
        });
    }

    // Gibt die Anzahl der Feed-Items in der Liste zurück
    @Override
    public int getItemCount() {
        return feedList.size();
    }


    static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView feedProfileImage;
        TextView tvFeedTitel, tvFeedInhalt;
        Button btnKontaktieren;

        // UI-Komponenten des Feed-Items
        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            feedProfileImage = itemView.findViewById(R.id.feed_profile_image);
            tvFeedTitel = itemView.findViewById(R.id.tvFeedTitel);
            tvFeedInhalt = itemView.findViewById(R.id.tvFeedInhalt);
            btnKontaktieren = itemView.findViewById(R.id.btnKontaktieren);
        }
    }
}
