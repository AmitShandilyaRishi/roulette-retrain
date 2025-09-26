package com.amitthakur.roulette.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import com.amitthakur.roulette.model.SpinRecord;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Wraps TensorFlow Lite Interpreter for roulette prediction.
 */
public class InferenceEngine {
    private final Interpreter tflite;
    private final FeatureExtractor extractor;

    /**
     * @param modelPath  Path in assets ("model.tflite") or absolute file.
     * @param encPath    Path in assets ("encoders.json") or absolute file.
     */
    public InferenceEngine(String modelPath, String encPath, Context ctx) throws Exception {
        MappedByteBuffer modelBuf = modelPath.startsWith("/")
                ? mapFile(modelPath)
                : mapAsset(modelPath, ctx);
        tflite = new Interpreter(modelBuf, new Interpreter.Options()
                .setNumThreads(4)                            // parallel threads
                .setUseNNAPI(false)                          // disable NNAPI
        );
        extractor = new FeatureExtractor(encPath, ctx);
    }

    private MappedByteBuffer mapAsset(String asset, Context ctx) throws Exception {
        AssetFileDescriptor fd = ctx.getAssets().openFd(asset);
        try (FileInputStream fis = new FileInputStream(fd.getFileDescriptor())) {
            return fis.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY,
                            fd.getStartOffset(),
                            fd.getDeclaredLength());
        }
    }

    private MappedByteBuffer mapFile(String path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path)) {
            FileChannel ch = fis.getChannel();
            return ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        }
    }

    /**
     * Returns predicted pocket index (0–36).
     */
    public int predict(SpinRecord record) {
        float[] input = extractor.extract(record);
        float[][] output = new float[1][extractor.NUM_POCKETS];
        tflite.run(input, output);

        // Argmax
        int best = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < extractor.NUM_POCKETS; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                best = i;
            }
        }
        return best;
    }
}


