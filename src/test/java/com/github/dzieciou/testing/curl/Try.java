package com.github.dzieciou.testing.curl;

public class Try {


    public static void main(String[] args) {
        for(int i=0;i<1024;i++) {
            char c = (char)i;
            System.out.println(i + "," + escapeAsHex(c) + "," + escapeAsHexFix(c));
            assert escapeAsHex(c).equals(escapeAsHexFix(c));
        }
    }

    private static String escapeAsHex(char c) {
        int code = (int) c;
        String codeAsHex = Integer.toHexString(code);
        if (code < 256) {
            // Add leading zero when needed to not care about the next character.
            return code < 16 ? "\\x0" + codeAsHex : "\\x" + codeAsHex;
        }
        return String.format("\\u%04x", (int) c);
    }

    private static String escapeAsHexFix(char c) {
        int code = c;
        if (code < 256) {
            return String.format("\\x%02x", (int)c);
        }
        return String.format("\\u%04x", (int) c);
    }

}