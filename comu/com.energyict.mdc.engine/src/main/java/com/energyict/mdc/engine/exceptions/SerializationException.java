/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class SerializationException extends LocalizedException {

    private SerializationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... objects) {
        super(thesaurus, messageSeed, objects);
    }

    /**
     * Creates a SerializationException what models the fact that an attempt was made to
     * serialize a DeviceCache object
     *
     * @param thesaurus   the used Thesaurus
     * @param cacheObject the object which we tried to serialize
     * @param message     the exception message
     * @param messageSeed The MessageSeed
     * @return the newly created SerializationException
     */
    public static SerializationException whenSerializingCacheObject(Thesaurus thesaurus, Object cacheObject, String message, MessageSeed messageSeed) {
        return new SerializationException(thesaurus, messageSeed, cacheObject, message);
    }

    /**
     * Creates a SerializationException what models the fact that an attempt was made to
     * deserialize a DeviceCache object
     *
     * @param thesaurus the used Thesaurus
     * @param bytes     the bytes which we tried to deserialized
     * @param message   the exception message
     * @param messageSeed The MessageSeed
     * @return the newly created SerializationException
     */
    public static SerializationException whenDeSerializingCacheObject(Thesaurus thesaurus, byte[] bytes, String message, MessageSeed messageSeed) {
        return new SerializationException(thesaurus, messageSeed, bytes, message);
    }

}