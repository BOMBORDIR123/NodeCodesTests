package by.javaguru.nordcodestests.utils;

import java.security.SecureRandom;

/**
 * Генератор токенов фиксированной длины (32 символа),
 * использующий криптографически стойкий случайный генератор.
 * Токен состоит из HEX-символов (0–9, A–F).
 */
public class TokenGenerator {

    private static final String SYMBOLS = "0123456789ABCDEF";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateToken() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            int index = RANDOM.nextInt(SYMBOLS.length());
            sb.append(SYMBOLS.charAt(index));
        }
        return sb.toString();
    }
}
