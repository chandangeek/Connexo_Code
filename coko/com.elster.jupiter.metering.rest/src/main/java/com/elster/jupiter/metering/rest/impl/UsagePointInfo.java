package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.rest.impl.ServiceLocationInfo;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.util.Optional;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class UsagePointInfo {

    private UsagePoint usagePoint;

    public long id;
    public ServiceKind serviceCategory;
    public long serviceLocationId;
    public String mRID;
    public String name;
    public boolean isSdp;
    public boolean isVirtual;
    public String outageRegion;
    public String readRoute;
    public String serviceDeliveryRemark;
    public String servicePriority;
    public long installationTime;

    public boolean grounded;
    public Quantity estimatedLoad;
    public Quantity nominalServiceVoltage;
    public PhaseCode phaseCode;
    public Quantity ratedCurrent;
    public Quantity ratedPower;
    public Boolean limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public Boolean interruptible;
    public Quantity pressure;
    public Quantity physicalCapacity;
    public YesNoAnswer bypass;
    public BypassStatus bypassStatus;
    public YesNoAnswer valve;
    public YesNoAnswer capped;
    public YesNoAnswer clamped;
    public YesNoAnswer collar;

    public long version;
    public long createTime;
    public long modTime;
    public ServiceLocationInfo serviceLocation;
    public long openIssues;

    public UsagePointInfo() {
    }

    public UsagePointInfo(UsagePoint usagePoint, Clock clock) {
        this.usagePoint = usagePoint;
        id = usagePoint.getId();
        mRID = usagePoint.getMRID();
        serviceCategory = usagePoint.getServiceCategory().getKind();
        serviceLocationId = usagePoint.getServiceLocationId();
        name = usagePoint.getName();
        isSdp = usagePoint.isSdp();
        isVirtual = usagePoint.isVirtual();
        outageRegion = usagePoint.getOutageRegion();
        readRoute = usagePoint.getReadRoute();
        servicePriority = usagePoint.getServicePriority();
        serviceDeliveryRemark = usagePoint.getServiceDeliveryRemark();
        installationTime = usagePoint.getInstallationTime().getEpochSecond();
        version = usagePoint.getVersion();
        createTime = usagePoint.getCreateDate().toEpochMilli();
        modTime = usagePoint.getModificationDate().toEpochMilli();
        Optional<? extends UsagePointDetail> detailHolder = usagePoint.getDetail(clock.instant());
        if (detailHolder.isPresent()) {
            UsagePointDetail detail = detailHolder.get();
            collar = detail.isCollarInstalled();
            if (detail instanceof ElectricityDetail) {
                ElectricityDetail eDetail = (ElectricityDetail) detail;
                estimatedLoad = eDetail.getEstimatedLoad();
                grounded = eDetail.isGrounded();
                nominalServiceVoltage = eDetail.getNominalServiceVoltage();
                phaseCode = eDetail.getPhaseCode();
                ratedCurrent = eDetail.getRatedCurrent();
                ratedPower = eDetail.getRatedPower();
                limiter = eDetail.isLimiter();
                loadLimiterType = eDetail.getLoadLimiterType();
                loadLimit = eDetail.getLoadLimit();
                interruptible = eDetail.isInterruptible();
            }
            else if (detail instanceof WaterDetail) {
                WaterDetail wDetail = (WaterDetail) detail;
                grounded = wDetail.isGrounded();
                physicalCapacity = wDetail.getPhysicalCapacity();
                pressure = wDetail.getPressure();
                limiter = wDetail.isLimiter();
                loadLimiterType = wDetail.getLoadLimiterType();
                loadLimit = wDetail.getLoadLimit();
                bypass = wDetail.isBypassInstalled();
                bypassStatus =wDetail.getBypassStatus();
                valve = wDetail.isValveInstalled();
                capped = wDetail.isCapped();
                clamped = wDetail.isClamped();
            }
            else if (detail instanceof GasDetail) {
                GasDetail gDetail = (GasDetail) detail;
                grounded = gDetail.isGrounded();
                physicalCapacity = gDetail.getPhysicalCapacity();
                pressure = gDetail.getPressure();
                limiter = gDetail.isLimiter();
                loadLimiterType = gDetail.getLoadLimiterType();
                loadLimit = gDetail.getLoadLimit();
                bypass = gDetail.isBypassInstalled();
                bypassStatus =gDetail.getBypassStatus();
                valve = gDetail.isValveInstalled();
                capped = gDetail.isCapped();
                clamped = gDetail.isClamped();
                interruptible = gDetail.isInterruptible();
            }
            else if (detail instanceof HeatDetail) {
                HeatDetail hDetail = (HeatDetail) detail;
                physicalCapacity = hDetail.getPhysicalCapacity();
                pressure = hDetail.getPressure();
                bypass = hDetail.isBypassInstalled();
                bypassStatus =hDetail.getBypassStatus();
                valve = hDetail.isValveInstalled();
            }
        }
    }

    public void addServiceLocationInfo() {
        usagePoint.getServiceLocation().ifPresent(location -> serviceLocation = new ServiceLocationInfo(location));
    }

}