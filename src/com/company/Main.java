package com.company;

import com.company.Decoding.Decoder;
import com.company.Encoding.Encoder;

public class Main {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            switch (args.length) {
                case 1:
                    Decoder decoder = new Decoder(args[0]);
                    decoder.decode();
                    break;
                case 2:
                    Encoder encoder = new Encoder(args[0], Integer.parseInt(args[1]));
                    encoder.encode();
            }
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Duration: " + (double) (endTime - startTime) / 1000 + " seconds");
    }
}
