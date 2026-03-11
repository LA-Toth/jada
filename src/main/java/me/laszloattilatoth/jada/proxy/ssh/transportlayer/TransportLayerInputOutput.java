package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

public interface TransportLayerInputOutput extends TransportLayerInput, TransportLayerOutput {
    void setTransportLayer(TransportLayer transportLayer);
}
