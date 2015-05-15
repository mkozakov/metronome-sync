package orangutap.com.metronomesync;


import java.util.Observable;

/**
 * A class that contains data about playback for the metronome, such as BPM
 * Notifies user when a change has been made.
 */

public class MetronomeData extends Observable {
    /**
     * MetronomeData is a singleton class, extends observable
     */

    private static MetronomeData instance = null;
    private int mBpm;

    /**
     * MetronomeData constructor, sets mBpm to the initial bpm
     *
     * @param bpm The beats per minute of the metronome
     */
    private MetronomeData(int bpm) {
        mBpm = bpm;
    }

    /**
     * Get the current BPM
     */
    public int getBPM() {
        return mBpm;
    }

    /**
     * Change The value of bpm the the value we get from getPBM
     *
     * @param bpm The beats per minute of the metronome
     */
    public void setBPM(int bpm) {
        mBpm = bpm;
        notifyObservers();
    }

    /**
     * Creates an instance of MetronomeData if none exist.
     *
     * @param bpm The beats per minute of the metronome
     * @return The instance of MetronomeData
     */
    public static MetronomeData getInstance(int bpm) {
        if (instance == null) {
            instance = new MetronomeData(bpm);
        } else {
            instance.setBPM(bpm);
        }
        return instance;
    }
}
