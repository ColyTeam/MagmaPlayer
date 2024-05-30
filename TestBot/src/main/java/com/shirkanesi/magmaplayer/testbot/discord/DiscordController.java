package com.shirkanesi.magmaplayer.testbot.discord;

import com.shirkanesi.magmaplayer.AudioPlayer;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.List;

public class DiscordController {

    @Getter
    private final JDA jda;

    /**
     * Registering the JDA with the needed GatewayIntents.
     *
     * @throws LoginException when the provided token is invalid
     */
    public DiscordController() throws LoginException {
        Collection<GatewayIntent> gatewayIntents = List.of(GatewayIntent.GUILD_VOICE_STATES);

        jda = JDABuilder.createDefault(System.getenv("BOT_TOKEN"), gatewayIntents)
                .disableCache(CacheFlag.EMOJI, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .build();
    }

    public static AudioPlayer getNewAudioPlayer(AudioManager audioManager) {
        AudioPlayer audioPlayer = new AudioPlayer();
        Runtime.getRuntime().addShutdownHook(new Thread(audioPlayer::close));
        audioManager.setSendingHandler(audioPlayer.createSendHandler());
        return audioPlayer;
    }

}
