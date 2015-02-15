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

    private Metronome mMetronome;
    private MetronomeData mMetronomeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }

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

        private Button mPlayStopBtn;
        private SeekBar mBpmSeekBar;
        private TextView mBpmText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mPlayStopBtn = (Button) rootView.findViewById(R.id.play_stop_btn);
            mBpmSeekBar = (SeekBar) rootView.findViewById(R.id.bpm_seek);
            mBpmText = (TextView) rootView.findViewById(R.id.bpm_value);

            final Metronome metronome = ((MainActivity) getActivity()).getMetronome();

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

            final int minBpm = getResources().getInteger(R.integer.min_bpm);
            final int maxBpm = getResources().getInteger(R.integer.max_bpm);
            final int defaultBpm = getResources().getInteger(R.integer.default_bpm);
            mBpmSeekBar.setMax(maxBpm - minBpm);
            mBpmSeekBar.setProgress(defaultBpm - minBpm);
            mBpmSeekBar.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            MetronomeData data = ((MainActivity) getActivity()).getMetronomeData();
                            data.setBPM(seekBar.getProgress() + minBpm);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            mBpmText.setText(Integer.toString(progress + minBpm));
                        }
                    }
            );

            return rootView;
        }
    }
}
