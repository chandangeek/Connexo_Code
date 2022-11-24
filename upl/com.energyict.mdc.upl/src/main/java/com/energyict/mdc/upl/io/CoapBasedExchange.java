/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.io;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface CoapBasedExchange {

    void respond(CoapCode coapCode, String payload);

    String getRequestText();

    byte[] getRequestPayload();

    void respond(String payload);

    public static enum CoapCode {
        _UNKNOWN_SUCCESS_CODE(0),
        CREATED(1),
        DELETED(2),
        VALID(3),
        CHANGED(4),
        CONTENT(5),
        CONTINUE(31),
        BAD_REQUEST(0),
        UNAUTHORIZED(1),
        BAD_OPTION(2),
        FORBIDDEN(3),
        NOT_FOUND(4),
        METHOD_NOT_ALLOWED(5),
        NOT_ACCEPTABLE(6),
        REQUEST_ENTITY_INCOMPLETE(8),
        CONFLICT(9),
        PRECONDITION_FAILED(12),
        REQUEST_ENTITY_TOO_LARGE(13),
        UNSUPPORTED_CONTENT_FORMAT(15),
        UNPROCESSABLE_ENTITY(22),
        TOO_MANY_REQUESTS(29),
        INTERNAL_SERVER_ERROR(0),
        NOT_IMPLEMENTED(1),
        BAD_GATEWAY(2),
        SERVICE_UNAVAILABLE(3),
        GATEWAY_TIMEOUT(4),
        PROXY_NOT_SUPPORTED(5);

        public final int value;

        private CoapCode(int value) {
            this.value = value;
        }
    }

    void respondOverload(int seconds);

    void respondClientOverload(int seconds);
}
