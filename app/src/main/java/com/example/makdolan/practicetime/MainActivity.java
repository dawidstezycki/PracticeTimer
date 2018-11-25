package com.example.makdolan.practicetime;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    int MILLIS_IN_MINUTE = 1000 * 60;
    int tempo;
    int mMinutes;
    int mSeconds;

    SeekBar mSeekbar;
    TextView mTempo;
    MediaPlayer mp;
    Button playMetronome;
    boolean metronomePlaying = false;
    boolean countdownCounting = false;
    TextView CountDownView;
    Button startCountDown;

    ScheduledThreadPoolExecutor mainExecutor;
    ScheduledFuture<?> t1;
    ScheduledFuture<?> t2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainExecutor = new ScheduledThreadPoolExecutor(2);

        tempo = 120;
        mTempo = (TextView) findViewById(R.id.tempo);
        mTempo.setText(""+tempo);

        mMinutes = 0;
        mSeconds = 0;
        CountDownView = (TextView) findViewById(R.id.countdown);

        startCountDown = (Button) findViewById(R.id.startcountdown);
        startCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countdownCounting){
                    if (t2 != null){
                        t2.cancel(false);
                    }
                    startCountDown.setText("START");
                    countdownCounting = false;
                } else if (mMinutes > 0 || mSeconds > 0){
                    startCounting();
                    startCountDown.setText("STOP");
                }

            }
        });

        mSeekbar = (SeekBar) findViewById(R.id.temposlide);
        mSeekbar.setProgress(tempo);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                tempo=seekBar.getProgress();
                mTempo.setText(""+tempo);
                if (metronomePlaying){
                    startMetronome();
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        playMetronome = (Button) findViewById(R.id.playmetro);
        playMetronome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (metronomePlaying){
                    if (t1 != null){
                        t1.cancel(false);
                    }
                    playMetronome.setText("PLAY");
                    metronomePlaying = false;
                } else{
                    startMetronome();
                    playMetronome.setText("STOP");
                    metronomePlaying = true;
                }
            }
        });
    }

    public void startCounting(){
        if (t2 != null){
            t2.cancel(false);
        }

        t2 = mainExecutor.scheduleAtFixedRate(new CountdownTask(), 0, 1, TimeUnit.SECONDS);
    }

    public void resetCountdown(View view){
        if (!countdownCounting) {
            mMinutes = 0;
            mSeconds = 0;
            CountDownView.setText(""+String.format("%d:%02d", mMinutes, mSeconds));
        }

    }

    public void changeCountdown(View view){
        if (!countdownCounting) {
            switch (view.getId()) {
                case R.id.addtime1:
                    if (mMinutes + 1 <= 99) {
                        mMinutes += 1;
                    } else {
                        mMinutes = 99;
                    }
                    break;
                case R.id.subtime1:
                    if (mSeconds > 0){
                        mSeconds = 0;
                    } else {
                        if (mMinutes - 1 >= 0) {
                            mMinutes -= 1;
                        } else {
                            mMinutes = 0;
                        }
                    }
            }
            mSeconds = 0;
            CountDownView.setText(""+String.format("%d:%02d", mMinutes, mSeconds));
        }
    }

    public void startMetronome() {
        if (t1 != null){
            t1.cancel(false);
        }

        if (tempo > 0){
            t1 = mainExecutor.scheduleAtFixedRate(new MetronomeTask(), 0, MILLIS_IN_MINUTE / tempo, TimeUnit.MILLISECONDS);
        }


    }

    public void changeTempo(View view){
        switch (view.getId()){
            case R.id.addtempo1:
                if (tempo + 1 <= 300){
                    tempo += 1;
                }else{
                    tempo = 300;
                }
                break;
            case R.id.addtempo5:
                if (tempo + 5 <= 300) {
                    tempo += 5;
                }else{
                    tempo = 300;
                }
                break;
            case R.id.subtempo1:
                if (tempo - 1 >= 0) {
                    tempo -= 1;
                }else{
                    tempo = 0;
                }

                break;
            case R.id.subtempo5:
                if (tempo - 5 >= 0) {
                    tempo -= 5;
                } else{
                    tempo = 0;
                }
                break;
        }
        mTempo.setText(""+ tempo);
        mSeekbar.setProgress(tempo);
        if (metronomePlaying){
            startMetronome();
        }
    }

    class MetronomeTask implements Runnable {

        @Override
        public void run() {
            playSound();
        }
    }

    private void playSound() {
        mp = MediaPlayer.create(this, R.raw.metronome);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            };
        });
    }

    class CountdownTask implements Runnable {

        @Override
        public void run() {
            countdownCounting = true;
            countdown();

            if (mSeconds == 0 && mMinutes == 0) {
                startCountDown.setText("START");
                countdownCounting = false;
                t2.cancel(false);
            }
        }
    }

    private void countdown(){
        if (mSeconds == 0){
            mMinutes -= 1;
            mSeconds = 59;
        } else {
            mSeconds -= 1;
        }
        CountDownView.setText(""+String.format("%d:%02d", mMinutes, mSeconds));
    }
}
