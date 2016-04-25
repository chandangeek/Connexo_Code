package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.imports.impl.FileImportRecordWithCustomProperties;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UsagePointImportRecord extends FileImportRecordWithCustomProperties {
    //General
    private String mRID;
    private String serviceKind;
    private String name;
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

    public UsagePointImportRecord() {
    }

    public UsagePointImportRecord(long lineNumber) {
        super(lineNumber);
    }

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
        this.serviceKind = serviceKind != null ? serviceKind.toUpperCase() : null;
    }

    public List<String> getLocation() {
        return location;
    }

    public void addLocation(String location) {
        this.location.add(location);
    }

    public List<String> getGeoCoordinates() {
        return Arrays.asList(latitude, longitude, elevation);
    }

    public void setLatitude(String latitude){
        this.latitude = latitude;
    }

    public void setLongitude(String longitude){
        this.longitude = longitude;
    }

    public void setElevation(String elevation){
        this.elevation = elevation;
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
        this.phaseCode = phaseCode!=null?phaseCode.toUpperCase():phaseCode;
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
}
