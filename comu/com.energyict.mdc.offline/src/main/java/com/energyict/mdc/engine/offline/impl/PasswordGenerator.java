/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.offline.impl;

import org.apache.commons.lang3.RandomStringUtils;

public class PasswordGenerator {

    public static String generatePassword() {
        return RandomStringUtils.random(10, 0, 0, true, true);
    }
}
