package orangutap.com.metronomesync;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Observable;
import java.util.Observer;

/**
 * Responsible for the metronome playback logistics. Supports vibration and
 * audio clicks.
 */
public class Metronome implements Observer {


    Vibrator mVibrator;
    private WorkerThread soundThread;
    private int silence = 0;
    private double[] silenceSoundArray;
    public final int tick = 1000;
    private boolean mIsRunning;
    private int mBeatsTicked;
    private int mSignature;
    private Context mContext;
    private int mMilliSecondsBetweenTicks;
    private MetronomeData mProperties;
    private Handler mHandler;
    private static Metronome instance;
    private SoundPool mSoundPool;
    private boolean mMediaReady;
    private int mSoundId;
    private AssetManager mAssets;
    private static final int HEADER_SIZE = 44;
    private InputStream clap;
    private int dataSize;
    private byte[] data;
    private int mTickLength;
    private AudioTrack metronomeBeep;
    private AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] soundTickArray;

    private Metronome(Context context, MetronomeData properties) {
        // Singleton constructor

        mIsRunning = false;
        mBeatsTicked = 0;
        mSignature = context.getResources().getInteger(R.integer.default_signature);
        mContext = context;
        mMediaReady = false;
        mProperties = properties;
        mTickLength = 100;
        soundTickArray = new double[mTickLength];
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        double[] bytesForSound = audioGenerator.getSineWave(mTickLength, 8000, 440);
        for(int i=0;i<mTickLength;i++) {
            soundTickArray[i] = bytesForSound[i];
        }
        audioGenerator.createPlayer();

        soundThread = new WorkerThread();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                mBeatsTicked++;
                tick();
            }
        };
    }

    public static byte[] readWavPcm(int dataSize, InputStream stream) throws IOException {
        byte[] data = new byte[dataSize];
        stream.read(data, 0, data.length);
        return data;
    }


    public AudioTrack writeAudio(AudioTrack at) {

        int i = 0;
        byte[] music = new byte[512];
        InputStream is = mContext.getResources().openRawResource(R.raw.clap);
        if(is == null) {
            System.err.println("clap was not read correctly");
        }

        try {
            if (is.skip(44) != 44) {
                System.err.println("Buffer did not read sound properly");
            }
        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        try{
            while((i = is.read(music)) != -1)
                at.write(music, 0, i);


        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
        return at;
    }


    void playSound() {
        audioGenerator.writeSound(soundTickArray);
        System.out.println("+++ PLAY ");

        //if audioTrack has been initialised, first, release any resources
        //then null it
        if (metronomeBeep != null) {
            metronomeBeep.release();
            metronomeBeep = null;
        }

        int buffSize = android.media.AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        metronomeBeep = new AudioTrack(AudioManager.STREAM_MUSIC,
                22050, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffSize ,AudioTrack.MODE_STREAM);
        writeAudio(metronomeBeep);
    }


    /**
     * Start metronome playback
     *
     * @param isVibrate set to true to enable vibration
     */
    public void play(boolean isVibrate) {
        Log.v("stdout", "url");

        Log.v("stdout", "urls");
        //start the ticking
        mIsRunning = true;
        mBeatsTicked = 0;
        updateTempo();
        tick();

    }

    /**
     * Stop the metronome playback
     */
    public void stop() {
        mIsRunning = false;
        mBeatsTicked = 0;
        mHandler.removeMessages(1);
    }

    /**
     * @return Playback properties
     */
    public MetronomeData getProperties() {
        return mProperties;
    }

    /**
     * Update properties for metronome playback
     * @param properties
     */
    public void setProperties(MetronomeData properties) {
        mProperties = properties;
        //TODO: check for updated property fields
    }

    /**
     * @return true if the metronome is currently active
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * TODO: Implement me
     * Updates all observers with the current BPM
     *
     * @param metronomeData metronome properties that were changed
     * @param obj will be ignored
     */
    public void update(Observable metronomeData, Object obj) {
        if(mIsRunning) {
            updateTempo();
        }
    }

    /**
     * Change the delay between ticks based on the current bpm
     */
    private void updateTempo() {
        mMilliSecondsBetweenTicks = 60000 / mProperties.getBPM();
    }

    /**
     * Handle one metronome beat
     */
    private void tick() {
        // if metronome is disabled, do nothing
        if (!mIsRunning) return;

        if (mBeatsTicked - mSignature == 0) {
            //first tick of the bar

            mBeatsTicked = 0;
            //mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
            mVibrator.vibrate(200);
//            audioGenerator.writeSound(soundTickArray);
            Log.v("stdout", "EY");

        } else {
            //tick in the middle of bar
            //mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
            mVibrator.vibrate(200);
//            audioGenerator.writeSound(soundTickArray);
            Log.v("stdout", "EY");
        }

        // schedule next tick
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), mMilliSecondsBetweenTicks);
    }

    /**
     * @return handler for scheduling metronome beats
     */
    public Handler getHandler() {
        return mHandler;
    }

    public static Metronome getInstance(Context context, MetronomeData data) {
        if (instance == null) {
            instance = new Metronome(context, data);
        }
        return instance;
    }

    public AssetManager getAssets() {
        return mAssets;
    }

    public Metronome() {
        audioGenerator.createPlayer();
    }

    public void playSounding() {
        mIsRunning = true;
        soundThread.running = true;
        soundThread.run();
    }

//    public void playSounding() {
//        // this initializes the sound for playing
//        mIsRunning = true;
//        silence = (int) (((60.0/ (float) mProperties.getBPM())*8000)-this.tick);
////        System.out.println(mProperties.getBPM());
////        System.out.println((60.0/ (float) mProperties.getBPM())*8000);
////        System.out.println(silence);
//        soundTickArray = new double[this.tick];
//        silenceSoundArray = new double[silence];
//        double[] soundArray = new double[this.tick + silence];
//        double[] tick = audioGenerator.getSineWave(this.tick, 8000, 100);
//        for(int i=0;i<this.tick;i++) {
//            soundArray[i] = tick[i];
//        }
//        for(int i=this.tick;i<silence;i++)
//        {
//            soundArray[i] = 0;
//        }
//        // this plays the sound
//        do {
//            audioGenerator.writeSound(soundArray);
//
//        } while(mIsRunning);
//    }

    public void stopSounding() {
        System.out.println("STOP");
        mIsRunning = false;
        soundThread.running = false;

        audioGenerator.destroyAudioTrack();
    }



    public class WorkerThread extends Thread {
        volatile boolean running = true;
        private int tick = 1000;

        public void run() {
            playSounding();
        }
        public void calcSilence()
        {
            silence = (int) (((60.0/(float) mProperties.getBPM())*8000)-this.tick);
            System.out.println((60.0/(float) mProperties.getBPM())*8000);
            soundTickArray = new double[this.tick];

            silenceSoundArray = new double[silence];
            double[] tick = audioGenerator.getSineWave(this.tick, 8000, 1000);
            for(int i=0;i<this.tick;i++) {
                soundTickArray[i] = tick[i];
            }
            for(int i=0;i<silence;i++)
                silenceSoundArray[i] = 0;
        }

        public void playSounding() {
            mIsRunning = true;
            calcSilence();
            do {
                audioGenerator.writeSound(soundTickArray);
                audioGenerator.writeSound(silenceSoundArray);
            } while(running);
        }

    }
}
