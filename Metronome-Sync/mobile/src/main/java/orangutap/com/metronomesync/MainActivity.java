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
        // Seek bar in the middle
        private SeekBar mBpmSeekBar;
        // The current bpm displayed large
        private TextView mBpmText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set up the fragment UI layout
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            // Initialize private UI element variables
            mPlayStopBtn = (Button) rootView.findViewById(R.id.play_stop_btn);
            mBpmSeekBar = (SeekBar) rootView.findViewById(R.id.bpm_seek);
            mBpmText = (TextView) rootView.findViewById(R.id.bpm_value);

            // Get singleton metronome backend class
            final Metronome metronome = ((MainActivity) getActivity()).getMetronome();

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

            // set bounds for bpm assignment
            final int minBpm = getResources().getInteger(R.integer.min_bpm);
            final int maxBpm = getResources().getInteger(R.integer.max_bpm);
            final int defaultBpm = getResources().getInteger(R.integer.default_bpm);
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
