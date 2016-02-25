package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;

public class UsagePointBuilderImpl implements UsagePointBuilder {

    private DataModel dataModel;

    private String aliasName;
    private String description;
    private String mRID;
    private String name;
    private boolean isSdp;
    private boolean isVirtual;
    private String outageRegion;
    private String readCycle;
    private String readRoute;
    private String servicePriority;
    private String serviceDeliveryRemark;
    private Instant installationTime;

    private ServiceCategory serviceCategory;
    private ServiceLocation serviceLocation;

    public UsagePointBuilderImpl(DataModel dataModel, String mRID, ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
        this.mRID = mRID;
        this.dataModel = dataModel;
    }

    @Override
    public UsagePointBuilder withAliasName(String aliasName) {
        this.aliasName = aliasName;
        return this;
    }

    @Override
    public UsagePointBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public UsagePointBuilder withMRID(String mRID) {
        this.mRID = mRID;
        return this;
    }

    @Override
    public UsagePointBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public UsagePointBuilder withIsSdp(Boolean isSdp) {
        this.isSdp = isSdp;
        return this;
    }

    @Override
    public UsagePointBuilder withIsVirtual(Boolean isVirtual) {
        this.isVirtual = isVirtual;
        return this;
    }

    @Override
    public UsagePointBuilder withOutageRegion(String outageRegion) {
        this.outageRegion = outageRegion;
        return this;
    }

    @Override
    public UsagePointBuilder withReadCycle(String readCycle) {
        this.readCycle = readCycle;
        return this;
    }

    @Override
    public UsagePointBuilder withReadRoute(String readRoute) {
        this.readRoute = readRoute;
        return this;
    }

    @Override
    public UsagePointBuilder withServicePriority(String servicePriority) {
        this.servicePriority = servicePriority;
        return this;
    }

    @Override
    public UsagePointBuilder withServiceDeliveryRemark(String serviceDeliveryRemark) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
        return this;
    }

    @Override
    public UsagePointBuilder setServiceLocation(ServiceLocation location) {
        this.serviceLocation = location;
        return this;
    }

    @Override
    public UsagePointBuilder withInstallationTime(Instant installationTime) {
        this.installationTime = installationTime;
        return this;
    }

    @Override
    public UsagePoint create() {
        UsagePointImpl usagePoint = dataModel.getInstance(UsagePointImpl.class).init(mRID, serviceCategory);
        usagePoint.setName(name);
        usagePoint.setSdp(isSdp);
        usagePoint.setVirtual(isVirtual);
        usagePoint.setOutageRegion(outageRegion);
        usagePoint.setReadRoute(readRoute);
        usagePoint.setServicePriority(servicePriority);
        usagePoint.setServiceLocation(serviceLocation);
        usagePoint.setInstallationTime(installationTime);
        usagePoint.setServiceDeliveryRemark(serviceDeliveryRemark);
        usagePoint.doSave();
        return usagePoint;
    }

    @Override
    public UsagePoint validate() {
        UsagePointImpl usagePoint = dataModel.getInstance(UsagePointImpl.class).init(mRID, serviceCategory);
        usagePoint.setName(name);
        usagePoint.setSdp(isSdp);
        usagePoint.setVirtual(isVirtual);
        usagePoint.setOutageRegion(outageRegion);
        usagePoint.setReadRoute(readRoute);
        usagePoint.setServicePriority(servicePriority);
        usagePoint.setServiceLocation(serviceLocation);
        usagePoint.setInstallationTime(installationTime);
        usagePoint.setServiceDeliveryRemark(serviceDeliveryRemark);
        Save.CREATE.validate(dataModel,usagePoint);
        return usagePoint;
    }
}
