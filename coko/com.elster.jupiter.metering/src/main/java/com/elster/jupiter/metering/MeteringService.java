package com.elster.jupiter.metering;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;

import aQute.bnd.annotation.ProviderType;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface MeteringService {
    String COMPONENTNAME = "MTR";

    ServiceLocationBuilder newServiceLocation();

    ReadingStorer createUpdatingStorer();

    ReadingStorer createUpdatingStorer(StorerProcess process);

    ReadingStorer createOverrulingStorer();

    ReadingStorer createNonOverrulingStorer();

    Optional<ReadingType> getReadingType(String mRid);

    List<ReadingType> findReadingTypes(List<String> mRids);

    Finder<ReadingType> findReadingTypes(ReadingTypeFilter filter);

    Optional<ReadingType> findAndLockReadingTypeByIdAndVersion(String mRID, long version);

    Optional<ServiceLocation> findServiceLocation(String mRid);

    Optional<ServiceLocation> findServiceLocation(long id);

    Optional<ServiceCategory> getServiceCategory(ServiceKind kind);

    Optional<UsagePoint> findUsagePoint(long id);

    Optional<UsagePoint> findAndLockUsagePointByIdAndVersion(long id, long version);

    Optional<Meter> findMeter(long id);

    Optional<Meter> findMeter(String mRid);

    Optional<EndDevice> findEndDevice(String mRid);

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

    Optional<UsagePoint> findUsagePoint(String mRID);

    Query<EndDevice> getEndDeviceQuery();

    Optional<EndDevice> findEndDevice(long id);

    List<EndDeviceEventType> getAvailableEndDeviceEventTypes();

    Optional<EndDeviceEventType> getEndDeviceEventType(String mRID);

    void configurePurge(PurgeConfiguration purgeConfiguration);

    ReadingType createReadingType(String mRID, String aliasName);

    /**
     * Creates a new EndDeviceEventType based on the mRID input.
     * mRID should have the format &lt;EndDeviceEventType&gt;.&lt;EndDeviceEventDomain&gt;.&lt;EndDeviceEventSubDomain&gt;.&lt;EndDeviceEventEventOrAction&gt;
     * with each field a valid (according to the CIM spec) number.
     * Will throw a {@link IllegalMRIDFormatException} if mRID has not the correct format
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
     * @param effective The instant in time on which the switch over was effective
     * @param oldStateMachine The old FiniteStateMachine
     * @param newStateMachine The new FiniteStateMachine
     * @param deviceAmrIdSubquery The query that returns the amrId of each device to which the change should be applied
     */
    void changeStateMachine(Instant effective, FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery);

    List<ReadingType> getAvailableEquidistantReadingTypes();

    List<ReadingType> getAvailableNonEquidistantReadingTypes();

    Finder<UsagePoint> getUsagePoints(@NotNull UsagePointFilter filter);

    ReadingTypeFieldsFactory getReadingTypeFieldCodesFactory();

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

    LocationBuilder newLocationBuilder();

    Optional<Location> findLocation(long id);

    Optional<Location> findDeviceLocation(String mRID);

    Optional<Location> findDeviceLocation(long id);

    Optional<Location> findUsagePointLocation(String mRID);

    Optional<Location> findUsagePointLocation(long id);

    Query<LocationMember> getLocationMemberQuery();

    void createLocationTemplate();

    LocationTemplate getLocationTemplate();

    List<List<String>> getFormattedLocationMembers(long id);

    GeoCoordinates createGeoCoordinates(String coordinates);

    Optional<GeoCoordinates> findGeoCoordinates(long id);

    Optional<GeoCoordinates> findDeviceGeoCoordinates(String mRID);

    Optional<GeoCoordinates> findDeviceGeoCoordinates(long id);

    Optional<GeoCoordinates> findUsagePointGeoCoordinates(String mRID);

    Optional<GeoCoordinates> findUsagePointGeoCoordinates(long id);

    Query<GeoCoordinates> getGeoCoordinatesQuery();

    Optional<EndDeviceControlType> getEndDeviceControlType(String mRID);

    EndDeviceControlType createEndDeviceControlType(String mRID);

    List<HeadEndInterface> getHeadEndInterfaces();

    Optional<HeadEndInterface> getHeadEndInterface(String amrSystem);

    Map<KnownAmrSystem, String> getSupportedApplicationsUrls();
}
