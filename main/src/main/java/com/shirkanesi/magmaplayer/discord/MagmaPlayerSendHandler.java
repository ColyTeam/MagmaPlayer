package com.shirkanesi.magmaplayer.discord;

import com.shirkanesi.magmaplayer.AudioPlayer;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public class MagmaPlayerSendHandler implements AudioSendHandler {

    private final AudioPlayer audioPlayer;

    public MagmaPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean canProvide() {
        return audioPlayer.canProvide();
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return audioPlayer.nextSnippet();
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
