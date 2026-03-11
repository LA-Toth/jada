package me.laszloattilatoth.jada.proxy.ssh.core;

import me.laszloattilatoth.jada.proxy.ssh.Options;

import java.util.logging.Logger;

public class DefaultSshProxy implements SshProxy {

    private final Logger logger = Logger.getGlobal();

    @Override
    public Options options() {
        return new Options();
    }

    @Override
    public boolean shouldQuit() {
        return false;
    }

    @Override
    public void run() {

    }

    @Override
    public Logger logger() {
        return logger;
    }
}
