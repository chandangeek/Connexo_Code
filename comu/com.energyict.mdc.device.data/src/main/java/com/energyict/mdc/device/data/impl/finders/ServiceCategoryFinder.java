package com.energyict.mdc.device.data.impl.finders;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;

import java.util.Optional;

public class ServiceCategoryFinder implements CanFindByLongPrimaryKey<ServiceCategory> {

    private final MeteringService meteringService;

    public ServiceCategoryFinder(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.SERVICE_CATEGORY;
    }

    @Override
    public Class<ServiceCategory> valueDomain() {
        return ServiceCategory.class;
    }

    @Override
    public Optional<ServiceCategory> findByPrimaryKey(long id) {
        if (id < ServiceKind.values().length) {
            ServiceKind serviceKind = ServiceKind.values()[Long.valueOf(id).intValue() - 1];
            return meteringService.getServiceCategory(serviceKind);
        }
        return Optional.empty();
    }
}
