package com.cardmanager.util;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class CardNumberGenerator {

    private static final Random RANDOM = new Random();
    private long lastTimestamp = -1L;
    private long sequence = 0L;
    private static final long MAX_SEQUENCE = 9999L;

    public synchronized String generateCardNumber() {
        long timestamp = getCustomTimestamp();

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) % MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextTimestamp(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        long timePart = timestamp % 100000;
        return String.format("%05d%04d", timePart, sequence);
    }

    public String generatePassword() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return password.toString();
    }

    private long getCustomTimestamp() {
        return System.currentTimeMillis() - 1704067200000L;
    }

    private long waitNextTimestamp(long lastTimestamp) {
        long timestamp = getCustomTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCustomTimestamp();
        }
        return timestamp;
    }
}
