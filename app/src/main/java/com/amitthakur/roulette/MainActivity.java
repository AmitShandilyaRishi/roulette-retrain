package com.amitthakur.roulette;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amitthakur.roulette.databinding.ActivityMainBinding;
import com.amitthakur.roulette.ml.InferenceEngine;
import com.amitthakur.roulette.ml.ModelManager;
import com.amitthakur.roulette.model.SpinRecord;
import com.amitthakur.roulette.ui.SpinAdapter;
import com.amitthakur.roulette.ui.SpinViewModel;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;
    private SpinViewModel vm;
    private final SpinAdapter adapter = new SpinAdapter();

    // Sensors & audio
    private SensorManager sm;
    private SensorEventListener sel;
    private AudioRecord ar;
    private boolean recording;
    private int audioSpikes;
    private List<Float> gyroSamples = new ArrayList<>();
    private List<Float> magSamples = new ArrayList<>();
    private long[] spinTimes = new long[2];

    // History
    private final Deque<Integer> lastQueue = new ArrayDeque<>(5);

    // Inference
    private InferenceEngine engine;

    // Permission launcher
    private ActivityResultLauncher<String> audioPermLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Obtain ViewModel via Hilt
        vm = new ViewModelProvider(this).get(SpinViewModel.class);

        // 2) Inflate & bind layout
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);
        b.setVm(vm);
        b.setLifecycleOwner(this);

        // 3) RecyclerView setup
        b.spinList.setLayoutManager(new LinearLayoutManager(this));
        b.spinList.setAdapter(adapter);

        // 4) Observe recent spins, build lastQueue
        vm.recentSpins.observe(this, list -> {
            adapter.submitList(list);
            lastQueue.clear();
            for (int i = 0; i < Math.min(5, list.size()); i++) {
                lastQueue.add(list.get(i).getOutcome());
            }
        });

        // 5) Prepare AUDIO permission launcher
        audioPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        Toast.makeText(this,
                                "Audio permission needed for bounce detection",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }

        // 6) SensorManager + listener
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        setupSensorListener();

        // 7) AudioRecord (reusable for each spin start)
        int sr = 8000;
        int bs = AudioRecord.getMinBufferSize(sr,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sr, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bs);

        // 8) Download latest model & encoders
        new ModelManager(this).fetchLatest(new ModelManager.Listener() {
            @Override
            public void onReady(String modelPath, String encPath) {
                runOnUiThread(() -> initEngine(modelPath, encPath));
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        initEngine("roulette_model.tflite","encoders.json"));
            }
        });

        // 9) Mark spin start
        b.btnStart.setOnClickListener(v -> {
            spinTimes[0] = SystemClock.elapsedRealtime();
            audioSpikes = 0; gyroSamples.clear(); magSamples.clear();
            recording = true; ar.startRecording();
            new Thread(() -> {
                short[] buf = new short[bs];
                while (recording) {
                    int r = ar.read(buf, 0, bs);
                    for (int i = 0; i < r; i++) {
                        if (Math.abs(buf[i]) > 2000) {
                            audioSpikes++;
                            break;
                        }
                    }
                }
            }).start();
            Toast.makeText(this,"Spin start marked",Toast.LENGTH_SHORT).show();
        });

        // 10) Mark ball settle
        b.btnSettle.setOnClickListener(v -> {
            spinTimes[1] = SystemClock.elapsedRealtime();
            recording = false; ar.stop();
            Toast.makeText(this,"Ball settled",Toast.LENGTH_SHORT).show();
        });

        // 11) Submit & predict
        b.submitBtn.setOnClickListener(v -> {
            try{
                if (engine == null) {
                    Toast.makeText(this, "Model not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
                    return;
                }
                SpinRecord r = collectRecord();
                int pred = engine.predict(r);
                Toast.makeText(this,"Predicted pocket: "+pred,Toast.LENGTH_LONG).show();
                vm.submitSpin(r);
            } catch(Exception e){
                e.printStackTrace();
                Toast.makeText(this,"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void setupSensorListener() {
        sel = new SensorEventListener() {
            long lastMagT = 0;
            float lastMagZ = 0;

            @Override
            public void onSensorChanged(SensorEvent e) {
                switch (e.sensor.getType()) {
                    case Sensor.TYPE_GYROSCOPE:
                        gyroSamples.add((float)(e.values[2]*60/(2*Math.PI)));
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        float mz = e.values[2];
                        long now = SystemClock.elapsedRealtime();
                        if (lastMagT>0 && Math.abs(mz-lastMagZ)>5f) {
                            magSamples.add(60f/((now-lastMagT)/1000f));
                            lastMagT = now;
                        } else if (lastMagT==0) {
                            lastMagT = now;
                        }
                        lastMagZ = mz;
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        float x=e.values[0],y=e.values[1],z=e.values[2];
                        double tilt=Math.acos(z/Math.sqrt(x*x+y*y+z*z))*180/Math.PI;
                        b.tvTilt.setText(String.format("Tilt: %.1f°",tilt));
                        break;
                }
            }
            @Override public void onAccuracyChanged(Sensor s,int i){}
        };
        sm.registerListener(sel,
                sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(sel,
                sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(sel,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    private void initEngine(String modelPath, String encPath) {
        try {
            engine = new InferenceEngine(modelPath, encPath, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SpinRecord collectRecord() {
        List<Integer> last = new ArrayList<>(lastQueue);
        while (last.size() < 5) last.add(0);

        SpinRecord r = new SpinRecord();
        r.setDealerId(b.dealerInput.getText().toString());
        r.setTimestamp(System.currentTimeMillis());
        r.setLastNumbers(last);
        r.setDirection(b.spinnerDirection.getSelectedItem().toString());
        r.setSpeedCategory(b.spinnerSpeed.getSelectedItem().toString());
        r.setFrictionEstimate(
                Float.parseFloat(b.frictionInput.getText().toString()));
        r.setVibrationLevel(
                Float.parseFloat(b.vibrationInput.getText().toString()));
        r.setBallInitRpm(avg(gyroSamples));
        r.setWheelInitRpm(avg(magSamples));
        r.setBallDecelRate(computeDecel(gyroSamples));
        r.setWheelDecelRate(computeDecel(magSamples));
        r.setReleaseAngleDeg(b.seekReleaseAngle.getProgress());
        r.setTimeToDropMs(spinTimes[1]-spinTimes[0]);
        r.setBounceCount(audioSpikes);
        r.setDropSector(determineSector(last.get(4)));
        r.setRadiusTransition(computeRadiusTransition(gyroSamples));
        r.setTableId(b.spinnerTableId.getSelectedItem().toString());
        r.setBallMaterial(b.spinnerBallMaterial.getSelectedItem().toString());
        r.setAmbientTempC(
                Float.parseFloat(b.tempInput.getText().toString()));
        r.setAmbientHumidity(
                Float.parseFloat(b.humidityInput.getText().toString()));
        r.setTableTiltDeg(parseTilt(b.tvTilt.getText().toString()));
        r.setDealerHeightCm(
                Float.parseFloat(b.dealerHeightInput.getText().toString()));
        r.setSpinTechnique(
                b.spinnerSpinTech.getSelectedItem().toString());
        LocalDateTime now = LocalDateTime.now();
        r.setHourOfDay(now.getHour());
        r.setDayOfWeek(now.getDayOfWeek().getValue());
        r.setAudioSpikeCount(audioSpikes);
        r.setAvgMagnetoRpm(avg(magSamples));
        r.setAvgGyroRpm(avg(gyroSamples));
        // For training labels only; set an actual outcome later
        r.setOutcome(last.get(0));
        return r;
    }

    private float avg(List<Float> L) {
        return L.isEmpty() ? 0f : L.stream().reduce(0f, Float::sum)/L.size();
    }

    private float computeDecel(List<Float> L) {
        if (L.size()<2) return 0f;
        return (L.get(0)-L.get(L.size()-1)) / (L.size()*(1/60f));
    }

    private String determineSector(int p) {
        if (p <= 4)  return "0–4";
        if (p <= 9)  return "5–9";
        if (p <= 14) return "10–14";
        if (p <= 19) return "15–19";
        if (p <= 24) return "20–24";
        if (p <= 29) return "25–29";
        if (p <= 34) return "30–34";
        return "35–36";
    }

    private float computeRadiusTransition(List<Float> samples) {
        if (samples.isEmpty()) return 0f;
        float max = samples.stream().reduce(Float::max).orElse(0f);
        float min = samples.stream().reduce(Float::min).orElse(0f);
        return (max-min)/(max+0.0001f);
    }

    private float parseTilt(String txt) {
        try {
            return Float.parseFloat(txt.replace("Tilt: ","").replace("°",""));
        } catch (Exception e) {
            return 0f;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(sel);
    }
}
