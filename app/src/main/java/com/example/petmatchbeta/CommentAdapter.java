package com.example.petmatchbeta;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Diese Klasse ist ein RecylerView-Adapter und dient dazu die Kommentare anzuzeigen. Für jeden Kommentar wird der Text angezeigt und wie der Hund des Verfasser heißt. Durch das klicken auf den NAmen wird man auf das Profil weitergeleitet.

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {


    private List<CommentItem> commentList;
    private Context context;
    private String currentUserId;
    private FirebaseFirestore firestore;
    private Map<String, String> petNameCache = new HashMap<>();

    // Konstruktor: Für mit der Kommentar-Liste, dem Kontext und der aktuellen Nutzer-ID
    public CommentAdapter(List<CommentItem> commentList, Context context, String currentUserId) {
        this.commentList = commentList;
        this.context = context;
        this.currentUserId = currentUserId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new CommentViewHolder(view);
    }

    // Bindet die Daten eines Kommentars an die ViewHolder-Instanz
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem comment = commentList.get(position);
        holder.tvMessageText.setText(comment.getText());

        // Laden und Anzzeigen vom Hundenamen des Kommentarverfassers
        String authorId = (comment.getAuthorRef() != null) ? comment.getAuthorRef().getId() : "";
        if (!authorId.isEmpty()) {
            // Überprüft, ob der Hundename bereits im Cache vorhanden ist
            if (petNameCache.containsKey(authorId)) {
                String petName = petNameCache.get(authorId);
                holder.tvAuthorName.setText(petName + ": ");
            } else {
                firestore.collection("pets")
                        .whereEqualTo("halterId", comment.getAuthorRef())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot petDoc = queryDocumentSnapshots.getDocuments().get(0);
                                String petName = petDoc.getString("name");
                                if (petName == null) {
                                    petName = "";
                                }
                                petNameCache.put(authorId, petName);
                                holder.tvAuthorName.setText(petName + ": ");
                            } else {
                                holder.tvAuthorName.setText("");
                            }
                        })
                        .addOnFailureListener(e -> holder.tvAuthorName.setText(""));
            }
            // Klick auf Autor-Namen: Öffnet das Profil des Haustiers des Kommentarverfassers
            holder.tvAuthorName.setOnClickListener(v -> {
                firestore.collection("pets")
                        .whereEqualTo("halterId", comment.getAuthorRef())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot petDoc = queryDocumentSnapshots.getDocuments().get(0);
                                String petDocPath = petDoc.getReference().getPath();
                                Intent intent = new Intent(context, OtherPetProfileActivity.class);
                                intent.putExtra("petDocPath", petDocPath);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "Kein Pet-Dokument gefunden", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Fehler beim Laden des Pet-Dokuments: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        } else {
            holder.tvAuthorName.setText("");
        }
        // Kommentare vom aktuellen User werden links, andere rechts angezeigt
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tvMessageText.getLayoutParams();
        if (comment.getAuthorRef() != null && comment.getAuthorRef().getId().equals(currentUserId)) {
            params.gravity = Gravity.START;
        } else {
            params.gravity = Gravity.END;
        }
        holder.tvMessageText.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText;
        TextView tvAuthorName;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}
