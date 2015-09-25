package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;

import javax.inject.Provider;

public class UsagePointBuilderImpl implements UsagePointBuilder {

    private final UsagePointImpl underConstruction;

    private boolean built;

    public UsagePointBuilderImpl(Provider<UsagePointImpl> provider, String mRID, ServiceCategory serviceCategory) {
        underConstruction = provider.get().init(mRID, serviceCategory);
    }

    @Override
    public UsagePoint create() {
        if (built) {
            throw new IllegalStateException();
        }
        underConstruction.doSave();
        try {
            return underConstruction;
        } finally {
            built = true;
        }
    }

    @Override
    public UsagePointBuilder setAliasName(String aliasName) {
        underConstruction.setAliasName(aliasName);
        return this;
    }

    @Override
    public UsagePointBuilder setDescription(String description) {
        underConstruction.setDescription(description);
        return this;
    }

    @Override
    public UsagePointBuilder setMRID(String mRID) {
        underConstruction.setMRID(mRID);
        return this;
    }

    @Override
    public UsagePointBuilder setName(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public UsagePointBuilder setIsSdp(boolean isSdp) {
        underConstruction.setSdp(isSdp);
        return this;
    }

    @Override
    public UsagePointBuilder setIsVirtual(boolean isVirtual) {
        underConstruction.setVirtual(isVirtual);
        return this;
    }

    @Override
    public UsagePointBuilder setOutageRegion(String outageRegion) {
        underConstruction.setOutageRegion(outageRegion);
        return this;
    }

    @Override
    public UsagePointBuilder setReadCycle(String readCycle) {
        underConstruction.setReadCycle(readCycle);
        return this;
    }

    @Override
    public UsagePointBuilder setReadRoute(String readRoute) {
        underConstruction.setReadRoute(readRoute);
        return this;
    }

    @Override
    public UsagePointBuilder setServicePriority(String servicePriority) {
        underConstruction.setServicePriority(servicePriority);
        return this;
    }

    @Override
    public UsagePointBuilder setServiceLocation(ServiceLocation serviceLocation) {
        underConstruction.setServiceLocation(serviceLocation);
        return this;
    }
}
