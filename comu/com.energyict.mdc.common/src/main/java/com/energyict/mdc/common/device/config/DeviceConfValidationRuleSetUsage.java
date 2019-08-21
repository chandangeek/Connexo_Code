/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.validation.ValidationRuleSet;

import aQute.bnd.annotation.ConsumerType;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 17/06/14
 * Time: 8:58
 * To change this template use File | Settings | File Templates.
 */
@ConsumerType
public interface DeviceConfValidationRuleSetUsage {

    DeviceConfiguration getDeviceConfiguration();

    ValidationRuleSet getValidationRuleSet();

    boolean isRuleSetActive();

    void setRuleSetStatus(boolean active);

    void delete();

    void save();

}