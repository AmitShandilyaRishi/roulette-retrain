package com.amitthakur.roulette.ml;


import android.content.Context;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

public class ModelManager {
    private final Context ctx;
    private final FirebaseStorage storage;

    public ModelManager(Context ctx) {
        this.ctx = ctx;
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Downloads the latest TFLite model and encoder metadata.
     * On success invokes onReady(modelPath, encodersPath).
     * On any failure invokes onError().
     */
    public void fetchLatest(Listener listener) {
        File modelFile    = new File(ctx.getFilesDir(), "roulette_model.tflite");
        File encodersFile = new File(ctx.getFilesDir(), "encoders.json");

        StorageReference modelRef    = storage.getReference("models/roulette_model.tflite");
        StorageReference encodersRef = storage.getReference("models/encoders.json");

        // Download encoders.json first
        encodersRef.getFile(encodersFile)
                .addOnSuccessListener(task -> {
                    // Then download the TFLite model
                    modelRef.getFile(modelFile)
                            .addOnSuccessListener(t2 ->
                                    listener.onReady(modelFile.getAbsolutePath(),
                                            encodersFile.getAbsolutePath())
                            )
                            .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
    }

    public interface Listener {
        void onReady(String modelPath, String encodersJsonPath);
        void onError(Exception e);
    }
}


