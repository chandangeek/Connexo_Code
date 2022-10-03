package com.energyict.mdc.upl.io;

public interface CoapBasedExchange {

    String getRequestText();

    byte[] getRequestPayload();

    void respond(String payload);

    void respondOverload(int seconds);

    void respondClientOverload(int seconds);
}
