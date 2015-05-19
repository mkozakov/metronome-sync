package orangutap.com.metronomesync;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import java.util.Observable;
import java.util.Observer;

/**
 * Responsible for the metronome playback logistics. Supports vibration and
 * audio clicks.
 */
public class Metronome implements Observer {


    private Vibrator mVibrator;         /* This controls the vibrating service */
    private boolean mIsRunning;         /* A boolean that says whether
                                        the metronome is vibrating */
    private int mBeatsTicked;           /* Represents the current beat in the bar
                                           of the time signature */
    private int mTimeSignature;         /* The time signature */
    private Context mContext;           /* The context the class is currently in */
    private int mMilliSecondsBetweenTicks; /* milliseconds between each tick determined
                                              by the bpm */
    private MetronomeData mProperties;  /* The current settings of the metronome */
    private Handler mHandler;           /* A handler class to handle the async vibrating */
    private static Metronome instance;  /* Singleton Metronome instance */

    /**
     * Singleton class that controls all the backend activities for the
     * metronome
     * @param context the context the class is currently in
     * @param properties the settings of the metronome
     */
    private Metronome(Context context, MetronomeData properties) {
        // Singleton constructor
        // Initialization of private variables
        mIsRunning = false;
        mBeatsTicked = 0;
        mTimeSignature = context.getResources().getInteger(R.integer.default_signature);
        this.mContext = context;
        mVibrator = (Vibrator) this.mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mProperties = properties;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                mBeatsTicked++;
                tick();
            }
        };
    }

    /**
     * Start metronome playback
     *
     * @param isVibrate set to true to enable vibration
     */
    public void play(boolean isVibrate) {
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

        if (mBeatsTicked - mTimeSignature == 0) {
            //first tick of the bar

            mVibrator.vibrate(100);
            mBeatsTicked = 0;
        } else {
            //tick in the middle of bar
            mVibrator.vibrate(100);
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
}
