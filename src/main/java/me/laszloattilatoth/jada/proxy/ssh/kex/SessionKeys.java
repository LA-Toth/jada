package me.laszloattilatoth.jada.proxy.ssh.kex;

public record SessionKeys(byte[] iv, byte[] enc_key, byte[] integrity_key) {

    public static SessionKeys createServerSessionKeys(KexOutput output) {
        return new SessionKeys(output.iv_s2c(), output.enc_key_s2c(), output.integrity_key_s2c());
    }

    public static SessionKeys createClientSessionKeys(KexOutput output) {
        return new SessionKeys(output.iv_c2s(), output.enc_key_c2s(), output.integrity_key_c2s());
    }
}
