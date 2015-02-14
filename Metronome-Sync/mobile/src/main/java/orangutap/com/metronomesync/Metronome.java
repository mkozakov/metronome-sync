package orangutap.com.metronomesync;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;


public class Metronome {

    private boolean mIsRunning = false;
    private int mBeatsTicked;
    private int mBeatsInBar = getMPeriod4;
    private MediaPlayer mediaPlayer;
    private Context mContext;
    private Vibrator mVibrator;
    private View mBackground;
    private int mMilliSecondsBetweenTicks;
    private static Metronome instance;



    private Metronome(Context context, Vibrator vibrator, View view) {
        // Singleton constructor
        mContext = context;
        mVibrator = vibrator;
        mBackground = view;
        mBeatsTicked = 0;
        mediaPlayer = new MediaPlayer();
    }

    public Metronome getInstance(Context context, Vibrator vibrator, View view) {
        if(instance == null) {
            instance = new Metronome(context, vibrator, view);
        }
        return instance;
    }

    public void startTick(int mBpm) {
        //start the ticking
        mIsRunning = true;
        mBeatsTicked = 0;
        mediaPlayer.create(mContext, R.raw.clap);

        mMilliSecondsBetweenTicks = 60000 / mBpm;

        tick();

    }

    public void tick() {
        //stop the ticking if stop button was pressed
        if(!mIsRunning) return;
        if(mBeatsTicked - mBeatsInBar == 0) {
            mBeatsTicked = 0;

            mediaPlayer.start();
            // DO ACTION when ran all times in the bar
        }
        else {
            mediaPlayer.start();
            // DO ACTION normal case

        }

        // calls handler to tick with a delay, works recursively
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), mMilliSecondsBetweenTicks);
    }

    public void stopTick() {
        //called when stop button is pressed to stop tick
        mIsRunning = false;
        mBeatsTicked = 0;

        mHandler.removeMessage(1);
    }

    private Handler mHandler = new Handler() {
        // I think this method must be called handleMessage
        // It is responsible for ticking async but still affect
        // UI Thread
        @Override
        public void handleMessage(Message message) {
            mBeatsTicked++;
            tick();
        }
    };

    public Handler getHandler() {
        return mHandler;
    }
}
