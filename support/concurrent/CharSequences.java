package com.han.startup.support.concurrent;

import java.util.Iterator;

public class CharSequences {

    CharSequences() {
    }

    public static Iterable<CharSequence> generateSuffixes(final CharSequence input) {
        return () -> new Iterator<CharSequence>() {
            int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < input.length();
            }

            @Override
            public CharSequence next() {
                return input.subSequence(currentIndex++, input.length());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Modification not supported");
            }
        };
    }

    public static Iterable<CharSequence> generatePrefixes(final CharSequence input) {
        return () -> new Iterator<CharSequence>() {
            int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < input.length();
            }

            @Override
            public CharSequence next() {
                return input.subSequence(0, ++currentIndex);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Modification not supported");
            }
        };
    }

    public static CharSequence getCommonPrefix(CharSequence first, CharSequence second) {
        int minLength = Math.min(first.length(), second.length());
        for (int i = 0; i < minLength; i++) {
            if (first.charAt(i) != second.charAt(i)) {
                return first.subSequence(0, i);
            }
        }
        return first.subSequence(0, minLength);
    }

    public static CharSequence getSuffix(CharSequence input, int startIndex) {
        if (startIndex >= input.length()) {
            return "";
        }
        return input.subSequence(startIndex, input.length());
    }

    public static CharSequence getPrefix(CharSequence input, int endIndex) {
        if (endIndex > input.length()) {
            return input;
        }
        return input.subSequence(0, endIndex);
    }

    public static CharSequence subtractPrefix(CharSequence main, CharSequence prefix) {
        int startIndex = prefix.length();
        int mainLength = main.length();
        if (startIndex > mainLength) {
            return "";
        }
        return main.subSequence(startIndex, mainLength);
    }

    public static CharSequence concatenate(final CharSequence first, final CharSequence second) {
        return new StringBuilder().append(first).append(second);
    }

    public static CharSequence reverse(CharSequence input) {
        return new StringBuilder(input.length()).append(input).reverse();
    }

    public static CharSequence fromCharArray(final char[] characters) {
        return new StringBuilder(characters.length).append(characters);
    }

    public static char[] toCharArray(CharSequence charSequence) {
        final int numChars = charSequence.length();
        char[] charArray = new char[numChars];
        for (int i = 0; i < numChars; i++) {
            charArray[i] = charSequence.charAt(i);
        }
        return charArray;
    }

    public static String toString(CharSequence charSequence) {
        if (charSequence == null) {
            return null;
        }
        if (charSequence instanceof String) {
            return (String) charSequence;
        }
        return String.valueOf(charSequence);
    }
}
