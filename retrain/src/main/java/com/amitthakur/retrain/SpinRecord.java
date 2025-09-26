package com.amitthakur.retrain;

import java.util.List;

public class SpinRecord {
    // 1. Basic
    public String dealerId;
    public long timestamp;
    public List<Integer> lastNumbers;  // size = 5
    public String direction;
    public String speedCategory;
    public float frictionEstimate;
    public float vibrationLevel;

    // 2. Ball–Wheel Kinematics
    public float ballInitRpm;
    public float wheelInitRpm;
    public float ballDecelRate;
    public float wheelDecelRate;
    public float releaseAngleDeg;
    public long timeToDropMs;

    // 3. Bounce & Drop Dynamics
    public int bounceCount;
    public String dropSector;
    public float radiusTransition;

    // 4. Table & Environment
    public String tableId;
    public String ballMaterial;
    public float ambientTempC;
    public float ambientHumidity;
    public float tableTiltDeg;

    // 5. Dealer & Operational
    public float dealerHeightCm;
    public String spinTechnique;
    public int hourOfDay;
    public int dayOfWeek;

    // 6. Sensor-Fusion
    public int audioSpikeCount;
    public float avgMagnetoRpm;
    public float avgGyroRpm;

    // 7. Label for training
    public int outcome;

    public SpinRecord() { }

    public static String csvHeader() {
        return String.join(",",
                "dealerId","timestamp","last0","last1","last2","last3","last4",
                "direction","speedCategory","frictionEstimate","vibrationLevel",
                "ballInitRpm","wheelInitRpm","ballDecelRate","wheelDecelRate",
                "releaseAngleDeg","timeToDropMs",
                "bounceCount","dropSector","radiusTransition",
                "tableId","ballMaterial","ambientTempC","ambientHumidity","tableTiltDeg",
                "dealerHeightCm","spinTechnique","hourOfDay","dayOfWeek",
                "audioSpikeCount","avgMagnetoRpm","avgGyroRpm",
                "outcome"
        );
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(dealerId).append(',').append(timestamp);
        for (int n : lastNumbers) sb.append(',').append(n);
        sb.append(',').append(direction)
                .append(',').append(speedCategory)
                .append(',').append(frictionEstimate)
                .append(',').append(vibrationLevel)
                .append(',').append(ballInitRpm)
                .append(',').append(wheelInitRpm)
                .append(',').append(ballDecelRate)
                .append(',').append(wheelDecelRate)
                .append(',').append(releaseAngleDeg)
                .append(',').append(timeToDropMs)
                .append(',').append(bounceCount)
                .append(',').append(dropSector)
                .append(',').append(radiusTransition)
                .append(',').append(tableId)
                .append(',').append(ballMaterial)
                .append(',').append(ambientTempC)
                .append(',').append(ambientHumidity)
                .append(',').append(tableTiltDeg)
                .append(',').append(dealerHeightCm)
                .append(',').append(spinTechnique)
                .append(',').append(hourOfDay)
                .append(',').append(dayOfWeek)
                .append(',').append(audioSpikeCount)
                .append(',').append(avgMagnetoRpm)
                .append(',').append(avgGyroRpm)
                .append(',').append(outcome);
        return sb.toString();
    }

    public static SpinRecord fromCsv(String line) {
        String[] c = line.split(",");
        SpinRecord r = new SpinRecord();
        int i = 0;
        r.dealerId = c[i++];
        r.timestamp = Long.parseLong(c[i++]);
        r.lastNumbers = List.of(
                Integer.parseInt(c[i++]),
                Integer.parseInt(c[i++]),
                Integer.parseInt(c[i++]),
                Integer.parseInt(c[i++]),
                Integer.parseInt(c[i++])
        );
        r.direction = c[i++];
        r.speedCategory = c[i++];
        r.frictionEstimate = Float.parseFloat(c[i++]);
        r.vibrationLevel   = Float.parseFloat(c[i++]);
        r.ballInitRpm      = Float.parseFloat(c[i++]);
        r.wheelInitRpm     = Float.parseFloat(c[i++]);
        r.ballDecelRate    = Float.parseFloat(c[i++]);
        r.wheelDecelRate   = Float.parseFloat(c[i++]);
        r.releaseAngleDeg  = Float.parseFloat(c[i++]);
        r.timeToDropMs     = Long.parseLong(c[i++]);
        r.bounceCount      = Integer.parseInt(c[i++]);
        r.dropSector       = c[i++];
        r.radiusTransition = Float.parseFloat(c[i++]);
        r.tableId          = c[i++];
        r.ballMaterial     = c[i++];
        r.ambientTempC     = Float.parseFloat(c[i++]);
        r.ambientHumidity  = Float.parseFloat(c[i++]);
        r.tableTiltDeg     = Float.parseFloat(c[i++]);
        r.dealerHeightCm   = Float.parseFloat(c[i++]);
        r.spinTechnique    = c[i++];
        r.hourOfDay        = Integer.parseInt(c[i++]);
        r.dayOfWeek        = Integer.parseInt(c[i++]);
        r.audioSpikeCount  = Integer.parseInt(c[i++]);
        r.avgMagnetoRpm    = Float.parseFloat(c[i++]);
        r.avgGyroRpm       = Float.parseFloat(c[i++]);
        r.outcome          = Integer.parseInt(c[i]);
        return r;
    }

    public String uniqueKey() {
        return dealerId + "-" + timestamp;
    }
}
