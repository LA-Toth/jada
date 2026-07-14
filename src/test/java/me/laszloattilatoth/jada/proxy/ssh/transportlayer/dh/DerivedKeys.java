package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

public record DerivedKeys(
        byte[] ivC2S,
        byte[] ivS2C,
        byte[] keyC2S,
        byte[] keyS2C,
        byte[] macC2S,
        byte[] macS2C
) {}
