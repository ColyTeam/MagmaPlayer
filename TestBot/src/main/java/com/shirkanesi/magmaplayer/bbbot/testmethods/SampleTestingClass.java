package com.shirkanesi.magmaplayer.bbbot.testmethods;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.YTDLPAudioTrack;
import com.shirkanesi.magmaplayer.bbbot.Disabled;
import com.shirkanesi.magmaplayer.bbbot.TestingClass;

@Disabled // remove this to enable
public class SampleTestingClass implements TestingClass {

    @Override
    public void execute(AudioPlayer audioPlayer) {
        String[] tracks = System.getenv("TEST_TRACKS").split(";");
        for (String track : tracks) {
            AudioTrack ytdlpAudioTrack = new YTDLPAudioTrack(track);
            audioPlayer.enqueueTrack(ytdlpAudioTrack);
        }
    }

}
