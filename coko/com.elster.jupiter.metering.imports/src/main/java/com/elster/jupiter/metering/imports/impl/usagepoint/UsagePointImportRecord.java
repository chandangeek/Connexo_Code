package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.imports.impl.CustomPropertySetRecord;
import com.elster.jupiter.metering.imports.impl.FileImportRecord;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UsagePointImportRecord extends FileImportRecord {
    //General
    private String usagePointIdentifier;
    private String serviceKind;
    private boolean isSdp;
    private boolean isVirtual;
    private Instant installationTime;
    private String outageRegion;
    private String readRoute;
    private String servicePriority;
    private String serviceDeliveryRemark;
    private List<String> location = new ArrayList<>();
    private String latitude;
    private String longitude;
    private String elevation;

    //Technical
    private YesNoAnswer collar;
    private YesNoAnswer grounded;
    private Quantity pressure;
    private Quantity physicalCapacity;
    private YesNoAnswer limiter;
    private String loadLimiterType;
    private Quantity loadLimit;
    private YesNoAnswer bypass;
    private String bypassStatus;
    private YesNoAnswer valve;
    private YesNoAnswer capped;
    private YesNoAnswer clamped;
    private YesNoAnswer interruptible;
    private String phaseCode;
    private Quantity ratedPower;
    private Quantity ratedCurrent;
    private Quantity estimatedLoad;
    private Quantity nominalVoltage;
    private boolean allowUpdate;

    public String metrologyConfiguration;
    public Instant metrologyConfigurationApplyTime;
    private Map<CustomPropertySet, CustomPropertySetRecord> customPropertySets;
    private Pair<MeterRole, Meter> meterRoleAndMeter;

    public UsagePointImportRecord() {
    }

    public UsagePointImportRecord(long lineNumber) {
        super(lineNumber);
    }

    public Optional<String> getUsagePointIdentifier() {
        return Optional.ofNullable(usagePointIdentifier);
    }

    public void setUsagePointIdentifier(String usagePointIdentifier) {
        this.usagePointIdentifier = usagePointIdentifier;
    }

    public Optional<String> getServiceKind() {
        return Optional.ofNullable(serviceKind);
    }

    public void setServiceKind(String serviceKind) {
        this.serviceKind = serviceKind != null ? serviceKind.toUpperCase() : null;
    }

    public List<String> getLocation() {
        return Collections.unmodifiableList(location);
    }

    public void addLocation(String location) {
        this.location.add(location);
    }

    public List<String> getGeoCoordinates() {
        return Arrays.asList(latitude, longitude, elevation);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public String getOutageRegion() {
        return outageRegion;
    }

    public void setOutageRegion(String outageRegion) {
        this.outageRegion = outageRegion;
    }

    public String getReadRoute() {
        return readRoute;
    }

    public void setReadRoute(String readRoute) {
        this.readRoute = readRoute;
    }

    public String getServicePriority() {
        return servicePriority;
    }

    public void setServicePriority(String servicePriority) {
        this.servicePriority = servicePriority;
    }

    public String getServiceDeliveryRemark() {
        return serviceDeliveryRemark;
    }

    public void setServiceDeliveryRemark(String serviceDeliveryRemark) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
    }

    public Optional<YesNoAnswer> isCollarInstalled() {
        return Optional.ofNullable(collar);
    }

    public void setCollar(YesNoAnswer collar) {
        this.collar = collar;
    }

    public Optional<YesNoAnswer> isGrounded() {
        return Optional.ofNullable(grounded);
    }

    public void setGrounded(YesNoAnswer grounded) {
        this.grounded = grounded;
    }

    public Optional<YesNoAnswer> isInterruptible() {
        return Optional.ofNullable(interruptible);
    }

    public void setInterruptible(YesNoAnswer interruptible) {
        this.interruptible = interruptible;
    }

    public Optional<PhaseCode> getPhaseCode() {
        return Arrays.stream(PhaseCode.values()).filter(p -> p.name().equalsIgnoreCase(phaseCode)).findFirst();
    }

    public void setPhaseCode(String phaseCode) {
        this.phaseCode = phaseCode != null ? phaseCode.toUpperCase() : phaseCode;
    }

    public Optional<YesNoAnswer> isLimiterInstalled() {
        return Optional.ofNullable(limiter);
    }

    public void setLimiter(YesNoAnswer limiter) {
        this.limiter = limiter;
    }

    public Optional<String> getLoadLimiterType() {
        return Optional.ofNullable(loadLimiterType);
    }

    public void setLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
    }

    public Optional<Quantity> getLoadLimit() {
        return Optional.ofNullable(loadLimit);
    }

    public void setLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
    }

    public Optional<YesNoAnswer> isBypassInstalled() {
        return Optional.ofNullable(bypass);
    }

    public void setBypass(YesNoAnswer bypass) {
        this.bypass = bypass;
    }

    public Optional<BypassStatus> getBypassStatus() {
        return Arrays.stream(BypassStatus.values()).filter(p -> p.name().equalsIgnoreCase(bypassStatus)).findFirst();
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
        return Optional.ofNullable(pressure);
    }

    public void setPressure(Quantity pressure) {
        this.pressure = pressure;
    }

    public Optional<Quantity> getPhysicalCapacity() {
        return Optional.ofNullable(physicalCapacity);
    }

    public void setPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
    }

    public Optional<Quantity> getRatedPower() {
        return Optional.ofNullable(ratedPower);
    }

    public void setRatedPower(Quantity ratedPower) {
        this.ratedPower = ratedPower;
    }

    public Optional<Quantity> getRatedCurrent() {
        return Optional.ofNullable(ratedCurrent);
    }

    public void setRatedCurrent(Quantity ratedCurrent) {
        this.ratedCurrent = ratedCurrent;
    }

    public Optional<Quantity> getEstimatedLoad() {
        return Optional.ofNullable(estimatedLoad);
    }

    public void setEstimatedLoad(Quantity estimatedLoad) {
        this.estimatedLoad = estimatedLoad;
    }

    public Optional<Quantity> getNominalVoltage() {
        return Optional.ofNullable(nominalVoltage);
    }

    public void setNominalVoltage(Quantity nominalVoltage) {
        this.nominalVoltage = nominalVoltage;
    }

    public Optional<Instant> getInstallationTime() {
        return Optional.ofNullable(installationTime);
    }

    public void setInstallationTime(Instant installationTime) {
        this.installationTime = installationTime;
    }

    public boolean isAllowUpdate() {
        return allowUpdate;
    }

    public void setAllowUpdate(Boolean allowUpdate) {
        this.allowUpdate = allowUpdate != null && allowUpdate;
    }

    public void setSdp(boolean sdp) {
        isSdp = sdp;
    }

    public void setVirtual(boolean virtual) {
        isVirtual = virtual;
    }

    public boolean isSdp() {
        return isSdp;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public Optional<String> getMetrologyConfiguration() {
        return Optional.ofNullable(metrologyConfiguration);
    }

    public void setMetrologyConfiguration(String metrologyConfiguration) {
        this.metrologyConfiguration = metrologyConfiguration;
    }

    public Optional<Instant> getMetrologyConfigurationApplyTime() {
        return Optional.ofNullable(metrologyConfigurationApplyTime);
    }

    public void setMetrologyConfigurationApplyTime(Instant metrologyConfigurationApplyTime) {
        this.metrologyConfigurationApplyTime = metrologyConfigurationApplyTime;
    }

    public Map<CustomPropertySet, CustomPropertySetRecord> getCustomPropertySets() {
        return customPropertySets;
    }

    public void setCustomPropertySets(Map<CustomPropertySet, CustomPropertySetRecord> customPropertySets) {
        this.customPropertySets = customPropertySets;
    }

    public Pair<MeterRole, Meter> getMeterRoles() {
        return meterRoleAndMeter;
    }

    public void setMeterRoles(Pair<MeterRole, Meter> meterRoleAndMeter) {
        this.meterRoleAndMeter = meterRoleAndMeter;
    }
}
