package com.company.Encoding;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Encoder {
    private String filename;
    private int wordLength;
    private String tail;
    private Map<String, Long> table;
    private Node tree;
    private List<Node> nodes;
    private final int BUFFER_SIZE = 256;
    private Map<String, String> dictionary;

    public Encoder(String filename, int wordLength) {
        this.filename = filename;
        this.wordLength = wordLength;
        this.tail = "";
        this.table = new HashMap<>();
        this.dictionary = new HashMap<>();
    }

    public void encode() throws IOException {
        generateOccurrencesTable();
        generateTree();
        generateDictionary();
        codingProcess();
    }

    private void generateOccurrencesTable() throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        String converted = "";
        while ((read = fis.read(buffer)) > 0) {

            if (read == BUFFER_SIZE) {
                converted = convertToBinary(buffer);
            } else {
                converted = convertToBinary(Arrays.copyOf(buffer, read));
            }
            String fullText = tail + converted;
            tail = fullText.substring(fullText.length() - fullText.length() % wordLength);
            String currentText = fullText.substring(0, fullText.length() - fullText.length() % wordLength);
            updateTable(currentText);
        }
        fis.close();
    }

    private String convertToBinary(int number) {
        return convertNumberToBinary((byte) number);
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

    private void updateTable(String fileContent) {
        for (int start = 0; start < fileContent.length(); start += wordLength) {
            String word = fileContent.substring(start, Math.min(fileContent.length(), start + wordLength));

            if (table.containsKey(word)) {
                table.put(word, table.get(word) + 1);
            } else {
                table.put(word, (long) 1);
            }
        }
    }

    private void generateTree() {
        List<Node> nodes = new ArrayList<>();

        for (String word : table.keySet()) {
            nodes.add(new Node(word, table.get(word)));
        }
        this.nodes = List.copyOf(nodes);

        while (nodes.size() != 1) {
            Collections.sort(nodes);
            Node first = nodes.get(0);
            Node second = nodes.get(1);
            nodes.add(new Node(first, second));
            nodes.remove(0);
            nodes.remove(0);
        }
        tree = nodes.get(0);
        tree.setCode();
    }

    private void generateDictionary() {
        for (Node n : nodes) {
            dictionary.put(n.getWord(), n.getCode());
        }
    }

    private void codingProcess() throws IOException {
        String header = createHeader();

        FileOutputStream fos = new FileOutputStream("en_" + filename);
        int startEnd = header.length() - header.length() % 8;
        String textToWrite = header.substring(0, startEnd);
        String leftovers = header.substring(startEnd);
        fos.write(convertFromBinaryToDecimal(textToWrite));

        FileInputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        String textInBits = "";
        String unconvertedTail = "";
        while ((read = fis.read(buffer)) > 0) {

            if (read == BUFFER_SIZE) {
                textInBits = convertToBinary(buffer);
            } else {
                textInBits = convertToBinary(Arrays.copyOf(buffer, read));
                textInBits=textInBits.substring(0,textInBits.length()-textInBits.length()%wordLength);
            }

            String fullUnconvertedText = unconvertedTail + textInBits;
            unconvertedTail = fullUnconvertedText.substring(fullUnconvertedText.length() -
                    fullUnconvertedText.length() % wordLength);
            String textToConvert = fullUnconvertedText.substring(0,
                    fullUnconvertedText.length() - fullUnconvertedText.length() % wordLength);
            String fullText = leftovers + code(textToConvert);
            startEnd = fullText.length() - fullText.length() % 8;
            textToWrite = fullText.substring(0, startEnd);
            leftovers = fullText.substring(startEnd);
            fos.write(convertFromBinaryToDecimal(textToWrite));
        }
        textToWrite = leftovers + tail;

        while (textToWrite.length() % 8 != 0) {
            textToWrite += "0";
        }

        fos.write(convertFromBinaryToDecimal(textToWrite));
        fis.close();
        fos.close();
    }

    private String createHeader() {
        String tree = this.tree.getTreeAsString("");
        int missingZeroBits = 8 - (10 + tree.length() + getNotFullBytesFromText() + tail.length()) % 8;

        return convertToBinary(wordLength).substring(3) +
                convertToBinary(missingZeroBits + tail.length()).substring(3) +
                tree;
    }

    private int getNotFullBytesFromText() {
        long bytes = 0;
        for (Node n : nodes) {
            bytes += n.getOccurrence() * n.getCode().length();
            bytes %= 8;
        }
        return (int) bytes;
    }

    public byte[] convertFromBinaryToDecimal(String text) {
//        System.out.println("printinimui"+text);
        byte[] chars = new byte[text.length() / 8];

        for (int i = 0; i < text.length() / 8; i++) {
            chars[i] = (byte)Integer.parseInt(text.substring(8 * i, 8 * (i + 1)), 2);
//            System.out.println((int)chars[i]);
        }

        return chars;
    }

    private String code(String textToConvert) {
        String convertedText = "";

        for (int start = 0; start < textToConvert.length(); start += wordLength) {
            String binary = textToConvert.substring(start, start + wordLength);
            convertedText += dictionary.get(binary);
        }

        return convertedText;
    }
}
