package me.laszloattilatoth.jada.core;

import java.security.SecureRandom;

public final class TestRandom extends SecureRandom {
    private final SecureRandom delegate;

    public TestRandom(long seed) {
        try {
            delegate = SecureRandom.getInstance("SHA1PRNG");
            delegate.setSeed(seed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nextBytes(byte[] bytes) {
        delegate.nextBytes(bytes);
    }
}
