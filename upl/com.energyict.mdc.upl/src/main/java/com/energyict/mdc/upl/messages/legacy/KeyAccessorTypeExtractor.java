/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.messages.legacy;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.security.CertificateWrapper;
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
@ProviderType
public interface KeyAccessorTypeExtractor {

    String id(KeyAccessorType keyAccessorType);

    String name(KeyAccessorType keyAccessorType);

    Optional<Object> actualValue(KeyAccessorType keyAccessorType);

    /**
     * Extracts the actual value of a {@link KeyAccessorType} or {@code Optional.empty()} if not available.
     * This method should be used for MBus devices. The extra {@param deviceId} is used to discriminate between
     * all possible slave device security accessors.
     * @param keyAccessorType requested {@link KeyAccessorType}
     * @param deviceId of the MBus device
     * @return wrapped value of the requested security accessor or {@code Optional.empty()}
     */
    Optional<Object> actualValue(KeyAccessorType keyAccessorType, int deviceId);

    String actualValueContent(KeyAccessorType keyAccessorType);

    /**
     * Extracts the actual value content of a {@link KeyAccessorType}.
     * This method should be used for MBus devices. The extra {@param deviceId} is used to discriminate between
     * all possible slave device security accessors.
     * @param keyAccessorType requested {@link KeyAccessorType}
     * @param deviceId of the MBus device
     * @return the security value as String or empty string
     * @throws UnsupportedOperationException for {@link CertificateWrapper} instances
     */
    String actualValueContent(KeyAccessorType keyAccessorType, int deviceId);

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

    String getWrapperKeyActualValue(KeyAccessorType keyAccessorType);

    /**
     * Models contextual information that can be different
     * for each Thread that uses this TariffCalendarExtractor.
     */
    interface ThreadContext {
        OfflineDevice getDevice();

        void setDevice(OfflineDevice offlineDevice);
    }
}