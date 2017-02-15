/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc;

/**
 * Marker to quickly find protocol components that used to store
 * configuration information using deprecated UserFile technology.
 * The latter technology has been removed from Connexo and replaced
 * by {@link com.energyict.mdc.protocol.api.DeviceMessageFile}s.
 * All components that were using UserFiles were affected by that
 * and part of their functionality now no longer works.
 * In fact, they will now all throw a RuntimeException,
 * sometimes wrapped in an IOException (because that is the
 * bespoke and legacy approach in protocols to report problems).
 * The intend is to re-enable the functionality later on
 * once product management has decided on the correct approach
 * within Connexo. Meanwhile, the affected components
 * are marked with this annotation to quickly find them.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-17 (13:48)
 */
public @interface StoresConfigurationInformationInSystemGlobalFile {
}