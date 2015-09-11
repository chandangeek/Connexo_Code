package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface MeteringService {
    String COMPONENTNAME = "MTR";

    ServiceLocation newServiceLocation();

    ReadingStorer createUpdatingStorer();

    ReadingStorer createUpdatingStorer(StorerProcess process);

    ReadingStorer createOverrulingStorer();

    ReadingStorer createNonOverrulingStorer();

    Optional<ReadingType> getReadingType(String mRid);

    Optional<ServiceLocation> findServiceLocation(String mRid);

    Optional<ServiceLocation> findServiceLocation(long id);

    Optional<ServiceCategory> getServiceCategory(ServiceKind kind);

    Optional<UsagePoint> findUsagePoint(long id);

    Optional<Meter> findMeter(long id);

    Optional<Meter> findMeter(String mRid);

    Optional<EndDevice> findEndDevice(String mRid);

    Optional<MeterActivation> findMeterActivation(long meterActivationId);

    Optional<Channel> findChannel(long id);

    Optional<AmrSystem> findAmrSystem(long id);

    List<ReadingType> getAvailableReadingTypes();

    /**
     * Get a list of ReadingTypes which don't have an interval (eg. MacroPeriod and TimePeriod are NOT_APPLICABLE)
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
}
