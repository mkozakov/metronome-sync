package orangutap.com.metronomesync;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity {

    private Metronome mMetronome;          /* The metronome backend class */
    private MetronomeData mMetronomeData;  /* The settings for the metronome */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Initialize UI layout
        setContentView(R.layout.activity_main);
        // Set up fragment layout
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
        // Initialize private variables
        mMetronomeData = MetronomeData.getInstance(getResources().getInteger(R.integer.default_bpm));
        mMetronome = Metronome.getInstance(this, mMetronomeData);
    }

    /**
     * @return metronome controller
     */
    public Metronome getMetronome() {
        return mMetronome;
    }

    /**
     * @return metronome settings
     */
    public MetronomeData getMetronomeData() {
        return mMetronomeData;
    }

    /**
     * Main fragment containing the metronome controls.
     */
    public static class MainFragment extends Fragment {
        // Play/Stop button in the bottom center
        private Button mPlayStopBtn;
        // The tap tempo button
        private Button mTapTempoBtn;
        // Seek bar in the middle
        private SeekBar mBpmSeekBar;
        // The current bpm displayed large
        private TextView mBpmText;

        // The time between previous tap tempo
        private long[] mTapTempoArray = new long[] {0, 0, 0, 0 ,0};
        // The time since last tap of tap tempo
        private long mLastTapTempo = 0;
        // The current time when clicking tap tempo button
        private long mCurrentTapTempo;
        // The number of taps in the current tap tempo frequent pushing
        private int mNumTaps = 0;
        // Threshold for how long a session of tapping the tempo lasts
        private long mTapTimeTimeout = 3000;

        /**
         * Calculates the bpm from an array of longs representing milliseconds between taps of tap
         * tempo button
         * @param tapArray the array that holds the millisecond values
         * @return the new bpm!
         */
        public int calculateBpm(long[] tapArray) {
            long sum = 0;
            float average;
            for (int i = 0; i < tapArray.length; i++) {
                sum += tapArray[i];
            }
            average = (float) (sum / tapArray.length);
            return (int) (60000.0 / average);
        }

        /**
         * Pushes a value to the front of an array and deletes the last value
         * @param array array of longs representing milliseconds between taps of button
         * @param value the latest difference between taps!
         * @return the new array of the latest n taps! Woo!
         */
        public long[] pushValue(long[] array, long value)
        {
            for(int i = array.length - 2; i >= 0; i--)
            {
                array[i + 1] = array[i];
            }
            array[0] = value;
            return array;
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set up the fragment UI layout
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            // Initialize private UI element variables
            mPlayStopBtn = (Button) rootView.findViewById(R.id.play_stop_btn);
            mTapTempoBtn = (Button) rootView.findViewById(R.id.tap_tempo_btn);
            mBpmSeekBar = (SeekBar) rootView.findViewById(R.id.bpm_seek);
            mBpmText = (TextView) rootView.findViewById(R.id.bpm_value);

            // Get singleton metronome backend class
            final Metronome metronome = ((MainActivity) getActivity()).getMetronome();

            // set bounds for bpm assignment
            final int minBpm = getResources().getInteger(R.integer.min_bpm);
            final int maxBpm = getResources().getInteger(R.integer.max_bpm);
            final int defaultBpm = getResources().getInteger(R.integer.default_bpm);

            // Listener for the tap tempo button
            // TODO: Break into smaller methods!
            mTapTempoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long mTimeBetweenTaps;
                    int newBpm;
                    mCurrentTapTempo = System.currentTimeMillis();
                    // If no last tap was recorded, this is the first!
                    if ( mNumTaps == 0)
                    {
                        mLastTapTempo = mCurrentTapTempo;
                        mNumTaps++;
                    }
                    else {
                        // calculate time between taps
                        mTimeBetweenTaps = mCurrentTapTempo - mLastTapTempo;
                        mLastTapTempo = mCurrentTapTempo;
                        // check if it was a long time since last tap
                        if (mTimeBetweenTaps > mTapTimeTimeout) {
                            mNumTaps = 0;
                        }
                        else
                        {
                            mTapTempoArray = pushValue(mTapTempoArray, mTimeBetweenTaps);
                            if(mNumTaps < 3) mNumTaps++;
                            if(mNumTaps >= 2) {
                                newBpm = calculateBpm(mTapTempoArray);
                                mBpmText.setText(Integer.toString(newBpm));
                                // TODO: Turn this into a method, its also used in seek bar
                                MetronomeData data = ((MainActivity) getActivity()).getMetronomeData();
                                data.setBPM(newBpm);
                                metronome.update(data, new Object());
                                mBpmSeekBar.setProgress(newBpm - minBpm);
                            }

                        }

                    }



                }
            });

            // Listens for start and stop clicks
            mPlayStopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(metronome.isRunning()) {
                        metronome.stop();
                    } else {
                        metronome.play(true);
                    }
                }
            });

            mBpmSeekBar.setMax(maxBpm - minBpm);
            mBpmSeekBar.setProgress(defaultBpm - minBpm);


            // Listener for the seek bar in the middle
            mBpmSeekBar.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        // When the seek bar is changed update the tempo
                        // immediately
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            mBpmText.setText(Integer.toString(progress + minBpm));
                            MetronomeData data = ((MainActivity) getActivity()).getMetronomeData();
                            data.setBPM(seekBar.getProgress() + minBpm);
                            metronome.update(data, new Object());
                        }
                    }
            );

            return rootView;
        }
    }
}
