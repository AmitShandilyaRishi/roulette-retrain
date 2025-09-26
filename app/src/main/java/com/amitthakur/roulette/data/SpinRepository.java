package com.amitthakur.roulette.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.amitthakur.roulette.model.SpinRecord;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SpinRepository {
    private final CollectionReference ref;

    @Inject
    public SpinRepository(FirebaseFirestore db) {
        this.ref = db.collection("roulette_spins");
    }

    public void addSpin(SpinRecord r) {
        ref.add(r);
    }

    public LiveData<List<SpinRecord>> getRecentSpins() {
        MutableLiveData<List<SpinRecord>> live = new MutableLiveData<>();
        ref.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snap, e) -> {
                    if (e == null && snap != null) {
                        live.postValue(snap.toObjects(SpinRecord.class));
                    }
                });
        return live;
    }
}


