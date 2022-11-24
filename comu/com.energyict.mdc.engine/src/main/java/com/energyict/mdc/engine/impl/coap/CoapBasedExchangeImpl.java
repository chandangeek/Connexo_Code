/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.energyict.mdc.upl.io.CoapBasedExchange;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoapBasedExchangeImpl implements CoapBasedExchange {

    private final CoapExchange coapExchange;

    public CoapBasedExchangeImpl(CoapExchange coapExchange) {
        this.coapExchange = coapExchange;
    }

    @Override
    public String getRequestText() {
        return coapExchange.getRequestText();
    }

    @Override
    public byte[] getRequestPayload() {
        return coapExchange.getRequestPayload();
    }

    @Override
    public void respond(String payload) {
        coapExchange.respond(payload);
    }

    @Override
    public void respond(CoapCode coapCode, String payload) {
        coapExchange.respond(CoAP.ResponseCode.valueOf(coapCode.value), payload);
    }

    @Override
    public void respondOverload(int seconds) {
        coapExchange.respondOverload(seconds);
    }

    @Override
    public void respondClientOverload(int seconds) {
        coapExchange.respondClientOverload(seconds);
    }
}