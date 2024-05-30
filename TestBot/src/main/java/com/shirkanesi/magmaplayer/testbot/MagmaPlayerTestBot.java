package com.shirkanesi.magmaplayer.testbot;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.AudioTrackInformation;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioTrack;
import com.shirkanesi.magmaplayer.testbot.discord.DiscordController;
import com.shirkanesi.magmaplayer.testbot.discord.SlashCommandListener;
import com.shirkanesi.magmaplayer.listener.AudioTrackEventListener;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackEndEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackStartedEvent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

/**
 * Main class from the ButterBrot Bot
 *
 * @author Julian, Niklas, Gregyyy
 * @version 1.1
 */
@Slf4j
public class MagmaPlayerTestBot {

    /**
     * Program entry point
     *
     * @param args command line args
     * @throws Exception when something goes wrong during initialization
     */
    public static void main(String[] args) throws Exception {
        System.out.println("""
                  __  __                             _____  _                      \s
                 |  \\/  |                           |  __ \\| |                     \s
                 | \\  / | __ _  __ _ _ __ ___   __ _| |__) | | __ _ _   _  ___ _ __\s
                 | |\\/| |/ _` |/ _` | '_ ` _ \\ / _` |  ___/| |/ _` | | | |/ _ \\ '__|
                 | |  | | (_| | (_| | | | | | | (_| | |    | | (_| | |_| |  __/ |  \s
                 |_|  |_|\\__,_|\\__, |_| |_| |_|\\__,_|_|    |_|\\__,_|\\__, |\\___|_|  \s
                                __/ |                                __/ |         \s
                               |___/                                |___/           \
                """);
        log.info("Starting MagaPlayer-TestBot");

        DiscordController discordController = new DiscordController();


        discordController.getJda().awaitReady();
        log.info("Startup complete");

        Guild guildById = discordController.getJda().getGuildById(System.getenv("TEST_GUILD"));
        assert guildById != null;

        updateSlashCommands(guildById);
        addShutdownHook(discordController.getJda());


        guildById.getAudioManager().openAudioConnection(guildById.getVoiceChannelById(System.getenv("TEST_CHANNEL")));

        AudioPlayer audioPlayer = DiscordController.getNewAudioPlayer(guildById.getAudioManager());

        addTestTrack(audioPlayer);

        discordController.getJda().addEventListener(new SlashCommandListener(audioPlayer));
    }

    private static void updateSlashCommands(Guild guild) {
        CommandListUpdateAction commands = guild.updateCommands();

        commands.addCommands(
                Commands.slash("play", "Play the sound of the url")
                        .addOption(STRING, "url", "URL of sound", true),
                Commands.slash("skip", "Skip the current track"),
                Commands.slash("queue", "Output the current queue"),
                Commands.slash("track", "Output information about current track"),
                Commands.slash("pause", "Pause or resume the current track")
        );

        commands.queue();
        log.info("Updated Slash Commands");
    }

    private static void addTestTrack(AudioPlayer audioPlayer) {
        String[] tracks = System.getenv("TEST_TRACKS").split(";");
        for (String track : tracks) {
            AudioTrack ytdlpAudioTrack = new YTDLPAudioTrack(track);
            audioPlayer.enqueue(ytdlpAudioTrack);
        }
    }

    static void addShutdownHook(JDA jda) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopBot(false, jda)));
    }

    static boolean shutdown = false;

    public static void stopBot(boolean systemExit, JDA jda) {
        if (shutdown)
            return;

        jda.shutdown();
        shutdown = true;

        log.info("Good Bye! :c");
        if (systemExit) System.exit(0);
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
        audioPlayer.enqueue(ytdlpAudioTrack3);
    }

}
