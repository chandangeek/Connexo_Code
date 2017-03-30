/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

/**
 * Models a level of security for a physical device
 * and the {@link PropertySpec}s
 * that the Device will require to be specified
 * before decrypting the data that is
 * secured by this level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (17:17)
 */
public interface EncryptionDeviceAccessLevel extends DeviceAccessLevel {
}