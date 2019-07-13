/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guide.theta360.thetagyroscope;

import android.content.Context;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import guide.theta360.thetagyroscope.AccelerationSensor.AccelerationGraSensor;

import guide.theta360.thetagyroscope.sensors.OrientationSensor;
import guide.theta360.thetagyroscope.task.TakePictureTask;
import guide.theta360.thetagyroscope.task.TakePictureTask.Callback;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginlibrary.values.LedColor;
import com.theta360.pluginlibrary.values.LedTarget;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends PluginActivity {



    private TakePictureTask.Callback mTakePictureTaskCallback = new Callback() {
        @Override
        public void onTakePicture(String fileUrl) {

        }
    };


    // specific to sensor tutorial
    private SensorManager graSensorManager;
    private AccelerationGraSensor accelerationGraSensor;

    private SensorManager orientationSensorManager;
    private OrientationSensor orientationSensor;

    private static final int ACCELERATION_INTERVAL_PERIOD = 1000;
    private Timer timer;
    private static final float ACCELERATION_THRESHOLD = 3.0f;

    private static final float ORIENTATION_THRESHOLD = .2f;
    private static final float ORIENTATION_THRESHOLD_PITCH = .9f;
    private static final float ORIENTATION_THRESHOLD_ROLL = .2f;
    private static final float ORIENTATION_THRESHOLD_AZIMUTH = 1f;
    private static final String RECORDER_TAG = "Recorder";
    private static final String PLAYER_TAG = "Player";

    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String soundFilePath;

    int position=0;

    private boolean isEnded = false;
    private static final String TAG = "Plug-in::MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri bm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bm);
        Uri hd = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.hd);
        Uri lol = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lol);
        Uri trolo = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.trolo);
        Uri s1234 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.s1234);
        Uri o9000 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.o9000);
        Uri td = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.td);
        Uri kill = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kill);
        Uri ding = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ding);
        Uri sus = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sus);


        graSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerationGraSensor = new AccelerationGraSensor(graSensorManager);

        orientationSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        orientationSensor = new OrientationSensor(orientationSensorManager);

        // Set enable to close by pluginlibrary, If you set false, please call close() after finishing your end processing.
        setAutoClose(true);
        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {


            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {

                if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                    if (position==0 || position==1) {
                        startPlayer(hd);
                    }
                    else if (position==2 || position==3) {
                        startPlayer(trolo);
                    }
                    else if (position==4 || position==5) {
                        startPlayer(ding);

                    }
                }
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    if (position==0 || position==1) {
                        startPlayer(lol);
                    }
                    else if (position==2 || position==3) {
                        startPlayer(o9000);

                    }
                    else if (position==4 || position==5) {
                        startPlayer(kill);
                    }

                }
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) {
                    if (position==0 || position==1) {
                        startPlayer(td);
                    }
                    else if (position==2 || position==3) {
                        startPlayer(bm);

                    }
                    else if (position==4 || position==5) {
                        startPlayer(sus);

                    }
                }
            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {
                notificationLedBlink(LedTarget.LED3, LedColor.WHITE, 1000);
            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    Log.d(TAG, "Do end process.");
                    closeCamera();
                }
                if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                    new TakePictureTask(mTakePictureTaskCallback).execute();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {


                float current_azimuth = orientationSensor.getOrientation()[0];
                float current_pitch = orientationSensor.getOrientation()[1];
                float current_roll = orientationSensor.getOrientation()[2];

//                Log.d("ORIENTATION", "Azimuth: " + String.valueOf(current_azimuth));
//                Log.d("ORIENTATION", "Pitch: " + String.valueOf(current_pitch));
//                Log.d("ORIENTATION", "Roll: " + String.valueOf(current_roll));

                if(current_azimuth>0 && current_pitch<0 && current_roll<0)
                {
                    position=0;
                }
                else if(current_azimuth<0 && current_pitch<0 && current_roll>0)
                {
                    position=1;
                }
                else if(current_azimuth<0 && current_pitch>0 && current_roll>0)
                {
                    position=2;
                }
                else if(current_azimuth>0 && current_pitch>0 && current_roll<0)
                {
                    position=3;
                }
                else if(current_azimuth<0 && current_pitch>0 && current_roll<0)
                {
                    position=4;
                }
                else if(current_azimuth<0 && current_pitch<0 && current_roll<0)
                {
                    position=5;
                }

            }
        }, 0, ACCELERATION_INTERVAL_PERIOD);
    }

    @Override
    protected void onPause() {
        // Do end processing
        //close();

        super.onPause();
        timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (graSensorManager != null) {
            // イベントリスナーの解除
            graSensorManager.unregisterListener(accelerationGraSensor);
        }
    }

    private void pnote(Uri notes, int msec)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startPlayer(notes);
            }
        }, msec);

    }

    private void startRecorder() {
        new MediaRecorderPrepareTask().execute();
    }

    private void stopRecorder() {
        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.d(RECORDER_TAG, "RuntimeException: stop() is called immediately after start()");
            deleteSoundFile();
        } finally {
            isRecording = false;
            releaseMediaRecorder();
        }
        Log.d(RECORDER_TAG, "Stop");
    }

    private void startPlayer(Uri media) {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING); // 2019/1/21追記
        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVol, 0); // 2019/1/21追記
        MediaPlayer mediaPlayer = new MediaPlayer();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_RING) // 2019/1/21追記
                .build();
        try {
            mediaPlayer.setAudioAttributes(attributes);
            mediaPlayer.setDataSource(getApplicationContext(), media);
            mediaPlayer.setVolume(1.0f, 1.0f); // 7 Max volume
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepare();
            Log.d(PLAYER_TAG, "Start");
        } catch (Exception e) {
            Log.e(RECORDER_TAG, "Exception starting MediaPlayer: " + e.getMessage());
            mediaPlayer.release();
            notificationError("");
        }
    }

    private boolean prepareMediaRecorder() {
        Log.d(RECORDER_TAG, soundFilePath);
        deleteSoundFile();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setParameters("RicUseBFormat=false");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setAudioSamplingRate(44100); // 2019/1/21追記
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(soundFilePath);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            Log.e(RECORDER_TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void deleteSoundFile() {
        File file = new File(soundFilePath);
        if (file.exists()) {
            file.delete();
        }
        file = null;
    }

    private class MediaRecorderPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (prepareMediaRecorder()) {
                mediaRecorder.start();
                isRecording = true;
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.e(RECORDER_TAG, "MediaRecorder prepare failed");
                notificationError("");
                return;
            }
            Log.d(RECORDER_TAG, "Start");
        }
    }
    private void closeCamera() {
        if (isEnded) {
            return;
        }
    }
}
