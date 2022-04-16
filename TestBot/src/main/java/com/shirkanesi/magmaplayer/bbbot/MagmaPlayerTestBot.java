package com.shirkanesi.magmaplayer.bbbot;

import lombok.extern.slf4j.Slf4j;

import static com.shirkanesi.magmaplayer.bbbot.Init.addShutdownHook;
import static com.shirkanesi.magmaplayer.bbbot.Init.init;
import static com.shirkanesi.magmaplayer.bbbot.Init.postInit;
import static com.shirkanesi.magmaplayer.bbbot.Init.startupComplete;

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
        System.out.println("  __  __                             _____  _                       \n" +
                " |  \\/  |                           |  __ \\| |                      \n" +
                " | \\  / | __ _  __ _ _ __ ___   __ _| |__) | | __ _ _   _  ___ _ __ \n" +
                " | |\\/| |/ _` |/ _` | '_ ` _ \\ / _` |  ___/| |/ _` | | | |/ _ \\ '__|\n" +
                " | |  | | (_| | (_| | | | | | | (_| | |    | | (_| | |_| |  __/ |   \n" +
                " |_|  |_|\\__,_|\\__, |_| |_| |_|\\__,_|_|    |_|\\__,_|\\__, |\\___|_|   \n" +
                "                __/ |                                __/ |          \n" +
                "               |___/                                |___/           ");
        log.info("Starting MagaPlayer-TestBot");

        addShutdownHook();
        init();
        postInit();
        startupComplete();

    }

}
