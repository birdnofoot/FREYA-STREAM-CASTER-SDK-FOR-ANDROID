package com.freya.stream.caster.sdk;

public interface FreyaStreamProcessorCallBack {
    public void onConnecting();
    public void onConnected();
    public void onDisconnect();
    public void onConnectError(int err);
}
