package xyz.rtsvk.alfax.util.lavaplayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import xyz.rtsvk.alfax.util.text.TextUtils;

/**
 * Utility methods for use with LavaPlayer
 * @author Jastrobaron
 */
public class LavaPlayerUtils {

    /**
     * Returns a display name for the track
     * @param track to display
     * @return the display name
     */
    public static String getTrackDisplayName(AudioTrack track) {
        String author = track.getInfo().author;
        String title = track.getInfo().title;
        return TextUtils.format("${0} - ${1}", author, title);
    }

}
