package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
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
    private String readRoute;
    private String servicePriority;
    private String serviceDeliveryRemark;
    private String serviceLocationString;
    private Instant installationTime;
    private long locationId;
    private GeoCoordinates geoCoordinates;

    private ServiceCategory serviceCategory;
    private ServiceLocation serviceLocation;
    private MetrologyConfiguration metrologyConfiguration;

    public UsagePointBuilderImpl(DataModel dataModel, String mRID, Instant installationTime, ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
        this.mRID = mRID;
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
    public UsagePointBuilder withMRID(String mRID) {
        this.mRID = mRID;
        return this;
    }

    @Override
    public UsagePointBuilder withLocation(Location location){
        this.locationId = location.getId();
        return this;
    }

    @Override
    public UsagePointBuilder withGeoCoordinates(GeoCoordinates geoCoordinates){
        this.geoCoordinates = geoCoordinates;
        return this;
    }

    @Override
    public UsagePointBuilder withName(String name) {
        this.name = name;
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
    public UsagePointBuilder withMetrologyConfiguration(MetrologyConfiguration metrologyConfiguration) {
        this.metrologyConfiguration = metrologyConfiguration;
        return this;
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
        Save.CREATE.validate(dataModel,usagePoint);
        return usagePoint;
    }

    private UsagePointImpl build(){
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
        usagePoint.setServiceLocationString(serviceLocationString);
        usagePoint.setGeoCoordinates(geoCoordinates);
        usagePoint.setLocation(locationId);
        if (metrologyConfiguration != null) {
            usagePoint.apply(metrologyConfiguration);
        }
        return usagePoint;
    }
}
