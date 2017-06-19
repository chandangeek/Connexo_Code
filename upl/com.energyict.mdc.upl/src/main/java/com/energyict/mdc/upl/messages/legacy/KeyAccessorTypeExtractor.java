/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.security.KeyAccessorType;

import java.util.Optional;

/**
 * Extracts information that pertains to UPL {@link com.energyict.mdc.upl.security.KeyAccessorType}s
 * from message related objects for the purpose
 * of formatting it.
 *
 * @author stijn
 * @since 11.05.17 - 08:44
 */
public interface KeyAccessorTypeExtractor {

    String id(KeyAccessorType keyAccessorType);

    String name(KeyAccessorType keyAccessorType);

    Optional<Object> actualValue(KeyAccessorType keyAccessorType);

    String actualValueContent(KeyAccessorType keyAccessorType);

    Optional<Object> passiveValue(KeyAccessorType keyAccessorType);

    String passiveValueContent(KeyAccessorType keyAccessorType);

    Optional<String> correspondingSecurityAttribute(String keyAccessorType, String securityPropertySetName);

    /**
     * Gets (or creates) the {@link ThreadContext} for the current Thread.
     * Each Thread will get its own ThreadContext.
     *
     * @return The ThreadContext
     */
    ThreadContext threadContext();

    /**
     * Models contextual information that can be different
     * for each Thread that uses this TariffCalendarExtractor.
     */
    interface ThreadContext {
        OfflineDevice getDevice();

        void setDevice(OfflineDevice offlineDevice);
    }
}