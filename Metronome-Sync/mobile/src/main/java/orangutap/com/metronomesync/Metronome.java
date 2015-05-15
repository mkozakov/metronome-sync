package orangutap.com.metronomesync;

import android.content.Context;
import android.media.MediaPlayer;
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

    private Vibrator vibrator;
    private boolean mIsRunning;
    private int beatsTicked;
    private int timeSignature;
    private Context mContext;
    private int mMilliSecondsBetweenTicks;
    private MetronomeData mProperties;
    private Handler mHandler;
    private static Metronome instance;


    private Metronome(Context context, MetronomeData properties) {
        // Singleton constructor
        mIsRunning = false;
        beatsTicked = 0;
        timeSignature = context.getResources().getInteger(R.integer.default_signature);
        mContext = context;
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mProperties = properties;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                beatsTicked++;
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
        beatsTicked = 0;
        updateTempo();
        tick();
    }

    /**
     * Stop the metronome playback
     */
    public void stop() {
        mIsRunning = false;
        beatsTicked = 0;
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

        if (beatsTicked - timeSignature == 0) {
            //first tick of the bar
            vibrator.vibrate(200);
            beatsTicked = 0;
        } else {
            //tick in the middle of bar
            vibrator.vibrate(100);
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
