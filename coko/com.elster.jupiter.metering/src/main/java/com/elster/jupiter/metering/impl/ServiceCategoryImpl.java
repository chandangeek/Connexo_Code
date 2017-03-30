/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.ServiceCategoryMeterRoleUsage;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ServiceCategoryImpl implements ServiceCategory {

    enum Fields {
        CUSTOMPROPERTYSETUSAGE("serviceCategoryCustomPropertySetUsages"),
        METERROLEUSAGE("serviceCategoryMeterRoleUsages");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    //persistent fields
    private ServiceKind kind;
    private String aliasName;
    private String description;
    private boolean active;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final Clock clock;
    private final Thesaurus thesaurus;

    private List<ServiceCategoryCustomPropertySetUsage> serviceCategoryCustomPropertySetUsages = new ArrayList<>();
    private List<ServiceCategoryMeterRoleUsage> serviceCategoryMeterRoleUsages = new ArrayList<>();

    @Inject
    ServiceCategoryImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    ServiceCategoryImpl init(ServiceKind kind) {
        this.kind = kind;
        return this;
    }

    public ServiceKind getKind() {
        return kind;
    }

    public long getId() {
        return kind.ordinal() + 1;
    }

    @Override
    public String getName() {
        return kind.getDisplayName(thesaurus);
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void persist() {
        dataModel.persist(this);
    }

    public void update() {
        dataModel.update(this);
    }

    public UsagePointBuilder newUsagePoint(String name) {
        return this.newUsagePoint(name, this.clock.instant());
    }

    @Override
    public UsagePointBuilder newUsagePoint(String name, Instant installationTime) {
        return new UsagePointBuilderImpl(dataModel, name, installationTime, this);
    }

    @Override
    public UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start) {
        Interval interval = Interval.of(Range.atLeast(start));
        if (kind.equals(ServiceKind.ELECTRICITY)) {
            return ElectricityDetailImpl.from(dataModel, usagePoint, interval);
        } else if (kind.equals(ServiceKind.GAS)) {
            return GasDetailImpl.from(dataModel, usagePoint, interval);
        } else if (kind.equals(ServiceKind.WATER)) {
            return WaterDetailImpl.from(dataModel, usagePoint, interval);
        } else if (kind.equals(ServiceKind.HEAT)) {
            return HeatDetailImpl.from(dataModel, usagePoint, interval);
        } else {
            return DefaultDetailImpl.from(dataModel, usagePoint, interval);
        }
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return serviceCategoryCustomPropertySetUsages
                .stream()
                .map(ServiceCategoryCustomPropertySetUsage::getRegisteredCustomPropertySet)
                .collect(Collectors.toList());
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        if (serviceCategoryCustomPropertySetUsages.stream().noneMatch(e -> e.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())) {
            ServiceCategoryCustomPropertySetUsage serviceCategoryCustomPropertySetUsage = this.dataModel.getInstance(ServiceCategoryCustomPropertySetUsage.class)
                    .initialize(this, registeredCustomPropertySet);
            this.serviceCategoryCustomPropertySetUsages.add(serviceCategoryCustomPropertySetUsage);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        serviceCategoryCustomPropertySetUsages.stream()
                .filter(f -> f.getServiceCategory().getId() == this.getId())
                .filter(f -> f.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())
                .findAny().ifPresent(serviceCategoryCustomPropertySetUsages::remove);
    }

    @Override
    public String getDisplayName() {
        return this.kind.getDisplayName(thesaurus);
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    History<? extends ServiceCategory> getHistory() {
        return new History<>(dataModel.mapper(ServiceCategory.class).getJournal(this.getKind()), this);
    }

    @Override
    public List<MeterRole> getMeterRoles() {
        return this.serviceCategoryMeterRoleUsages.stream().map(ServiceCategoryMeterRoleUsage::getMeterRole).collect(Collectors.toList());
    }

    @Override
    public void addMeterRole(MeterRole meterRole) {
        if (this.serviceCategoryMeterRoleUsages.stream().noneMatch(usage -> usage.getMeterRole().equals(meterRole))) {
            ServiceCategoryMeterRoleUsage usage = dataModel.getInstance(ServiceCategoryMeterRoleUsage.class).init(this, meterRole);
            this.serviceCategoryMeterRoleUsages.add(usage);
        }
    }

    @Override
    public void removeMeterRole(MeterRole meterRole) {
        this.serviceCategoryMeterRoleUsages.stream()
                .filter(usage -> usage.getMeterRole().equals(meterRole))
                .findFirst()
                .ifPresent(this.serviceCategoryMeterRoleUsages::remove);
    }
}
