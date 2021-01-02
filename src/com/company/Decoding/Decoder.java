package com.company.Decoding;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Decoder {
    private String filename;
    private int wordLength;
    private int tailLength;
    private final int BUFFER_SIZE = 256;
    private String unresolvedPart;
    private String tail;
    private Node tree;
    private FileInputStream fis;
    FileOutputStream fos;
    private boolean fileEnded;

    public Decoder(String filename) throws FileNotFoundException {
        this.filename = filename;
        fis = new FileInputStream(filename);
        fileEnded = false;
    }

    public void decode() throws IOException {
        unresolvedPart = loadNewBits();
        getWordLengthAndTail();
        generateTree();
        decoding();
    }

    private String loadNewBits() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = fis.read(buffer);
        String newBinaryString = "";
        if (read > 0) {
            if (read == BUFFER_SIZE) {
                newBinaryString = convertToBinary(buffer);
            } else {
                newBinaryString = convertToBinary(Arrays.copyOf(buffer, read));
                tail = newBinaryString.substring(newBinaryString.length() - tailLength);
                newBinaryString = newBinaryString.substring(0, newBinaryString.length() - tailLength);
                fileEnded = true;
            }
            return newBinaryString;
        }
        return "";
    }

    private String convertToBinary(byte[] array) {
        StringBuilder result = new StringBuilder();
        for (byte b : array) {
            String value = convertNumberToBinary(b);
            result.append(value);
        }
        return result.toString();
    }

    private static String convertNumberToBinary(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    private byte[] convertArrayFromBinaryToDecimal(String text) {
        byte[] chars = new byte[text.length() / 8];

        for (int i = 0; i < text.length() / 8; i++) {
            chars[i] = (byte) Integer.parseInt(text.substring(8 * i, 8 * (i + 1)), 2);
        }

        return chars;
    }

    private int convertFromBinaryToDecimal(String text) {
        return Integer.parseInt(text, 2);
    }

    private void getWordLengthAndTail() {
        wordLength = convertFromBinaryToDecimal(unresolvedPart.substring(0, 5));
        tailLength = convertFromBinaryToDecimal(unresolvedPart.substring(5, 10));
        unresolvedPart = unresolvedPart.substring(10);
    }

    private void generateTree() throws IOException {
        generateFirstTreeNode();

        while (!tree.isTreeComplete()) {
            if (unresolvedPart.length() == 0) {
                unresolvedPart = loadNewBits();
            }
            String leaf = unresolvedPart.substring(0, 1);
            unresolvedPart = unresolvedPart.substring(1);

            if (leaf.equals("0")) {
                tree.insert(false, "");
            } else {
                if (unresolvedPart.length() < wordLength) {
                    unresolvedPart += loadNewBits();
                }
                String word = unresolvedPart.substring(0, wordLength);
                unresolvedPart = unresolvedPart.substring(wordLength);
                tree.insert(true, word);
            }
        }
    }

    private void generateFirstTreeNode() throws IOException {
        String first = unresolvedPart.substring(0, 1);
        unresolvedPart = unresolvedPart.substring(1);
        if (first.equals("0")) {
            tree = new Node(this, 0);
        } else {
            if (unresolvedPart.length() < wordLength) {
                unresolvedPart = unresolvedPart + loadNewBits();
            }
            tree = new Node(this, 0, unresolvedPart.substring(0, wordLength));
            unresolvedPart = unresolvedPart.substring(wordLength);
        }
    }

    private void decoding() throws IOException {
        fos = new FileOutputStream("dec_" + filename);

        int longestWordLength = tree.getBiggestWordLength();
        String currentText = "";
        while (!fileEnded || unresolvedPart.length() > 0) {
            if (unresolvedPart.length() < longestWordLength) {
                unresolvedPart += loadNewBits();
                currentText = writeBytes(currentText);
            }
            currentText += tree.getWord(unresolvedPart);
        }

        currentText += tail;
        writeBytes(currentText);
        fis.close();
        fos.close();
    }

    public void updateUnresolved(int bytesRead) {
        unresolvedPart = unresolvedPart.substring(bytesRead);
    }

    private String writeBytes(String byteString) throws IOException {
        int stringEnd = byteString.length() - byteString.length() % 8;

        byte[] bytes = convertArrayFromBinaryToDecimal(byteString.substring(0, stringEnd));

        fos.write(bytes);

        return byteString.substring(stringEnd);
    }
}
