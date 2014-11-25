package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 6/3/14
 * Time: 10:45 AM
 */
public class SecuritySetFinder implements CanFindByLongPrimaryKey<SecurityPropertySet> {

    private final DeviceConfigurationService deviceConfigurationService;

    public SecuritySetFinder(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.SECURITY_SET;
    }

    @Override
    public Class<SecurityPropertySet> valueDomain() {
        return SecurityPropertySet.class;
    }

    @Override
    public Optional<SecurityPropertySet> findByPrimaryKey(long id) {
        return this.deviceConfigurationService.findSecurityPropertySet(id);
    }

}