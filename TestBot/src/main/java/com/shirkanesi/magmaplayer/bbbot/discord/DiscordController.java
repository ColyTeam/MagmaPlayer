package com.shirkanesi.magmaplayer.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.List;

public class DiscordController {

    private static DiscordController instance;

    private final JDA jda;

    /**
     * Registering the JDA with the needed GatewayIntents.
     *
     * @throws LoginException when the provided token is invalid
     */
    public DiscordController() throws LoginException {
        instance = this;
        Collection<GatewayIntent> gatewayIntents = List.of(GatewayIntent.GUILD_VOICE_STATES);

        jda = JDABuilder.createDefault(System.getenv("BOT_TOKEN"), gatewayIntents)
                .disableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .build();
    }

    /**
     * Get the Main JDA instance for executing future tasks
     *
     * @return the JDA instance from the BOT
     */
    public static JDA getJDA() {
        return instance.jda;
    }

}
