/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.common;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.security.SecurityPropertySet;
import com.energyict.protocolimplv2.messages.validators.KeyMessageChangeValidator;

/**
 * @author Kristof Hennebel
 * @since 12.07.17 - 10:10
 */
public class CryptoKeyMessageChangeValidator extends KeyMessageChangeValidator {

    @Override
    public void validateSecurityKeyLength(SecurityPropertySet securityPropertySet, String propertyName, String key, DeviceMessage deviceMessage) {
//        new SecurityPropertyValueParser().parseSecurityPropertyValue(propertyName, key);
    }

}