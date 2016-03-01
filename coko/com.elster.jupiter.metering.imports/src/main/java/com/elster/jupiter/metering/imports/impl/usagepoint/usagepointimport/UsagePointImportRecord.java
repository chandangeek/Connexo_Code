package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportRecord;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class UsagePointImportRecord extends FileImportRecord {

    //General
    private String mRID;
    private String serviceKind;
    private Long serviceLocationID;
    private String serviceLocationString;
    private String name;
    private ZonedDateTime installationTime;
    private String outageRegion;
    private String readRoute;
    private String servicePriority;
    private String serviceDeliveryRemark;

    //Technical

    private YesNoAnswer collar;
    private Boolean grounded;

    private BigDecimal pressureValue;
    private Integer pressureMultiplier;
    private String pressureUnit;

    private BigDecimal physicalCapacityValue;
    private Integer physicalCapacityMultiplier;
    private String physicalCapacityUnit;

    private Boolean limiter;
    private String loadLimiterType;
    private BigDecimal loadLimitValue;
    private Integer loadLimitMultiplier;
    private String loadLimitUnit;

    private YesNoAnswer bypass;
    private String bypassStatus;

    private YesNoAnswer valve;
    private YesNoAnswer capped;
    private YesNoAnswer clamped;
    private Boolean interruptible;

    private String phaseCode;

    private BigDecimal ratedPowerValue;
    private Integer ratedPowerMultiplier;
    private String ratedPowerUnit;

    private BigDecimal ratedCurrentValue;
    private Integer ratedCurrentMultiplier;
    private String ratedCurrentUnit;

    private BigDecimal estimatedLoadValue;
    private Integer estimatedLoadMultiplier;
    private String estimatedLoadUnit;

    private BigDecimal nominalVoltageValue;
    private Integer nominalVoltageMultiplier;
    private String nominalVoltageUnit;


    private Boolean allowUpdate;

    private Map<CustomPropertySet, CustomPropertySetValues> customPropertySetValues;


    public Optional<String> getmRID() {
        return Optional.ofNullable(mRID);
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public Optional<String> getServiceKind() {
        return Optional.ofNullable(serviceKind);
    }

    public void setServiceKind(String serviceKind) {
        this.serviceKind = serviceKind.toUpperCase();
    }

    public Optional<Long> getServiceLocationID() {
        return Optional.ofNullable(serviceLocationID);
    }

    public void setServiceLocationID(Long serviceLocationID) {
        this.serviceLocationID = serviceLocationID;
    }

    public Optional<String> getServiceLocationString() {
        return Optional.ofNullable(serviceLocationString);
    }

    public void setServiceLocationString(String serviceLocationString) {
        this.serviceLocationString = serviceLocationString;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getOutageRegion() {
        return Optional.ofNullable(outageRegion);
    }

    public void setOutageRegion(String outageregion) {
        this.outageRegion = outageregion;
    }

    public Optional<String> getReadRoute() {
        return Optional.ofNullable(readRoute);
    }

    public void setReadRoute(String readroute) {
        this.readRoute = readroute;
    }

    public Optional<String> getServicePriority() {
        return Optional.ofNullable(servicePriority);
    }

    public void setServicePriority(String servicePriority) {
        this.servicePriority = servicePriority;
    }

    public Optional<String> getServiceDeliveryRemark() {
        return Optional.ofNullable(serviceDeliveryRemark);
    }

    public void setServiceDeliveryRemark(String serviceLocationString) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
    }

    public Optional<YesNoAnswer> isCollarInstalled() {
        return Optional.ofNullable(collar);
    }

    public void setCollar(YesNoAnswer collar) {
        this.collar = collar;
    }

    public Optional<Boolean> isGrounded() {
        return Optional.ofNullable(grounded);
    }

    public void setGrounded(Boolean grounded) {
        this.grounded = grounded;
    }

    public Optional<Boolean> isInterruptible() {
        return Optional.ofNullable(interruptible);
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }

    public Optional<PhaseCode> getPhaseCode() {
        return Arrays.stream(PhaseCode.values()).filter(p -> p.name().equals(phaseCode)).findFirst();
    }

    public void setPhaseCode(String phaseCode) {
        this.phaseCode = phaseCode.toUpperCase();
    }

    public Optional<Boolean> isLimiterInstalled() {
        return Optional.ofNullable(limiter);
    }

    public void setLimiter(Boolean limiter) {
        this.limiter = limiter;
    }

    public Optional<String> getLoadLimiterType() {
        return Optional.ofNullable(loadLimiterType);
    }

    public void setLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
    }

    public Optional<Quantity> getLoadLimit() {
        return Optional.ofNullable(getQuantity(loadLimitValue, loadLimitMultiplier, loadLimitUnit));
    }

    public void setLoadLimitValue(BigDecimal loadLimitValue) {
        this.loadLimitValue = loadLimitValue;
    }

    public void setLoadLimitMultiplier(Integer loadLimitMultiplier) {
        this.loadLimitMultiplier = loadLimitMultiplier;
    }

    public void setLoadLimitUnit(String loadLimitUnit) {
        this.loadLimitUnit = loadLimitUnit;
    }

    public Optional<YesNoAnswer> isBypassInstalled() {
        return Optional.ofNullable(bypass);
    }

    public void setBypass(YesNoAnswer bypass) {
        this.bypass = bypass;
    }

    public Optional<BypassStatus> getBypassStatus() {
        return Arrays.stream(BypassStatus.values()).filter(p -> p.name().equals(bypassStatus)).findFirst();
    }

    public void setBypassStatus(String bypassStatus) {
        this.bypassStatus = bypassStatus;
    }

    public Optional<YesNoAnswer> isValveInstalled() {
        return Optional.ofNullable(valve);
    }

    public void setValve(YesNoAnswer valve) {
        this.valve = valve;
    }

    public Optional<YesNoAnswer> isCapped() {
        return Optional.ofNullable(capped);
    }

    public void setCap(YesNoAnswer capped) {
        this.capped = capped;
    }

    public Optional<YesNoAnswer> isClamped() {
        return Optional.ofNullable(clamped);
    }

    public void setClamp(YesNoAnswer clamped) {
        this.clamped = clamped;
    }

    public Optional<Quantity> getPressure() {
        return Optional.ofNullable(getQuantity(pressureValue, pressureMultiplier, pressureUnit));
    }

    public void setPressureValue(BigDecimal pressureValue) {
        this.pressureValue = pressureValue;
    }

    public void setPressureMultiplier(Integer pressureMultiplier) {
        this.pressureMultiplier = pressureMultiplier;
    }

    public void setPressureUnit(String pressureUnit) {
        this.pressureUnit = pressureUnit;
    }

    public Optional<Quantity> getPhysicalCapacity() {
        return Optional.ofNullable(getQuantity(physicalCapacityValue, physicalCapacityMultiplier, physicalCapacityUnit));
    }

    public void setPhysicalCapacityValue(BigDecimal physicalCapacityValue) {
        this.physicalCapacityValue = physicalCapacityValue;
    }

    public void setPhysicalCapacityMultiplier(Integer physicalCapacityMultiplier) {
        this.physicalCapacityMultiplier = physicalCapacityMultiplier;
    }

    public void setPhysicalCapacityUnit(String physicalCapacityUnit) {
        this.physicalCapacityUnit = physicalCapacityUnit;
    }

    public Optional<Quantity> getRatedPower() {
        return Optional.ofNullable(getQuantity(ratedPowerValue, ratedPowerMultiplier, ratedPowerUnit));
    }

    public void setRatedPowerValue(BigDecimal ratedPowerValue) {
        this.ratedPowerValue = ratedPowerValue;
    }

    public void setRatedPowerMultiplier(Integer ratedPowerMultiplier) {
        this.ratedPowerMultiplier = ratedPowerMultiplier;
    }

    public void setRatedPowerUnit(String ratedPowerUnit) {
        this.ratedPowerUnit = ratedPowerUnit;
    }

    public Optional<Quantity> getRatedCurrent() {
        return Optional.ofNullable(getQuantity(ratedCurrentValue, ratedCurrentMultiplier, ratedCurrentUnit));
    }

    public void setRatedCurrentValue(BigDecimal ratedCurrentValue) {
        this.ratedCurrentValue = ratedCurrentValue;
    }

    public void setRatedCurrentMultiplier(Integer ratedCurrentMultiplier) {
        this.ratedCurrentMultiplier = ratedCurrentMultiplier;
    }

    public void setRatedCurrentUnit(String ratedCurrentUnit) {
        this.ratedCurrentUnit = ratedCurrentUnit;
    }

    public Optional<Quantity> getEstimatedLoad() {
        return Optional.ofNullable(getQuantity(estimatedLoadValue, estimatedLoadMultiplier, estimatedLoadUnit));
    }

    public void setEstimatedLoadValue(BigDecimal estimatedLoadValue) {
        this.estimatedLoadValue = estimatedLoadValue;
    }

    public void setEstimatedLoadMultiplier(Integer estimatedLoadMultiplier) {
        this.estimatedLoadMultiplier = estimatedLoadMultiplier;
    }

    public void setEstimatedLoadUnit(String estimatedLoadUnit) {
        this.estimatedLoadUnit = estimatedLoadUnit;
    }

    public Optional<Quantity> getNominalVoltage() {
        return Optional.ofNullable(getQuantity(nominalVoltageValue, nominalVoltageMultiplier, nominalVoltageUnit));
    }

    public void setNominalVoltageValue(BigDecimal nominalVoltageValue) {
        this.nominalVoltageValue = nominalVoltageValue;
    }

    public void setNominalVoltageMultiplier(Integer nominalVoltageMultiplier) {
        this.nominalVoltageMultiplier = nominalVoltageMultiplier;
    }


    public void setNominalVoltageUnit(String nominalVoltageUnit) {
        this.nominalVoltageUnit = nominalVoltageUnit;
    }

    public Map<CustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues() {
        return customPropertySetValues;
    }

    public void setCustomPropertySetValues(Map<CustomPropertySet, CustomPropertySetValues> customPropertySetValues) {
        this.customPropertySetValues = customPropertySetValues;
    }

    public Optional<Instant> getInstallationTime() {
        return Optional.ofNullable(installationTime.toInstant());
    }

    public void setInstallationTime(ZonedDateTime installationTime) {
        this.installationTime = installationTime;
    }

    public boolean isAllowUpdate() {
        return allowUpdate;
    }

    public void setAllowUpdate(Boolean allowUpdate) {
        this.allowUpdate = allowUpdate != null && allowUpdate;
    }

    private Quantity getQuantity(BigDecimal value, Integer multipler, String unit) {
        if (unit != null && value != null) {
            if (multipler != null) {
                return Quantity.create(value, multipler, unit);
            } else {
                return Quantity.create(value, unit);
            }
        } else {
            return null;
        }
    }
}
