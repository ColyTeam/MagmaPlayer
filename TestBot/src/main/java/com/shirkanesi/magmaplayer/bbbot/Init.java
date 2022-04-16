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

        AudioPlayer audioPlayer = new AudioPlayer();


//        AudioTrack ytdlpAudioTrack = new YTDLPAudioTrack("https://www.youtube.com/watch?v=hG4lT4fxj8M");
////        new YTDLPAudioTrack("https://www.youtube.com/watch?v=hG4lT4fxj8M");
//        AudioTrack ytdlpAudioTrack2 = new YTDLPAudioTrack("https://www.youtube.com/watch?v=mkHi6jei0XU");
//        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://www.youtube.com/watch?v=68F0AeI_nDY");
        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://www.youtube.com/watch?v=fP9i4yqvGHY");
//        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://soundcloud.com/emmaesknallt/azteken-salbei-emma-esknallt-klinisch-getestet");
//        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://liveradio.swr.de/rddez3a/swr3/");    // https://prod.radio-api.net/stations/local?stationIds=swr3
//        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://stream.regenbogen.de/karlsruhe/aac-128/radiode");    // https://prod.radio-api.net/stations/local?stationIds=swr3
//        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://www.ardmediathek.de/video/feuer-und-flamme/folge-1-grosseinsatz-und-der-weg-zum-feuer-ist-versperrt-s05-e01/wdr/Y3JpZDovL3dkci5kZS9CZWl0cmFnLTJkOWQwZmY0LWU5MWMtNDBmOS05MjZhLTNmMWNkMmQ1YWMwOA");
//        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://vimeo.com/272444763");

//        audioPlayer = new AudioPlayer(ytdlpAudioTrack3);

//        guildById.getAudioManager().setSendingHandler(new AlternativeSendHandler(audioPlayer));
        guildById.getAudioManager().setSendingHandler(audioPlayer.createSendHandler());

//        TimeUnit.SECONDS.sleep(3);
//        audioPlayer.enqueueTrack(new YTDLPAudioTrack("https://www.youtube.com/watch?v=68F0AeI_nDY"));
        audioPlayer.enqueueTrack(ytdlpAudioTrack3);
//        audioPlayer.enqueueTrack(ytdlpAudioTrack);
////        audioPlayer.enqueueTrack(ytdlpAudioTrack2);
//
//        TimeUnit.SECONDS.sleep(5);
//        audioPlayer.next();
//        audioPlayer.pause();
//        TimeUnit.SECONDS.sleep(5);
//        audioPlayer.resume();


//        TimeUnit.SECONDS.sleep(6); // puzllqueue
//        ytdlpAudioTrack.jumpTo(60);
//
//        TimeUnit.SECONDS.sleep(6);
//        ytdlpAudioTrack.jumpTo(5);

//        audioPlayer.setTrack(ytdlpAudioTrack2);
//        ytdlpAudioTrack2.restart();

//        AudioPlayer audioPlayer2 = new AudioPlayer("https://www.youtube.com/watch?v=mkHi6jei0XU");

//        audioPlayer.setOnAfterFinish(() ->
//                guildById.getAudioManager().setSendingHandler(new AlternativeSendHandler(audioPlayer2))
//        );


//        audioPlayer.jumpTo(60);

//        guildById.getAudioManager().setSendingHandler(new AudioPlayerSendHandler("https://www.youtube.com/watch?v=hG4lT4fxj8M"));
//
//        TimeUnit.SECONDS.sleep(10);
//
//        guildById.getAudioManager().setSendingHandler(new AudioPlayerSendHandler("https://www.youtube.com/watch?v=mkHi6jei0XU"));

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
