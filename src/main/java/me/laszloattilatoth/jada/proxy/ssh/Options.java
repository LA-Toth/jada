// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh;

import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;

import java.util.Map;

public class Options extends ProxyOptions {
    public final SideOptions clientSide = new SideOptions();
    public final SideOptions serverSide = new SideOptions();

    public final SideOptions sideOptions(Side side) {
        return side == Side.CLIENT ? clientSide : serverSide;
    }

    @Override
    public void load(Map<String, Object> globalOptions, Map<String, Object> options) throws InvalidOptions {
        if (globalOptions != null)
            loadSingle(globalOptions);

        if (options != null)
            loadSingle(options);
    }

    private void loadSingle(Map<String, Object> options) throws InvalidOptions {
        for (Map.Entry<String, Object> entry : options.entrySet()) {

            SideOptions sideOptions = switch (entry.getKey()) {
                case "client" -> clientSide;
                case "server" -> serverSide;
                default -> throw new InvalidOptions(String.format("Unknown SSH option  %s", entry.getKey()));
            };
            if (entry.getValue() instanceof Map entries) {
                Map<String, Object> map = entries;
                for (Map.Entry<String, Object> sideEntry : map.entrySet()) {
                    processEntry(sideOptions, sideEntry);
                }
            }
        }
    }

    private void processEntry(SideOptions sideOptions, Map.Entry<String, Object> entry) throws InvalidOptions {
        String key = entry.getKey();
        if (entry.getValue() instanceof String value) {
            switch (key) {
                case "kex-algos" -> sideOptions.kexAlgorithms = value;
                case "hostkey-algos" -> sideOptions.hostkeyAlgorithms = value;
                default -> throw new InvalidOptions(String.format("Unknown SSH option  %s", entry.getKey()));
            }
        }
    }

    public static class SideOptions {
        public String kexAlgorithms = "diffie-hellman-group14-sha1";
        public String hostkeyAlgorithms = "ssh-rsa";
        public String encryptionAlgorithms = "aes128-ctr";
        public String macAlgorithms = "hmac-sha1";
        public String compressionAlgorithms = "none";
    }
}
