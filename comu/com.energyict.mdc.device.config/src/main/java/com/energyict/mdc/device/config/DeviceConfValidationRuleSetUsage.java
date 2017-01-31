/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.validation.ValidationRuleSet;

import aQute.bnd.annotation.ProviderType;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 17/06/14
 * Time: 8:58
 * To change this template use File | Settings | File Templates.
 */
@ProviderType
public interface DeviceConfValidationRuleSetUsage {

    DeviceConfiguration getDeviceConfiguration();

    ValidationRuleSet getValidationRuleSet();

    void delete();

    void save();

}