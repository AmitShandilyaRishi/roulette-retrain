package com.amitthakur.retrain;

import java.util.*;
import java.util.stream.*;

public class Encoder {
    private static final int K = 5, NUM_POCKETS = 37;
    public Map<String,Integer> speedMap = Map.of("LOW",0,"MEDIUM",1,"HIGH",2);
    public Map<String,Integer> dirMap   = Map.of("LEFT",0,"RIGHT",1);
    public List<String> dealerLabels;
    public List<String> tableLabels;
    public List<String> materialLabels;
    public List<String> techniqueLabels;
    public List<String> dropSectorLabels;
    public double[] contMean, contStd;

    public static Encoder fit(List<SpinRecord> data) {
        Encoder e = new Encoder();
        e.dealerLabels     = distinctSorted(data, r->r.dealerId);
        e.tableLabels      = distinctSorted(data, r->r.tableId);
        e.materialLabels   = distinctSorted(data, r->r.ballMaterial);
        e.techniqueLabels  = distinctSorted(data, r->r.spinTechnique);
        e.dropSectorLabels = distinctSorted(data, r->r.dropSector);

        double[] fr = data.stream().mapToDouble(r->r.frictionEstimate).toArray();
        double[] vb = data.stream().mapToDouble(r->r.vibrationLevel).toArray();
        double[] bi = data.stream().mapToDouble(r->r.ballInitRpm).toArray();
        double[] wi = data.stream().mapToDouble(r->r.wheelInitRpm).toArray();
        double[] bd = data.stream().mapToDouble(r->r.ballDecelRate).toArray();
        double[] wd = data.stream().mapToDouble(r->r.wheelDecelRate).toArray();
        double[] ra = data.stream().mapToDouble(r->r.releaseAngleDeg).toArray();
        double[] td = data.stream().mapToDouble(r->r.timeToDropMs).toArray();
        double[] rt = data.stream().mapToDouble(r->r.radiusTransition).toArray();
        double[] tt = data.stream().mapToDouble(r->r.tableTiltDeg).toArray();

        e.contMean = new double[]{
                mean(fr), mean(vb), mean(bi), mean(wi),
                mean(bd), mean(wd), mean(ra), mean(td),
                mean(rt), mean(tt)
        };
        e.contStd = new double[]{
                std(fr), std(vb), std(bi), std(wi),
                std(bd), std(wd), std(ra), std(td),
                std(rt), std(tt)
        };
        return e;
    }

    private static <T> List<T> distinctSorted(List<SpinRecord> data,
                                              java.util.function.Function<SpinRecord,T> f) {
        return data.stream().map(f).distinct().sorted().collect(Collectors.toList());
    }

    private static double mean(double[] a) {
        return Arrays.stream(a).average().orElse(0);
    }
    private static double std(double[] a) {
        double m = mean(a);
        return Math.sqrt(Arrays.stream(a).map(x->(x - m)*(x - m)).average().orElse(0));
    }

    public int featureDim() {
        return K * NUM_POCKETS
                + 1                                 // speed
                + 1                                 // direction
                + contMean.length
                + dealerLabels.size()
                + tableLabels.size()
                + materialLabels.size()
                + techniqueLabels.size()
                + dropSectorLabels.size();
    }

    public double[] extractFeatures(SpinRecord r) {
        double[] f = new double[featureDim()];
        int off = 0;

        for (int i = 0; i < K; i++) {
            f[off + i*NUM_POCKETS + r.lastNumbers.get(i)] = 1.0;
        }
        off += K * NUM_POCKETS;

        f[off++] = speedMap.get(r.speedCategory);
        f[off++] = dirMap.get(r.direction);

        double[] cont = {
                r.frictionEstimate, r.vibrationLevel, r.ballInitRpm,
                r.wheelInitRpm, r.ballDecelRate, r.wheelDecelRate,
                r.releaseAngleDeg, r.timeToDropMs,
                r.radiusTransition, r.tableTiltDeg
        };
        for (int i = 0; i < cont.length; i++) {
            f[off + i] = (cont[i] - contMean[i]) / contStd[i];
        }
        off += contMean.length;

        oneHot(f, off, dealerLabels.indexOf(r.dealerId), dealerLabels.size());
        off += dealerLabels.size();
        oneHot(f, off, tableLabels.indexOf(r.tableId), tableLabels.size());
        off += tableLabels.size();
        oneHot(f, off, materialLabels.indexOf(r.ballMaterial), materialLabels.size());
        off += materialLabels.size();
        oneHot(f, off, techniqueLabels.indexOf(r.spinTechnique), techniqueLabels.size());
        off += techniqueLabels.size();
        oneHot(f, off, dropSectorLabels.indexOf(r.dropSector), dropSectorLabels.size());

        return f;
    }

    private void oneHot(double[] f, int off, int idx, int size) {
        if (idx >= 0) f[off + idx] = 1.0;
    }
}
