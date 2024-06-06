package com.shirkanesi.magmaplayer.util;

import java.util.ArrayList;
import java.util.List;

public class FormatUtils {


    public static String[] format(String[] format, String... args) {
        List<String> formatted = new ArrayList<>();
        List<String> argList = new ArrayList<>(List.of(args));

        for (String formatArg : format) {
            formatted.add(String.format(formatArg, argList.toArray()));
            int count = formatArg.split("%s", -1).length - 1;
            if (count > 0) {
                argList.subList(0, count).clear();
            }
        }

        return formatted.toArray(new String[0]);
    }

}
