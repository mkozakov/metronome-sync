package orangutap.com.metronomesync;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

/**
 * Responsible for the metronome playback logistics. Supports vibration and
 * audio clicks.
 */
public class Metronome {

    private boolean mIsRunning;
    private int mBeatsTicked;
    private int mSignature;
    private MediaPlayer mediaPlayer;
    private Context mContext;
    private int mMilliSecondsBetweenTicks;
    private MetronomeData mProperties;
    private Handler mHandler;
    private static Metronome instance;


    private Metronome(Context context, MetronomeData properties) {
        // Singleton constructor
        mIsRunning = false;
        mBeatsTicked = 0;
        mSignature = context.getResources().getInteger(R.integer.signature);
        mContext = context;
        mediaPlayer = new MediaPlayer();
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
        //TODO: get bpm from mProperties
        int bpm = 120;

        //start the ticking
        mIsRunning = true;
        mBeatsTicked = 0;
        mediaPlayer.create(mContext, R.raw.clap);
        mMilliSecondsBetweenTicks = 60000 / bpm;
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
     * Handle one metronome beat
     */
    private void tick() {
        // if metronome is disabled, do nothing
        if (!mIsRunning) return;

        if (mBeatsTicked - mSignature == 0) {
            //first tick of the bar

            mBeatsTicked = 0;
            mediaPlayer.start();
        } else {
            //tick in the middle of bar

            mediaPlayer.start();
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

    public Metronome getInstance(Context context, MetronomeData data) {
        if (instance == null) {
            instance = new Metronome(context, data);
        }
        return instance;
    }
}
