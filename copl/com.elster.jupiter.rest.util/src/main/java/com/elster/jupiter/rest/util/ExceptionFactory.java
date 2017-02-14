/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

/**
 * Created by bvn on 6/23/14.
 */
public class ExceptionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ExceptionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    /**
     * Create a new rest-layer exception. This exception will end as HTTP 400
     * @param messageSeed The message to translate into the return value
     * @return
     */
    public LocalizedException newException(MessageSeed messageSeed) {
        return new RestException(thesaurus, messageSeed);
    }

    /**
     * Create a new rest-layer exception. This exception will end as HTTP 400
     * @param messageSeed The message to translate into the return value, formatted with arguments
     * @return
     */
    public LocalizedException newException(MessageSeed messageSeed, Object... args) {
        return new RestException(thesaurus, messageSeed, args);
    }

    /**
     * Supplies a new rest-layer exception. This exception will end as HTTP 400
     * @param messageSeed The message to translate into the return value, formatted with arguments
     * @return
     */
    public Supplier<LocalizedException> newExceptionSupplier(MessageSeed messageSeed, Object... args) {
        return () -> new RestException(thesaurus, messageSeed, args);
    }

    /**
     * Create a new rest-layer exception. This exception will end with supplied status, typically 404 if
     * error in the URI and 400 if error in message body
     * @param messageSeed The message to translate into the return value
     * @return
     */
    public LocalizedException newException(Response.Status status, MessageSeed messageSeed) {
        return new RestException(status, thesaurus, messageSeed);
    }

    /**
     * Create a new rest-layer exception. This exception will end with supplied status, typically 404 if
     * error in the URI and 400 if error in message body
     * @param messageSeed The message to translate into the return value formatted with arguments
     * @return
     */
    public LocalizedException newException(Response.Status status, MessageSeed messageSeed, Object... args) {
        return new RestException(status, thesaurus, messageSeed, args);
    }

    /**
     * Supplies a new rest-layer exception. This exception will end with supplied status, typically 404 if
     * error in the URI and 400 if error in message body
     * @param messageSeed The message to translate into the return value formatted with arguments
     * @return
     */
    public Supplier<LocalizedException> newExceptionSupplier(Response.Status status, MessageSeed messageSeed, Object... args) {
        return () -> new RestException(status, thesaurus, messageSeed, args);
    }

    public static class RestException extends LocalizedException {

        private final Response.Status status;

        public RestException(Thesaurus thesaurus, MessageSeed messageSeed) {
            this(Response.Status.BAD_REQUEST, thesaurus, messageSeed);
        }

        public RestException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            this(Response.Status.BAD_REQUEST, thesaurus, messageSeed, args);
        }

        public RestException(Response.Status status, Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
            this.status = status;
        }

        public RestException(Response.Status status, Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
            this.status = status;
        }

        public Response.Status getStatus() {
            return status;
        }
    }
}