package com.shirkanesi.magmaplayer.bbbot.discord;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.AudioTrackInformation;
import com.shirkanesi.magmaplayer.YTDLPAudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

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
                audioPlayer.enqueueTrack(new YTDLPAudioTrack(url));
                event.reply("Track enqueued").setEphemeral(true).queue();
            }
            case "skip" -> {
                audioPlayer.next();
                event.reply("Skipped").setEphemeral(true).queue();
            }
            case "queue" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();

                int index = 1;
                for (AudioTrack audioTrack : audioPlayer.getTrackQueue()) {
                    AudioTrackInformation information = audioTrack.getInformation();
                    embedBuilder.addField(index + ". Track",
                            information.getTitle() + " - " + information.getCreator(), false);
                    index++;
                }

                if (audioPlayer.getTrackQueue().isEmpty()) {
                    event.reply("Queue is empty").setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(embedBuilder.build()).queue();
                }
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

                event.replyEmbeds(embedBuilder.build()).queue();
            }
        }
    }
}
