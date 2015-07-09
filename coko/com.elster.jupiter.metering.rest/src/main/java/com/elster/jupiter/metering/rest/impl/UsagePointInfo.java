package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.units.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;
import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class UsagePointInfo {
    private UsagePoint usagePoint;

    public long id;
    public ServiceKind serviceCategory;
    public long serviceLocationId;
    public String aliasName;
    public String description;
    public String mRID;
    public String name;
    public AmiBillingReadyKind amiBillingReady;
    public boolean checkBilling;
    public UsagePointConnectedKind connectionState;
    public Quantity estimatedLoad;
    public boolean grounded;
    public boolean isSdp;
    public boolean isVirtual;
    public boolean minimalUsageExpected;
    public Quantity nominalServiceVoltage;
    public String outageRegion;
    public PhaseCode phaseCode;
    public Quantity ratedCurrent;
    public Quantity ratedPower;
    public String readCycle;
    public String readRoute;
    public String serviceDeliveryRemark;
    public String servicePriority;
    public long version;
    public long createTime;
    public long modTime;
    public ServiceLocationInfo serviceLocation;

    public UsagePointInfo() {
    }

    public UsagePointInfo(UsagePoint usagePoint, Clock clock) {
        this.usagePoint = usagePoint;
        id = usagePoint.getId();
        mRID = usagePoint.getMRID();
        serviceCategory = usagePoint.getServiceCategory().getKind();
        serviceLocationId = usagePoint.getServiceLocationId();
        aliasName = usagePoint.getAliasName();
        description = usagePoint.getDescription();
        name = usagePoint.getName();
        isSdp = usagePoint.isSdp();
        isVirtual = usagePoint.isVirtual();
        outageRegion = usagePoint.getOutageRegion();
        readCycle = usagePoint.getReadCycle();
        readRoute = usagePoint.getReadRoute();
        servicePriority = usagePoint.getServicePriority();
        version = usagePoint.getVersion();
        createTime = usagePoint.getCreateDate().toEpochMilli();
        modTime = usagePoint.getModificationDate().toEpochMilli();
        Optional<? extends UsagePointDetail> detailHolder = usagePoint.getDetail(clock.instant());
        if (detailHolder.isPresent()) {
            UsagePointDetail detail = detailHolder.get();
            minimalUsageExpected = detail.isMinimalUsageExpected();
            amiBillingReady = detail.getAmiBillingReady();
            checkBilling = detail.isCheckBilling();
            connectionState = detail.getConnectionState();
            serviceDeliveryRemark = detail.getServiceDeliveryRemark();
            if (detail instanceof ElectricityDetail) {
                ElectricityDetail eDetail = (ElectricityDetail) detail;
                estimatedLoad = eDetail.getEstimatedLoad();
                grounded = eDetail.isGrounded();
                nominalServiceVoltage = eDetail.getNominalServiceVoltage();
                phaseCode = eDetail.getPhaseCode();
                ratedCurrent = eDetail.getRatedCurrent();
                ratedPower = eDetail.getRatedPower();
            }
        }
    }

    void addServiceLocationInfo() {
        if (usagePoint.getServiceLocation().isPresent()) {
            serviceLocation = new ServiceLocationInfo(usagePoint.getServiceLocation().get());
        }
    }
}