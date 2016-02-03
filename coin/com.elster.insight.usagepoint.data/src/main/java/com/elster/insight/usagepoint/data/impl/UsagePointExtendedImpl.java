package com.elster.insight.usagepoint.data.impl;

import com.elster.insight.usagepoint.data.UsagePointExtended;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UsagePointExtendedImpl implements UsagePointExtended {

    private UsagePoint delegate;

    @Inject
    public UsagePointExtendedImpl() {

    }

    public UsagePointExtendedImpl init(UsagePoint usagePoint){
        this.delegate = usagePoint;
        return this;
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyCustomPropertySetValues() {
        return null;
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyCustomPropertySetValues(Instant effectiveTimeStamp) {
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Inherited methods, can be abandoned
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public long getId() {
        return delegate.getId();
    }

    @Override
    public boolean isSdp() {
        return delegate.isSdp();
    }

    @Override
    public boolean isVirtual() {
        return delegate.isVirtual();
    }

    @Override
    public String getOutageRegion() {
        return delegate.getOutageRegion();
    }

    @Override
    public String getReadCycle() {
        return delegate.getReadCycle();
    }

    @Override
    public String getReadRoute() {
        return delegate.getReadRoute();
    }

    @Override
    public String getServicePriority() {
        return delegate.getServicePriority();
    }

    @Override
    public List<? extends MeterActivation> getMeterActivations() {
        return delegate.getMeterActivations();
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return delegate.getCurrentMeterActivation();
    }

    @Override
    public long getServiceLocationId() {
        return delegate.getServiceLocationId();
    }

    @Override
    public Optional<ServiceLocation> getServiceLocation() {
        return delegate.getServiceLocation();
    }

    @Override
    public ServiceCategory getServiceCategory() {
        return delegate.getServiceCategory();
    }

    @Override
    public void setServiceLocation(ServiceLocation serviceLocation) {
        delegate.setServiceLocation(serviceLocation);
    }

    @Override
    public void setServicePriority(String servicePriority) {
        delegate.setServicePriority(servicePriority);
    }

    @Override
    public void setReadRoute(String readRoute) {
        delegate.setReadRoute(readRoute);
    }

    @Override
    public void setReadCycle(String readCycle) {
        delegate.setReadCycle(readCycle);
    }

    @Override
    public void setOutageRegion(String outageRegion) {
        delegate.setOutageRegion(outageRegion);
    }

    @Override
    public void setVirtual(boolean isVirtual) {
        delegate.setVirtual(isVirtual);
    }

    @Override
    public void setSdp(boolean isSdp) {
        delegate.setSdp(isSdp);
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public void setMRID(String mRID) {
        delegate.setMRID(mRID);
    }

    @Override
    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @Override
    public void setAliasName(String aliasName) {
        delegate.setAliasName(aliasName);
    }

    @Override
    public void update() {
        delegate.update();
    }

    @Override
    public Instant getCreateDate() {
        return delegate.getCreateDate();
    }

    @Override
    public Instant getModificationDate() {
        return delegate.getModificationDate();
    }

    @Override
    public long getVersion() {
        return delegate.getVersion();
    }

    @Override
    public MeterActivation activate(Instant start) {
        return delegate.activate(start);
    }

    @Override
    public MeterActivation activate(Meter meter, Instant start) {
        return delegate.activate(meter, start);
    }

    @Override
    public List<UsagePointAccountability> getAccountabilities() {
        return delegate.getAccountabilities();
    }

    public UsagePointAccountability addAccountability(PartyRole role, Party party, Instant start) {
        return delegate.addAccountability(role, party, start);
    }

    @Override
    public Optional<Party> getCustomer(Instant when) {
        return delegate.getCustomer(when);
    }

    @Override
    public Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole) {
        return delegate.getResponsibleParty(when, marketRole);
    }

    @Override
    public boolean hasAccountability(User user) {
        return delegate.hasAccountability(user);
    }

    @Override
    public void delete() {
        delegate.delete();
    }

    @Override
    public List<? extends UsagePointDetail> getDetail(Range<Instant> range) {
        return delegate.getDetail(range);
    }

    @Override
    public Optional<? extends UsagePointDetail> getDetail(Instant when) {
        return delegate.getDetail(when);
    }

    @Override
    public void addDetail(UsagePointDetail usagePointDetail) {
        delegate.addDetail(usagePointDetail);
    }

    @Override
    public UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date) {
        return delegate.terminateDetail(detail, date);
    }

    @Override
    public Optional<MeterActivation> getMeterActivation(Instant when) {
        return delegate.getMeterActivation(when);
    }

    @Override
    public ElectricityDetailBuilder newElectricityDetailBuilder(Instant start) {
        return delegate.newElectricityDetailBuilder(start);
    }

    @Override
    public GasDetailBuilder newGasDetailBuilder(Instant instant) {
        return delegate.newGasDetailBuilder(instant);
    }

    @Override
    public WaterDetailBuilder newWaterDetailBuilder(Instant instant) {
        return delegate.newWaterDetailBuilder(instant);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType) {
        return delegate.getReadingsWithFill(range, readingType);
    }

    @Override
    public UsagePointConfigurationBuilder startingConfigurationOn(Instant startTime) {
        return delegate.startingConfigurationOn(startTime);
    }

    @Override
    public Optional<UsagePointConfiguration> getConfiguration(Instant time) {
        return delegate.getConfiguration(time);
    }

    @Override
    public String getAliasName() {
        return delegate.getAliasName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getMRID() {
        return delegate.getMRID();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        return delegate.getReadingTypes(range);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        return delegate.getReadings(range, readingType);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        return delegate.getReadingsUpdatedSince(range, readingType, since);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return delegate.getReadingsBefore(when, readingType, count);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return delegate.getReadingsOnOrBefore(when, readingType, count);
    }

    @Override
    public boolean hasData() {
        return delegate.hasData();
    }

    @Override
    public boolean is(ReadingContainer other) {
        return delegate.is(other);
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return delegate.getMeter(instant);
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return delegate.getUsagePoint(instant);
    }

    @Override
    public ZoneId getZoneId() {
        return delegate.getZoneId();
    }

    @Override
    public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
        return delegate.toList(readingType, exportInterval);
    }

    @Override
    public List<ReadingQualityRecord> getReadingQualities(ReadingQualityType readingQualityType, ReadingType readingType, Range<Instant> interval) {
        return delegate.getReadingQualities(readingQualityType, readingType, interval);
    }
}
