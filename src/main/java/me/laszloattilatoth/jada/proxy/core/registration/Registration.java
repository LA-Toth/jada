package me.laszloattilatoth.jada.proxy.core.registration;

import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;

public record Registration(String name, Class<? extends ProxyMain> main, Class<? extends ProxyOptions> options) {

    public static Registration create(String name, Class<? extends ProxyMain> main, Class<? extends ProxyOptions> options) {
        return new Registration(name, main, options);
    }
}
