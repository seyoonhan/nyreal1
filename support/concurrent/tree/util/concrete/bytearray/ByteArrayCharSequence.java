package com.han.startup.support.concurrent.tree.util.concrete.bytearray;


import com.ubisoft.hfx.support.concurrent.CharSequences;

import javax.validation.constraints.NotNull;

public class ByteArrayCharSequence implements CharSequence {

    final byte[] bytes;
    final int start;
    final int end;

    public ByteArrayCharSequence(byte[] bytes, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start " + start + " < 0");
        }

        if (end > bytes.length) {
            throw new IllegalArgumentException("end " + end + " > length " + bytes.length);
        }

        if (end < start) {
            throw new IllegalArgumentException("end " + end + " < start " + start);
        }

        this.bytes = bytes;
        this.start = start;
        this.end = end;
    }

    public static byte[] toSingleByteUtf8Encoding(CharSequence charSequence) {
        final int length = charSequence.length();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            char inputChar = charSequence.charAt(i);
            if (inputChar > 255) {
                throw new IncompatibleCharacterException("Input contains a character which cannot be represented as a single byte in UTF-8: " + inputChar);
            }

            bytes[i] = (byte) inputChar;
        }
        return bytes;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        return (char) (bytes[index + start] & 0xFF);
    }

    @Override
    public ByteArrayCharSequence subSequence(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start " + start + " < 0");
        }

        if (end > length()) {
            throw new IllegalArgumentException("start " + start + " < 0");
        }

        if (end < start) {
            throw new IllegalArgumentException("end " + end + " < start " + start);
        }

        return new ByteArrayCharSequence(bytes, this.start + start, this.start + end);
    }

    @NotNull
    @Override
    public String toString() {
        return CharSequences.toString(this);
    }

    public static class IncompatibleCharacterException extends IllegalStateException {
        public IncompatibleCharacterException(String s) {
            super(s);
        }
    }
}
