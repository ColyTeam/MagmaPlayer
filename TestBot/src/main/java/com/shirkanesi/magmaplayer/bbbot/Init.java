package com.shirkanesi.magmaplayer.bbbot;

import com.shirkanesi.magmaplayer.AudioTrackInformation;
import com.shirkanesi.magmaplayer.YTDLPAudioTrack;
import com.shirkanesi.magmaplayer.bbbot.discord.DiscordController;
import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.listener.AudioTrackEventListener;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackEndEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackStartedEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Method;
import java.util.Set;

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
        Runtime.getRuntime().addShutdownHook(new Thread(audioPlayer::close));
        guildById.getAudioManager().setSendingHandler(audioPlayer.createSendHandler());



        Set<Class<? extends TestingClass>> testClasses = new Reflections("com.shirkanesi.magmaplayer.bbbot.testmethods", new SubTypesScanner(false)).getSubTypesOf(TestingClass.class);

        Method executeMethod = TestingClass.class.getDeclaredMethod("execute", AudioPlayer.class);

        for (Class<? extends TestingClass> testClass : testClasses) {
            if (!testClass.isAnnotationPresent(Disabled.class)) {
                executeMethod.invoke(testClass.getDeclaredConstructor().newInstance(), audioPlayer);
            }
        }

    }

    private void testJulian(Guild guildById, AudioPlayer audioPlayer) {
        AudioTrack ytdlpAudioTrack3 = new YTDLPAudioTrack("https://www.youtube.com/watch?v=fP9i4yqvGHY");

        AudioTrackInformation information = ytdlpAudioTrack3.getInformation();
        System.out.println(information);
        ytdlpAudioTrack3.getAudioTrackObserver().addEventListener(new AudioTrackEventListener() {
            @Override
            public void onAudioTrackStarted(AudioTrackStartedEvent event) {
                System.out.println("#########################################################\nStarted");
            }

            @Override
            public void onAudioTrackEnded(AudioTrackEndEvent event) {
                AudioTrackEventListener.super.onAudioTrackEnded(event);
                System.out.println("#########################################################\nEnded");
            }

        });

        guildById.getAudioManager().setSendingHandler(audioPlayer.createSendHandler());
        audioPlayer.enqueueTrack(ytdlpAudioTrack3);
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
