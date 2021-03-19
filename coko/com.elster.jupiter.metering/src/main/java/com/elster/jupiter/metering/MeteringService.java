/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.aggregation.ReadingQualityCommentCategory;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface MeteringService {
    String COMPONENTNAME = "MTR";
    String END_DEVICE_STAGE_SET_NAME = "END_DEVICE_STAGE_SET";
    String DEFAULT_MULTIPLIER_TYPE = "Default";

    ServiceLocationBuilder newServiceLocation();

    ReadingStorer createUpdatingStorer();

    ReadingStorer createUpdatingStorer(StorerProcess process);

    ReadingStorer createOverrulingStorer();

    ReadingStorer createNonOverrulingStorer();

    Optional<ReadingType> getReadingType(String mRid);

    Optional<ReadingType> getReadingTypeByName(String name);

    List<ReadingType> findReadingTypes(List<String> mRids);

    Finder<ReadingType> findReadingTypes(ReadingTypeFilter filter);

    Finder<ReadingType> findReadingTypesByAlias(ReadingTypeFilter filter, String aliasName);

    Finder<ReadingType> findReadingTypeByAlias(String aliasName);

    Optional<ReadingType> findAndLockReadingTypeByIdAndVersion(String mRID, long version);

    Optional<ServiceLocation> findServiceLocation(String mRid);

    Optional<ServiceLocation> findServiceLocation(long id);

    Optional<ServiceCategory> getServiceCategory(ServiceKind kind);

    Optional<UsagePoint> findUsagePointById(long id);

    Optional<UsagePoint> findAndLockUsagePointByIdAndVersion(long id, long version);

    Optional<UsagePoint> findAndLockUsagePointByMRIDAndVersion(String mRID, long version);

    Optional<UsagePoint> findAndLockUsagePointByNameAndVersion(String name, long version);

    Optional<UsagePoint> findUsagePointByMRID(String mRID);

    Optional<UsagePoint> findUsagePointByName(String name);

    /**
     * @deprecated Pls directly use {@link UsagePointLifeCycleConfigurationService#findUsagePointLifeCycleByName(String)}.
     */
    @Deprecated
    UsagePointLifeCycle findUsagePointLifeCycleByName(String name);

    UsagePointLifeCycle findUsagePointLifeCycle(String name);

    Optional<Meter> findMeterById(long id);

    Optional<Meter> findMeterByMRID(String mRID);

    Optional<Meter> findMeterByName(String name);

    Optional<EndDevice> findEndDeviceById(long id);

    Optional<EndDevice> findEndDeviceByMRID(String mRID);

    Optional<EndDevice> findEndDeviceByName(String name);

    /**
     * @deprecated Uses quite doubtful logic where the same string can be treated as MRID or as name at the same time.
     */
    @Deprecated
    List<EndDevice> findEndDevices(Set<String> mridsOrNames);

    @Deprecated
    Finder<EndDevice> findEndDevices(Set<String> mRIDs, Set<String> deviceNames);

    Finder<Meter> findMeters(MeterFilter filter);

    List<Meter> findMetersByLocation(Location location);

    Optional<MeterActivation> findMeterActivation(long meterActivationId);

    Optional<Channel> findChannel(long id);

    Optional<AmrSystem> findAmrSystem(long id);

    List<ReadingType> getAvailableReadingTypes();

    /**
     * Get a list of ReadingTypes which don't have an interval (eg. MacroPeriod and TimePeriod are NOT_APPLICABLE)
     *
     * @since v1.1
     */
    List<ReadingType> getAllReadingTypesWithoutInterval();

    List<JournalEntry<ServiceLocation>> findServiceLocationJournal(long id);

    Query<UsagePoint> getUsagePointQuery();

    Query<MeterActivation> getMeterActivationQuery();

    Query<ServiceLocation> getServiceLocationQuery();

    Condition hasAccountability();

    Condition hasAccountability(Instant when);

    Query<Meter> getMeterQuery();

    /**
     * Creates a query to retrieve Meter that have (measurement) data in the readingQualityTimestamp period
     * that have reading qualities of the passed ReadingQualityTypes
     *
     * @param readingQualityTimestamp the period of data that
     * @param readingQualityTypes     the ReadingQualityTypes
     * @return a query that retrieves Meter
     * @since 4.0
     */
    Query<Meter> getMeterWithReadingQualitiesQuery(Range<Instant> readingQualityTimestamp, ReadingQualityType... readingQualityTypes);

    Query<ChannelsContainer> getChannelsContainerWithReadingQualitiesQuery(Range<Instant> readingQualityTimestamp, ReadingQualityType... readingQualityTypes);

    Query<EndDevice> getEndDeviceQuery();

    List<EndDeviceEventType> getAvailableEndDeviceEventTypes();

    Optional<EndDeviceEventType> getEndDeviceEventType(String mRID);

    void configurePurge(PurgeConfiguration purgeConfiguration);

    ReadingType createReadingType(String mRID, String aliasName);

    /**
     * Creates a new EndDeviceEventType based on the mRID input.
     * mRID should have the format &lt;EndDeviceEventType&gt;.&lt;EndDeviceEventDomain&gt;.&lt;EndDeviceEventSubDomain&gt;.&lt;EndDeviceEventEventOrAction&gt;
     * with a valid (according to the CIM spec) number for each field.
     * Will throw a {@link IllegalMRIDFormatException} if mRID does not have the correct format
     *
     * @since 2.0
     */
    EndDeviceEventType createEndDeviceEventType(String mRID);

    void purge(PurgeConfiguration purgeConfiguration);

    /**
     * Changes the state of the devices identified by the <code>deviceAmrIdSubquery</code>
     * from the old {@link FiniteStateMachine} to a compatible {@link com.elster.jupiter.fsm.State}
     * in the new FiniteStateMachine. Currently, two States are considered compatible if
     * they have the same name. This means that changing to another FiniteStateMachine
     * maps the current State of each device to the State with the same name
     * in the new FiniteStateMachine.
     * Note that the effective timestamp cannot be in the future and if it is
     * this will throw an IllegalArgumentException.
     *
     * @param effective           The instant in time on which the switch over was effective
     * @param oldStateMachine     The old FiniteStateMachine
     * @param newStateMachine     The new FiniteStateMachine
     * @param deviceAmrIdSubquery The query that returns the amrId of each device to which the change should be applied
     */
    void changeStateMachine(Instant effective, FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery);

    List<ReadingType> getAvailableEquidistantReadingTypes();

    List<ReadingType> getAvailableNonEquidistantReadingTypes();

    Finder<UsagePoint> getUsagePoints(@NotNull UsagePointFilter filter);

    ReadingTypeFieldsFactory getReadingTypeFieldCodesFactory();

    ReadingTypeFieldsFactory getReadingTypeGroupFieldCodesFactory(Integer filterBy);

    Finder<ReadingType> getReadingTypesByMridFilter(@NotNull ReadingTypeMridFilter filter);

    /**
     * Creates a new {@link MultiplierType} with the specified name.
     *
     * @param name The required name
     * @return The newly created MultiplierType
     */
    MultiplierType createMultiplierType(String name);

    /**
     * Creates a new {@link MultiplierType} whose name is
     * determined by the specified NlsKey.
     *
     * @param name The required nameKey
     * @return The newly created MultiplierType
     */
    MultiplierType createMultiplierType(NlsKey name);

    /**
     * Gets the {@link MultiplierType} that was created by default.
     *
     * @param standardType The StandardType of interest
     * @return The MultiplierType
     */
    MultiplierType getMultiplierType(MultiplierType.StandardType standardType);

    Optional<MultiplierType> getMultiplierType(String name);

    List<MultiplierType> getMultiplierTypes();

    void createLocationTemplate();

    LocationTemplate getLocationTemplate();

    Optional<EndDeviceControlType> getEndDeviceControlType(String mRID);

    EndDeviceControlType createEndDeviceControlType(String mRID);

    List<HeadEndInterface> getHeadEndInterfaces();

    Optional<HeadEndInterface> getHeadEndInterface(String amrSystem);

    /**
     * Gets the GasDayOptions that were created at system installation time.
     *
     * @return The GasDayOptions
     */
    Optional<GasDayOptions> getGasDayOptions();

    List<MeterRole> getMeterRoles();

    List<ReadingQualityComment> getAllReadingQualityComments(ReadingQualityCommentCategory category);

    ReadingQualityComment createReadingQualityComment(ReadingQualityCommentCategory category, String comment);

    Optional<ReadingQualityComment> findReadingQualityComment(long id);

    /**
     * @deprecated Not quite generic method by its signature.
     * Its logic is as specific as filtering of end device events by a range of time and a subquery for end device ids;
     * same can be achieved by using {@link #streamEndDeviceEvents()} and filtering.
     * Also it seems to be badly implemented and <b>likely won't work at all</b>.
     */
    @Deprecated
    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, Subquery subquery);

    QueryStream<EndDeviceEventRecord> streamEndDeviceEvents();

    Optional<ChannelsContainer> findChannelsContainer(long id);
}
