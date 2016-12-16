package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import java.time.Instant;

public class UsagePointBuilderImpl implements UsagePointBuilder {

    private DataModel dataModel;

    private String aliasName;
    private String description;
    private String name;
    private boolean isSdp;
    private boolean isVirtual;
    private String outageRegion;
    private String readRoute;
    private String servicePriority;
    private String serviceDeliveryRemark;
    private String serviceLocationString;
    private Instant installationTime;
    private long locationId;
    private SpatialCoordinates spatialCoordinates;

    private ServiceCategory serviceCategory;
    private ServiceLocation serviceLocation;

    public UsagePointBuilderImpl(DataModel dataModel, String name, Instant installationTime, ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
        this.name = name;
        this.installationTime = installationTime;
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
    public UsagePointBuilder withLocation(Location location) {
        this.locationId = location.getId();
        return this;
    }

    @Override
    public UsagePointBuilder withGeoCoordinates(SpatialCoordinates geoCoordinates) {
        this.spatialCoordinates = geoCoordinates;
        return this;
    }

    @Override
    public UsagePointBuilder withIsSdp(boolean isSdp) {
        this.isSdp = isSdp;
        return this;
    }

    @Override
    public UsagePointBuilder withIsVirtual(boolean isVirtual) {
        this.isVirtual = isVirtual;
        return this;
    }

    @Override
    public UsagePointBuilder withOutageRegion(String outageRegion) {
        this.outageRegion = outageRegion;
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
    public UsagePointBuilder withServiceLocation(ServiceLocation location) {
        this.serviceLocation = location;
        return this;
    }

    @Override
    public UsagePointBuilder withServiceLocationString(String serviceLocationString) {
        this.serviceLocationString = serviceLocationString;
        return this;
    }

    @Override
    public LocationBuilder newLocationBuilder() {
        return new LocationBuilderImpl(dataModel);
    }

    @Override
    public UsagePoint create() {
        UsagePointImpl usagePoint = this.build();
        usagePoint.doSave();
        return usagePoint;
    }

    @Override
    public UsagePoint validate() {
        UsagePointImpl usagePoint = this.build();
        Save.CREATE.validate(dataModel, usagePoint);
        return usagePoint;
    }

    private UsagePointImpl build(){
        UsagePointImpl usagePoint = dataModel.getInstance(UsagePointImpl.class).init(name, serviceCategory);
        usagePoint.setSdp(isSdp);
        usagePoint.setVirtual(isVirtual);
        usagePoint.setOutageRegion(outageRegion);
        usagePoint.setReadRoute(readRoute);
        usagePoint.setServicePriority(servicePriority);
        usagePoint.setServiceLocation(serviceLocation);
        usagePoint.setInstallationTime(installationTime);
        usagePoint.setServiceDeliveryRemark(serviceDeliveryRemark);
        usagePoint.setServiceLocationString(serviceLocationString);
        usagePoint.setSpatialCoordinates(spatialCoordinates);
        usagePoint.setLocation(locationId);
        UsagePointState initialState = dataModel.getInstance(UsagePointLifeCycleConfigurationService.class).getDefaultLifeCycle().getStates()
                .stream()
                .filter(UsagePointState::isInitial)
                .findFirst()
                .get();
        usagePoint.setState(initialState, installationTime);
        return usagePoint;
    }
}
