package com.energyict.mdc.upl.io;

public interface CoapBasedExchange {

    String getRequestText();

    byte[] getRequestPayload();
}
