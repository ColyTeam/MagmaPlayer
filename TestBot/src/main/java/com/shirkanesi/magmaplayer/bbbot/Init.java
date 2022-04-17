package com.shirkanesi.magmaplayer.bbbot;

import com.shirkanesi.magmaplayer.bbbot.discord.DiscordController;
import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.YTDLPAudioTrack;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
public class Init {

    static void init() throws Exception {
        log.info("Starting init state");
        new DiscordController();
        log.info("Passed init state");
    }

    static void postInit() throws Exception {
        log.info("Starting post-init state");
//        new AudioManager();

        DiscordController.getJDA().awaitReady();
//        SlashCommandManager.initialize(DiscordController.getJDA());

        log.info("Passed post-init state");
    }

    @SneakyThrows
    static void startupComplete() {
        log.info("Startup complete");

        Guild guildById = DiscordController.getJDA().getGuildById(System.getenv("TEST_GUILD"));
        assert guildById != null;
        guildById.getAudioManager().openAudioConnection(guildById.getVoiceChannelById(System.getenv("TEST_CHANNEL")));

        try (AudioPlayer audioPlayer = new AudioPlayer()) {
            guildById.getAudioManager().setSendingHandler(audioPlayer.createSendHandler());

            String[] tracks = System.getenv("TEST_TRACKS").split(";");
            for (String track : tracks) {
                AudioTrack ytdlpAudioTrack = new YTDLPAudioTrack(track);
                audioPlayer.enqueueTrack(ytdlpAudioTrack);
            }
        }
    }

    static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopBot(false)));
    }

    static boolean shutdown = false;

    public static void stopBot(boolean systemExit) {
        if (shutdown)
            return;

        DiscordController.getJDA().shutdown();
        shutdown = true;

        log.info("Good Bye! :c");
        if (systemExit) System.exit(0);
    }

}
