/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.ServiceLocationBuilder;
import com.elster.jupiter.orm.DataModel;

class ServiceLocationBuilderImpl implements ServiceLocationBuilder {

    private final DataModel dataModel;
    private final ServiceLocationImpl underConstruction;
    private boolean built;

    ServiceLocationBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        this.underConstruction = dataModel.getInstance(ServiceLocationImpl.class);
    }

    @Override
    public ServiceLocation create() {
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
    public ServiceLocationBuilder setAlias(String alias) {
        underConstruction.setAliasName(alias);
        return this;
    }

    @Override
    public ServiceLocationBuilder setDescription(String description) {
        underConstruction.setDescription(description);
        return this;
    }

    @Override
    public ServiceLocationBuilder setMRID(String mRID) {
        underConstruction.setMRID(mRID);
        return this;
    }

    @Override
    public ServiceLocationBuilder setName(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public ServiceLocationBuilder setDirection(String direction) {
        underConstruction.setDirection(direction);
        return this;
    }

    @Override
    public ServiceLocationBuilder setElectronicAddress(ElectronicAddress electronicAddress) {
        underConstruction.setElectronicAddress(electronicAddress);
        return this;
    }

    @Override
    public ServiceLocationBuilder setGeoInfoReference(String geoInfoReference) {
        underConstruction.setGeoInfoReference(geoInfoReference);
        return this;
    }

    @Override
    public ServiceLocationBuilder setMainAddress(StreetAddress mainAddress) {
        underConstruction.setMainAddress(mainAddress);
        return this;
    }

    @Override
    public ServiceLocationBuilder setPhone1(TelephoneNumber phone1) {
        underConstruction.setPhone1(phone1);
        return this;
    }

    @Override
    public ServiceLocationBuilder setPhone2(TelephoneNumber phone2) {
        underConstruction.setPhone2(phone2);
        return this;
    }

    @Override
    public ServiceLocationBuilder setSecondaryAddress(StreetAddress secondaryAddress) {
        underConstruction.setSecondaryAddress(secondaryAddress);
        return this;
    }

    @Override
    public ServiceLocationBuilder setStatus(Status status) {
        underConstruction.setStatus(status);
        return this;
    }

    @Override
    public ServiceLocationBuilder setType(String type) {
        underConstruction.setType(type);
        return this;
    }

    @Override
    public ServiceLocationBuilder setAccessMethod(String accessMethod) {
        underConstruction.setAccessMethod(accessMethod);
        return this;
    }

    @Override
    public ServiceLocationBuilder setNeedsInspection(boolean needsInspection) {
        underConstruction.setNeedsInspection(needsInspection);
        return this;
    }

    @Override
    public ServiceLocationBuilder setSiteAccessProblem(String siteAccessProblem) {
        underConstruction.setSiteAccessProblem(siteAccessProblem);
        return this;
    }
}
