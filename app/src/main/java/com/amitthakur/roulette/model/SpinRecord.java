package com.amitthakur.roulette.model;

import java.util.List;

public class SpinRecord {
    private String dealerId;
    private long timestamp;
    private List<Integer> lastNumbers;
    private String direction;
    private String speedCategory;
    private float frictionEstimate;
    private float vibrationLevel;

    private float ballInitRpm;
    private float wheelInitRpm;
    private float ballDecelRate;
    private float wheelDecelRate;
    private float releaseAngleDeg;
    private long timeToDropMs;

    private int bounceCount;
    private String dropSector;
    private float radiusTransition;

    private String tableId;
    private String ballMaterial;
    private float ambientTempC;
    private float ambientHumidity;
    private float tableTiltDeg;

    private float dealerHeightCm;
    private String spinTechnique;
    private int hourOfDay;
    private int dayOfWeek;

    private int audioSpikeCount;
    private float avgMagnetoRpm;
    private float avgGyroRpm;

    private int outcome;

    public SpinRecord() { }

    // full-arg ctor omitted for brevity…

    public String getDealerId() { return dealerId; }
    public void setDealerId(String dealerId) { this.dealerId = dealerId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<Integer> getLastNumbers() { return lastNumbers; }
    public void setLastNumbers(List<Integer> lastNumbers) { this.lastNumbers = lastNumbers; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getSpeedCategory() { return speedCategory; }
    public void setSpeedCategory(String speedCategory) { this.speedCategory = speedCategory; }

    public float getFrictionEstimate() { return frictionEstimate; }
    public void setFrictionEstimate(float frictionEstimate) { this.frictionEstimate = frictionEstimate; }

    public float getVibrationLevel() { return vibrationLevel; }
    public void setVibrationLevel(float vibrationLevel) { this.vibrationLevel = vibrationLevel; }

    public float getBallInitRpm() { return ballInitRpm; }
    public void setBallInitRpm(float ballInitRpm) { this.ballInitRpm = ballInitRpm; }

    public float getWheelInitRpm() { return wheelInitRpm; }
    public void setWheelInitRpm(float wheelInitRpm) { this.wheelInitRpm = wheelInitRpm; }

    public float getBallDecelRate() { return ballDecelRate; }
    public void setBallDecelRate(float ballDecelRate) { this.ballDecelRate = ballDecelRate; }

    public float getWheelDecelRate() { return wheelDecelRate; }
    public void setWheelDecelRate(float wheelDecelRate) { this.wheelDecelRate = wheelDecelRate; }

    public float getReleaseAngleDeg() { return releaseAngleDeg; }
    public void setReleaseAngleDeg(float releaseAngleDeg) { this.releaseAngleDeg = releaseAngleDeg; }

    public long getTimeToDropMs() { return timeToDropMs; }
    public void setTimeToDropMs(long timeToDropMs) { this.timeToDropMs = timeToDropMs; }

    public int getBounceCount() { return bounceCount; }
    public void setBounceCount(int bounceCount) { this.bounceCount = bounceCount; }

    public String getDropSector() { return dropSector; }
    public void setDropSector(String dropSector) { this.dropSector = dropSector; }

    public float getRadiusTransition() { return radiusTransition; }
    public void setRadiusTransition(float radiusTransition) { this.radiusTransition = radiusTransition; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getBallMaterial() { return ballMaterial; }
    public void setBallMaterial(String ballMaterial) { this.ballMaterial = ballMaterial; }

    public float getAmbientTempC() { return ambientTempC; }
    public void setAmbientTempC(float ambientTempC) { this.ambientTempC = ambientTempC; }

    public float getAmbientHumidity() { return ambientHumidity; }
    public void setAmbientHumidity(float ambientHumidity) { this.ambientHumidity = ambientHumidity; }

    public float getTableTiltDeg() { return tableTiltDeg; }
    public void setTableTiltDeg(float tableTiltDeg) { this.tableTiltDeg = tableTiltDeg; }

    public float getDealerHeightCm() { return dealerHeightCm; }
    public void setDealerHeightCm(float dealerHeightCm) { this.dealerHeightCm = dealerHeightCm; }

    public String getSpinTechnique() { return spinTechnique; }
    public void setSpinTechnique(String spinTechnique) { this.spinTechnique = spinTechnique; }

    public int getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(int hourOfDay) { this.hourOfDay = hourOfDay; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getAudioSpikeCount() { return audioSpikeCount; }
    public void setAudioSpikeCount(int audioSpikeCount) { this.audioSpikeCount = audioSpikeCount; }

    public float getAvgMagnetoRpm() { return avgMagnetoRpm; }
    public void setAvgMagnetoRpm(float avgMagnetoRpm) { this.avgMagnetoRpm = avgMagnetoRpm; }

    public float getAvgGyroRpm() { return avgGyroRpm; }
    public void setAvgGyroRpm(float avgGyroRpm) { this.avgGyroRpm = avgGyroRpm; }

    public int getOutcome() { return outcome; }
    public void setOutcome(int outcome) { this.outcome = outcome; }
}