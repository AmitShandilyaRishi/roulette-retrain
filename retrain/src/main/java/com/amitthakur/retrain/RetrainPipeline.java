package com.amitthakur.retrain;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RetrainPipeline {
    private static final String COLLECTION = "roulette_spins";

    public static void main(String[] args) throws Exception {
        initFirebase();
        List<SpinRecord> newRecs = fetchFromFirestore();
        mergeCsv(newRecs);
        List<SpinRecord> all = loadMasterCsv("roulette_data.csv");
        Encoder enc = Encoder.fit(all);
        trainAndExport(all, enc);
    }

    private static void initFirebase() throws Exception {
        FileInputStream svc = new FileInputStream("config/serviceAccountKey.json");
        FirebaseOptions options =  FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(svc))
                .build();
        FirebaseApp.initializeApp(options);
    }

    private static List<SpinRecord> fetchFromFirestore() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION).get();
        List<SpinRecord> list = new ArrayList<>();
        
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            list.add(doc.toObject(SpinRecord.class));
        }
        try (PrintWriter pw = new PrintWriter("new_spins.csv")) {
            pw.println(SpinRecord.csvHeader());
            list.forEach(r -> pw.println(r.toCsv()));
        }
        return list;
    }

    private static void mergeCsv(List<SpinRecord> news) throws Exception {
        Set<String> seen = new HashSet<>();
        List<SpinRecord> merged = new ArrayList<>();

        for (SpinRecord r : loadMasterCsv("roulette_data.csv")) {
            merged.add(r);
            seen.add(r.uniqueKey());
        }
        for (SpinRecord r : news) {
            if (!seen.contains(r.uniqueKey())) {
                merged.add(r);
            }
        }
        try (PrintWriter pw = new PrintWriter("roulette_data.csv")) {
            pw.println(SpinRecord.csvHeader());
            merged.forEach(r -> pw.println(r.toCsv()));
        }
    }

    private static List<SpinRecord> loadMasterCsv(String path) throws Exception {
        List<SpinRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine();  // skip header
            String line;
            while ((line = br.readLine()) != null) {
                list.add(SpinRecord.fromCsv(line));
            }
        }
        return list;
    }

    private static void trainAndExport(List<SpinRecord> data, Encoder enc) throws Exception {
        int N = data.size();
        int D = enc.featureDim();

        // 1) Build Weka Attributes
        ArrayList<Attribute> attrs = new ArrayList<>(D + 1);
        for (int i = 0; i < D; i++) {
            attrs.add(new Attribute("f" + i));
        }
        List<String> classVals = new ArrayList<>();
        for (int i = 0; i < 37; i++) {
            classVals.add(String.valueOf(i));
        }
        attrs.add(new Attribute("class", classVals));

        // 2) Create dataset
        Instances dataset = new Instances("roulette", attrs, N);
        dataset.setClassIndex(D);

        // 3) Populate instances
        for (SpinRecord r : data) {
            double[] fv = enc.extractFeatures(r);
            DenseInstance inst = new DenseInstance(D + 1);
            for (int i = 0; i < D; i++) {
                inst.setValue(attrs.get(i), fv[i]);
            }
            inst.setValue(attrs.get(D), String.valueOf(r.outcome));
            inst.setDataset(dataset);
            dataset.add(inst);
        }

        // 4) Train logistic regression
        Logistic model = new Logistic();
        model.setRidge(1e-3);
        model.buildClassifier(dataset);

        // 5) Extract & pad coefficients
        double[][] coef = model.coefficients();  // shape: (37-1) x (D+1)
        int C = coef.length;                     // should be 36
        double[] bias = new double[37];
        double[][] weights = new double[37][D];
        for (int c = 0; c < C; c++) {
            bias[c] = coef[c][0];
            for (int j = 0; j < D; j++) {
                weights[c][j] = coef[c][j + 1];
            }
        }
        // leave class 36 weights/bias at 0

        // 6) Export JSON
        try (Writer w = new FileWriter("model.json")) {
            new Gson().toJson(new ModelExport(weights, bias), w);
        }
        try (Writer w = new FileWriter("encoders.json")) {
            new Gson().toJson(enc, w);
        }

        System.out.println("Exported model.json & encoders.json");
    }
}
