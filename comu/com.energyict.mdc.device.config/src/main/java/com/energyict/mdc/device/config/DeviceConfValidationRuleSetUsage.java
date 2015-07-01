package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.validation.ValidationRuleSet;

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