package com.shirkanesi.magmaplayer.testbot.discord;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.AudioTrackInformation;
import com.shirkanesi.magmaplayer.util.YTDLPManager;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioItem;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioPlaylist;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.net.MalformedURLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class SlashCommandListener extends ListenerAdapter {

    private final AudioPlayer audioPlayer;

    public SlashCommandListener(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "play" -> {
                String url = event.getOption("url", OptionMapping::getAsString);
                YTDLPAudioItem item;
                try {
                    item = YTDLPManager.loadUrl(url);
                } catch (MalformedURLException e) {
                    event.reply("Could not loud URL").setEphemeral(true).queue();
                    return;
                }
                if (item instanceof YTDLPAudioTrack track) {
                    audioPlayer.enqueue(track);
                    event.reply("Track enqueued").setEphemeral(true).queue();
                } else if (item instanceof YTDLPAudioPlaylist playlist) {
                    audioPlayer.enqueue(playlist);
                    event.reply(playlist.getTracks().size() + " tracks enqueued").setEphemeral(true).queue();
                }
            }
            case "skip" -> {
                audioPlayer.next();
                event.reply("Skipped").setEphemeral(true).queue();
            }
            case "queue" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                event.deferReply(true).queue(interaction -> {
                    int index = 1;
                    for (AudioTrack audioTrack : audioPlayer.getTrackQueue()) {
                        AudioTrackInformation information = audioTrack.getInformation();
                        embedBuilder.addField(index + ". Track",
                                information.getTitle() + " - " + information.getCreator(), false);
                        index++;
                    }

                    if (audioPlayer.getTrackQueue().isEmpty()) {
                        interaction.editOriginal("Queue is empty").queue();
                    } else {
                        interaction.editOriginalEmbeds(embedBuilder.build()).queue();
                    }
                });
            }
            case "track" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                AudioTrack currentTrack = audioPlayer.getCurrentAudioTrack();

                if (currentTrack == null) {
                    event.reply("No track currently playing").setEphemeral(true).queue();
                    return;
                }

                AudioTrackInformation information = currentTrack.getInformation();

                embedBuilder.addField("Title", information.getTitle(), false);
                embedBuilder.addField("Creator", information.getCreator(), false);

                DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("mm:ss").toFormatter();
                if (information.getDuration() > 60 * 60) {
                    formatter = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter();
                }
                String currentPosition = formatter.format(LocalTime.ofSecondOfDay(currentTrack.getCurrentPosition()));
                String duration = formatter.format(LocalTime.ofSecondOfDay(information.getDuration()));
                embedBuilder.addField("Duration", currentPosition + "/" + duration, false);

                event.replyEmbeds(embedBuilder.build()).queue();
            }
            case "pause" -> {
                if (audioPlayer.isPaused()) {
                    audioPlayer.resume();
                    event.reply("Audio player resumed").setEphemeral(true).queue();
                } else {
                    audioPlayer.pause();
                    event.reply("Audio player paused").setEphemeral(true).queue();
                }
            }
        }
    }
}
