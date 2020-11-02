package com.github.dzieciou.testing.curl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecutor {

    public static CommandOutput runCommand(String command) throws IOException {
        String[] commands = tokenize(command).toArray(new String[0]);
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(commands);
        return new CommandOutput(
                readAll(proc.getInputStream()), readAll(proc.getErrorStream())
        );
    }

    private static String readAll(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String s;
        while ((s = reader.readLine()) != null) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    private static List<String> tokenize(String cmd) {
        List<String> tokens = new ArrayList<String>();
        Matcher m = Pattern.compile("([^']\\S*|'.+?')\\s*").matcher(cmd);
        while (m.find())
            tokens.add(m.group(1).replace("'", ""));
        return tokens;
    }

    public static class CommandOutput {
        final String stdIn;
        final String stdErr;

        public CommandOutput(String stdIn, String stdErr) {
            this.stdIn = stdIn;
            this.stdErr = stdErr;
        }
    }
}
