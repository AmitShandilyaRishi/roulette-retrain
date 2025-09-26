package com.amitthakur.roulette.di;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/*
@SuppressWarnings("unused")
@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {
    @Provides @Singleton
    public FirebaseFirestore provideFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
        );
        return db;
    }
}*/

@SuppressWarnings("unused")
@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {

    @Provides
    @Singleton
    public FirebaseFirestore provideFirestore() {
        // Offline persistence is enabled by default,
        // so no need to call the deprecated setPersistenceEnabled().
        return FirebaseFirestore.getInstance();
    }
}
