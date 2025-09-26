package com.amitthakur.roulette.ml;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FeatureExtractor {
    private final int K;
    public final int NUM_POCKETS;
    private final Map<String,Integer> speedMap;
    private final Map<String,Integer> dirMap;
    private final String[] dealerLabels;
    private final String[] tableLabels;
    private final String[] materialLabels;
    private final String[] techniqueLabels;
    private final String[] dropSectorLabels;
    private final float[] contMean;
    private final float[] contStd;
    public final int TOTAL_DIM;

    public FeatureExtractor(String encodersJsonPath, Context ctx) throws Exception {
        InputStream is = encodersJsonPath.startsWith("/")
                ? new FileInputStream(encodersJsonPath)
                : ctx.getAssets().open(encodersJsonPath);
        byte[] buf = new byte[is.available()];
        is.read(buf);
        JSONObject o = new JSONObject(new String(buf, StandardCharsets.UTF_8));

        K = o.getInt("K");
        NUM_POCKETS = o.getInt("num_pockets");
        speedMap  = toMap(o.getJSONObject("speedMap"));
        dirMap    = toMap(o.getJSONObject("dirMap"));
        dealerLabels     = toArray(o.getJSONArray("dealerLabels"));
        tableLabels      = toArray(o.getJSONArray("tableLabels"));
        materialLabels   = toArray(o.getJSONArray("materialLabels"));
        techniqueLabels  = toArray(o.getJSONArray("techniqueLabels"));
        dropSectorLabels = toArray(o.getJSONArray("dropSectorLabels"));
        contMean = toFloatArray(o.getJSONArray("contMean"));
        contStd  = toFloatArray(o.getJSONArray("contStd"));

        TOTAL_DIM = K * NUM_POCKETS
                + 1     // speed
                + 1     // direction
                + contMean.length
                + dealerLabels.length
                + tableLabels.length
                + materialLabels.length
                + techniqueLabels.length
                + dropSectorLabels.length;
    }

    public float[] extract(com.amitthakur.roulette.model.SpinRecord r) {
        float[] f = new float[TOTAL_DIM];
        int off = 0;

        // 1) Last K one-hot
        for (int i = 0; i < K; i++) {
            int pocket = r.getLastNumbers().get(i);
            f[off + i * NUM_POCKETS + pocket] = 1f;
        }
        off += K * NUM_POCKETS;

        // 2) Speed & direction
        f[off++] = speedMap.get(r.getSpeedCategory());
        f[off++] = dirMap.get(r.getDirection());

        // 3) Continuous normalized
        float[] cont = {
                r.getFrictionEstimate(),
                r.getVibrationLevel(),
                r.getBallInitRpm(),
                r.getWheelInitRpm(),
                r.getBallDecelRate(),
                r.getWheelDecelRate(),
                r.getReleaseAngleDeg(),
                r.getTimeToDropMs(),
                r.getRadiusTransition(),
                r.getTableTiltDeg()
        };
        for (int i = 0; i < cont.length; i++) {
            f[off + i] = (cont[i] - contMean[i]) / contStd[i];
        }
        off += contMean.length;

        // 4) Categorical one-hots
        oneHot(f, off, indexOf(dealerLabels, r.getDealerId()),    dealerLabels.length);
        off += dealerLabels.length;
        oneHot(f, off, indexOf(tableLabels, r.getTableId()),     tableLabels.length);
        off += tableLabels.length;
        oneHot(f, off, indexOf(materialLabels, r.getBallMaterial()), materialLabels.length);
        off += materialLabels.length;
        oneHot(f, off, indexOf(techniqueLabels, r.getSpinTechnique()), techniqueLabels.length);
        off += techniqueLabels.length;
        oneHot(f, off, indexOf(dropSectorLabels, r.getDropSector()),   dropSectorLabels.length);

        return f;
    }

    private void oneHot(float[] f, int off, int idx, int size) {
        if (idx >= 0) f[off + idx] = 1f;
    }

    private int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i].equals(v)) return i;
        return -1;
    }

    /*private Map<String,Integer> toMap(JSONObject o) throws Exception {
        Map<String,Integer> m = new HashMap<>();
        for (String k : JSONObject.getNames(o))
            m.put(k, o.getInt(k));
        return m;
    }
*/
    /**
     * Converts a JSONObject like {"LOW":0,"MEDIUM":1,"HIGH":2}
     * into a Map<String,Integer> by iterating over its keys().
     */
    private Map<String,Integer> toMap(JSONObject o) throws Exception {
        Map<String,Integer> m = new HashMap<>();
        Iterator<String> keys = o.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            m.put(k, o.getInt(k));
        }
        return m;
    }

    private String[] toArray(JSONArray a) throws Exception {
        String[] s = new String[a.length()];
        for (int i = 0; i < a.length(); i++)
            s[i] = a.getString(i);
        return s;
    }

    private float[] toFloatArray(JSONArray a) throws Exception {
        float[] f = new float[a.length()];
        for (int i = 0; i < a.length(); i++)
            f[i] = (float) a.getDouble(i);
        return f;
    }
}
