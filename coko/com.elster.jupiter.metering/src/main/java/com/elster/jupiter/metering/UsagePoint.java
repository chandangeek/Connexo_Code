/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.units.Quantity;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface UsagePoint extends HasId, IdentifiedObject {

    long getVersion();

    boolean isSdp();

    void setSdp(boolean isSdp);

    boolean isVirtual();

    void setVirtual(boolean isVirtual);

    String getOutageRegion();

    void setOutageRegion(String outageRegion);

    void setName(String name);

    String getAliasName();

    void setAliasName(String aliasName);

    String getDescription();

    void setDescription(String description);

    String getReadRoute();

    void setReadRoute(String readRoute);

    String getServicePriority();

    void setServicePriority(String servicePriority);

    Optional<ServiceLocation> getServiceLocation();

    void setServiceLocation(ServiceLocation serviceLocation);

    String getServiceLocationString();

    void setServiceLocationString(String serviceLocationString);

    ServiceCategory getServiceCategory();

    Instant getInstallationTime();

    void setInstallationTime(Instant installationTime);

    String getServiceDeliveryRemark();

    void setServiceDeliveryRemark(String serviceDeliveryRemark);

    Instant getCreateDate();

    Instant getModificationDate();

    List<UsagePointAccountability> getAccountabilities();

    UsagePointAccountability addAccountability(PartyRole role, Party party, Instant start);

    Optional<Party> getCustomer(Instant when);

    Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole);

    boolean hasAccountability(User user);

    List<? extends UsagePointDetail> getDetail(Range<Instant> range);

    /**
     * Get all existing details for this usage point
     */
    List<? extends UsagePointDetail> getDetails();

    Optional<? extends UsagePointDetail> getDetail(Instant when);

    void addDetail(UsagePointDetail usagePointDetail);

    UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date);

    UsagePointDetailBuilder newDefaultDetailBuilder(Instant start);

    ElectricityDetailBuilder newElectricityDetailBuilder(Instant start);

    GasDetailBuilder newGasDetailBuilder(Instant instant);

    WaterDetailBuilder newWaterDetailBuilder(Instant instant);

    HeatDetailBuilder newHeatDetailBuilder(Instant start);

    List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType);

    UsagePointConfigurationBuilder startingConfigurationOn(Instant startTime);

    Optional<UsagePointConfiguration> getConfiguration(Instant time);

    Optional<Location> getLocation();

    void setLocation(long locationId);

    Optional<SpatialCoordinates> getSpatialCoordinates();

    void setSpatialCoordinates(SpatialCoordinates spatialCoordinates);

    LocationBuilder updateLocation();

    /**
     * Applies the specified {@link UsagePointMetrologyConfiguration} to this UsagePoint
     * from this point in time onward.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @see #apply(UsagePointMetrologyConfiguration, Instant)
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration);

    /**
     * Applies the specified {@link UsagePointMetrologyConfiguration} to this UsagePoint
     * from the specified instant in time onward.
     * Note that this may produce errors when e.g. the requirements of required {@link MetrologyContract}
     * of the {@link UsagePointMetrologyConfiguration} are not met by the Meter(s) that is/are
     * linked to this UsagePoint from that instant in time onward.
     *
     * @param metrologyConfiguration The UsagePointMetrologyConfiguration
     * @param when The instant in time
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant when);

    /**
     * Applies the specified metrology configuration as
     * {@link UsagePoint#apply(com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration, java.time.Instant)}
     * with extra possibility to activate optional purposes at the same time.
     * All the {@link ReadingTypeRequirement}s of mandatory and optional but activated {@link MetrologyContract}s
     * will be checked to be provided by linked meters from that instant in time onward.
     *
     * @param metrologyConfiguration The UsagePointMetrologyConfiguration
     * @param when The instant in time
     * @param optionalContractsToActivate The set of optional purposes of target metrology configuration to be activated on the usage point
     * @see #apply(com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration, java.time.Instant)
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant when, Set<MetrologyContract> optionalContractsToActivate);

    void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end);

    void updateWithInterval(EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationVersion, UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationByStart(Instant start);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getCurrentEffectiveMetrologyConfiguration();

    List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurations();

    List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurations(Range<Instant> when);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfiguration(Instant when);

    void removeMetrologyConfiguration(Instant when);

    void removeMetrologyConfigurationVersion(EffectiveMetrologyConfigurationOnUsagePoint version);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> findEffectiveMetrologyConfigurationById(long id);

    UsagePointCustomPropertySetExtension forCustomProperties();

    /**
     * Returns current connection state of the usage point.
     *
     * @return the ConnectionState
     * @deprecated Since the connection state is versioned this method may return 'null' if no connection state is defined on a usage point
     * <p>
     * Use {@link UsagePoint#getCurrentConnectionState()} instead
     */
    @Deprecated
    ConnectionState getConnectionState();

    /**
     * Returns translated name of current connection state of the usage point.
     *
     * @deprecated Use {@link UsagePointConnectionState#getConnectionStateDisplayName()} instead
     */
    @Deprecated
    String getConnectionStateDisplayName();

    /**
     * Returns current connection state of the usage point or Optional.empty() if there is connection state is not specified
     *
     * @return the UsagePointConnectionState
     */
    Optional<UsagePointConnectionState> getCurrentConnectionState();

    void setConnectionState(ConnectionState connectionState);

    void setConnectionState(ConnectionState connectionState, Instant instant);

    List<CompletionOptions> connect(Instant when, ServiceCall serviceCall);

    List<CompletionOptions> disconnect(Instant when, ServiceCall serviceCall);

    List<CompletionOptions> enableLoadLimit(Instant when, Quantity loadLimit, ServiceCall serviceCall);

    List<CompletionOptions> disableLoadLimit(Instant when, ServiceCall serviceCall);

    List<CompletionOptions> readData(Instant when, List<ReadingType> readingTypes, ServiceCall serviceCall);

    void update();

    void delete();

    /**
     * Use the {@link #getMeterActivations(Instant)} instead.
     * In fact this method returns meter activation for {@link com.elster.jupiter.metering.config.DefaultMeterRole#DEFAULT} meter role
     */
    @Deprecated
    Optional<MeterActivation> getMeterActivation(Instant when);

    /**
     * Returns collection which contains one MeterActivation per meter role.
     */
    List<MeterActivation> getMeterActivations(Instant when);

    List<MeterActivation> getMeterActivations();

    /**
     * Returns collection which contains effective meter activations per meter role.
     */
    List<MeterActivation> getCurrentMeterActivations();

    /**
     * Returns the list of MeterActivations that are associated with the meters
     * activated on usage point in particular meter role.
     * The list is sorted ascending by start date of MeterActivation.
     */
    List<MeterActivation> getMeterActivations(MeterRole role);

    /**
     * Use the {@link #activate(Meter, MeterRole, Instant)} instead.
     * In fact the mentioned method will be called with {@link com.elster.jupiter.metering.config.DefaultMeterRole#DEFAULT}
     */
    @Deprecated
    MeterActivation activate(Meter meter, Instant start);

    MeterActivation activate(Meter meter, MeterRole meterRole, Instant from);

    UsagePointMeterActivator linkMeters();

    UsagePointState getState();

    UsagePointState getState(Instant instant);

    /**
     * Sets initial state of default usage point life cycle if and only if the usage point
     * has no current state yet, which is a possible situation during upgrade from 10.2 to 10.x
     */
    void setInitialState();

    ZoneId getZoneId();

    /**
     * Makes this UsagePoint obsolete.
     * This UsagePoint will no longer show up in queries
     * except the one that is looking for a UsagePoint by its database id or mRID.
     */
    void makeObsolete();

    /**
     * The Instant in time when this UsagePoint was made obsolete.
     *
     * @return The instant in time or {@code Optional.empty()} if this UsagePoint is not obsolete
     */
    Optional<Instant> getObsoleteTime();

    UsedCalendars getUsedCalendars();

    interface UsagePointConfigurationBuilder {

        UsagePointConfigurationBuilder endingAt(Instant endTime);

        UsagePointReadingTypeConfigurationBuilder configureReadingType(ReadingType readingType);

        UsagePointConfiguration create();
    }

    interface UsagePointReadingTypeConfigurationBuilder {

        UsagePointConfiguration create();

        UsagePointReadingTypeMultiplierConfigurationBuilder withMultiplierOfType(MultiplierType multiplierOfType);

    }

    interface UsagePointReadingTypeMultiplierConfigurationBuilder {

        UsagePointConfigurationBuilder calculating(ReadingType readingType);
    }

    /**
     * Models the usage of a {@link Calendar} on a UsagePoint.
     * A Calendar is in use on a UsagePoint for a specified
     * period in time. Multiple Calendars can only be in use
     * at the same time if their {@link Category} is different.
     * In other words adding a Calendar of one Category
     * while another Calendar of the same Category is already
     * in use will automatically stop the old Calendar
     * from being in use.
     */
    interface CalendarUsage {
        Range<Instant> getRange();
        Calendar getCalendar();
    }

    interface UsedCalendars {

        /**
         * Adds the specified {@link Calendar} to this UsagePoint.
         * From this point in time onwards, the specified Calendar
         * will be the only Calendar in use of the specified {@link Category}.
         * Therefore, this will throw an exception if another
         * Calendar of the same Category was added with a date
         * further in the future.<br>
         * Should have the same effect as:
         * <code>
         * <pre>
         * Clock clock = ...
         * usagePoint.addCalendar(clock.instant(), calendar);
         * </pre>
         * </code>
         *
         * @param calendar The Calendar
         * @return The {@link CalendarUsage}
         * @see #addCalendar(Calendar, Instant)
         */
        CalendarUsage addCalendar(Calendar calendar);

        /**
         * Adds the specified {@link Calendar} to this UsagePoint
         * from the specified point in time onwards. Note that that
         * point in time should be in the future.
         * From that point in time onwards, the specified Calendar
         * will be the only Calendar in use of the specified {@link Category}.
         * Therefore, this will throw an exception if another
         * Calendar of the same Category was added with a date
         * even further in the future.
         *
         * @param calendar The Calendar
         * @param startAt The point in time from which the Calendar will be in use
         * @return The {@link CalendarUsage}
         */
        CalendarUsage addCalendar(Calendar calendar, Instant startAt);

        /**
         * Removes the specified {@link Calendar} so that it is no longer
         * in use from this point in time onwards.
         * Note that if the Calendar's {@link Category} is {@link com.elster.jupiter.calendar.OutOfTheBoxCategory#TOU time of use},
         * this will throw an exception if the {@link UsagePointMetrologyConfiguration}
         * requires {@link com.elster.jupiter.calendar.Event}s.<br>
         * Should have the same effect as:
         * <code>
         * <pre>
         * Clock clock = ...
         * usagePoint.removeCalendar(clock.instant(), calendar);
         * </pre>
         * </code>
         *
         * @param calendar The Calendar that will no longer be in use from this point in time onwards
         * @see #removeCalendar(Calendar, Instant)
         */
        void removeCalendar(Calendar calendar);

        /**
         * Removes the specified {@link Calendar} so that it is no longer
         * in use from the specified point in time onwards.
         * Note that if the Calendar's {@link Category} is {@link com.elster.jupiter.calendar.OutOfTheBoxCategory#TOU time of use},
         * this will throw an exception if the {@link UsagePointMetrologyConfiguration}
         * requires {@link com.elster.jupiter.calendar.Event}s.
         *
         * @param calendar The Calendar that will no longer be in use from this point in time onwards
         * @param removeAt The point in time from which the Calendar will no longer be in use
         * @see #removeCalendar(Calendar, Instant)
         */
        void removeCalendar(Calendar calendar, Instant removeAt);

        /**
         * Gets all the {@link Calendar}s that are or have been in use
         * since the creation of this UsagePoint organized by {@link Category}.
         *
         * @return The Calendar usages organized by Category
         */
        Map<Category, List<CalendarUsage>> getCalendars();

        /**
         * Gets all the {@link Calendar}s of the specified {@link Category}
         * that are or have been in use since the creation of this UsagePoint.
         *
         * @param category The Category
         * @return The List of {@link CalendarUsage}s
         */
        List<CalendarUsage> getCalendars(Category category);

        /**
         * Gets all the {@link Calendar}s that are in use on
         * the specified point in time.
         *
         * @param instant The point in time
         * @return The List of Calendar
         */
        List<Calendar> getCalendars(Instant instant);

        /**
         * Gets the only {@link Calendar} of the specified {@link Category}
         * that is in use on the specified point in time.
         *
         * @param instant The point in time
         * @param category The Category
         * @return The Calendar or <code>Optional.empty()</code> if no such Calendar exists
         */
        Optional<Calendar> getCalendar(Instant instant, Category category);
    }

}