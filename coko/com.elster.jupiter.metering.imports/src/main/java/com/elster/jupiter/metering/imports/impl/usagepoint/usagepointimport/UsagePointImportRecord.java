package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class UsagePointImportRecord extends FileImportRecord {

    private String mRID;
    private String serviceKind;
    private Long serviceLocationID;
    private String name;
    private String aliasName;
    private String description;
    private String outageregion;
    private String readcycle;
    private String readroute;
    private String servicePriority;
    private Boolean allowUpdate;
    private Boolean grounded;
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
    private Map<CustomPropertySet,CustomPropertySetValues> customPropertySetValues;

    public String getmRID() {
        return mRID;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public String getServiceKind() {
        return serviceKind;
    }

    public void setServiceKind(String serviceKind) {
        this.serviceKind = serviceKind.toUpperCase();
    }

    public Long  getServiceLocationID() {
        return serviceLocationID;
    }

    public void setServiceLocationID(Long serviceLocationID) {
        this.serviceLocationID = serviceLocationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutageregion() {
        return outageregion;
    }

    public void setOutageregion(String outageregion) {
        this.outageregion = outageregion;
    }

    public String getReadcycle() {
        return readcycle;
    }

    public void setReadcycle(String readcycle) {
        this.readcycle = readcycle;
    }

    public String getReadroute() {
        return readroute;
    }

    public void setReadroute(String readroute) {
        this.readroute = readroute;
    }

    public String getServicePriority() {
        return servicePriority;
    }

    public void setServicePriority(String servicePriority) {
        this.servicePriority = servicePriority;
    }

    public Boolean getAllowUpdate() {
        return allowUpdate;
    }

    public void setAllowUpdate(Boolean allowUpdate) {
        this.allowUpdate = allowUpdate;
    }

    public Boolean getGrounded() {
        return grounded;
    }

    public void setGrounded(Boolean grounded) {
        this.grounded = grounded;
    }

    public String getPhaseCode() {
        return phaseCode;
    }

    public void setPhaseCode(String phaseCode) {
        this.phaseCode = phaseCode.toUpperCase();
    }

    public BigDecimal getRatedPowerValue() {
        return ratedPowerValue;
    }

    public void setRatedPowerValue(BigDecimal ratedPowerValue) {
        this.ratedPowerValue = ratedPowerValue;
    }

    public Integer getRatedPowerMultiplier() {
        return ratedPowerMultiplier;
    }

    public void setRatedPowerMultiplier(Integer ratedPowerMultiplier) {
        this.ratedPowerMultiplier = ratedPowerMultiplier;
    }

    public String getRatedPowerUnit() {
        return ratedPowerUnit;
    }

    public void setRatedPowerUnit(String ratedPowerUnit) {
        this.ratedPowerUnit = ratedPowerUnit;
    }

    public BigDecimal getRatedCurrentValue() {
        return ratedCurrentValue;
    }

    public void setRatedCurrentValue(BigDecimal ratedCurrentValue) {
        this.ratedCurrentValue = ratedCurrentValue;
    }

    public Integer getRatedCurrentMultiplier() {
        return ratedCurrentMultiplier;
    }

    public void setRatedCurrentMultiplier(Integer ratedCurrentMultiplier) {
        this.ratedCurrentMultiplier = ratedCurrentMultiplier;
    }

    public String getRatedCurrentUnit() {
        return ratedCurrentUnit;
    }

    public void setRatedCurrentUnit(String ratedCurrentUnit) {
        this.ratedCurrentUnit = ratedCurrentUnit;
    }

    public BigDecimal getEstimatedLoadValue() {
        return estimatedLoadValue;
    }

    public void setEstimatedLoadValue(BigDecimal estimatedLoadValue) {
        this.estimatedLoadValue = estimatedLoadValue;
    }

    public Integer getEstimatedLoadMultiplier() {
        return estimatedLoadMultiplier;
    }

    public void setEstimatedLoadMultiplier(Integer estimatedLoadMultiplier) {
        this.estimatedLoadMultiplier = estimatedLoadMultiplier;
    }

    public String getEstimatedLoadUnit() {
        return estimatedLoadUnit;
    }

    public void setEstimatedLoadUnit(String estimatedLoadUnit) {
        this.estimatedLoadUnit = estimatedLoadUnit;
    }

    public BigDecimal getNominalVoltageValue() {
        return nominalVoltageValue;
    }

    public void setNominalVoltageValue(BigDecimal nominalVoltageValue) {
        this.nominalVoltageValue = nominalVoltageValue;
    }

    public Integer getNominalVoltageMultiplier() {
        return nominalVoltageMultiplier;
    }

    public void setNominalVoltageMultiplier(Integer nominalVoltageMultiplier) {
        this.nominalVoltageMultiplier = nominalVoltageMultiplier;
    }

    public String getNominalVoltageUnit() {
        return nominalVoltageUnit;
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
}
