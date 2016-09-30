package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.util.HasName;

import java.util.Optional;

public interface AmrSystem extends HasName {
    int getId();

    /**
     * Creates a new Meter whose state is not managed at all.
     *
     * @param amrId The identifier in the AmrSystem that is creating this Meter
     * @return The newly created Meter
     */
    MeterBuilder newMeter(String amrId, String name);

    EndDevice createEndDevice(String amrId);

    /**
     * @since 1.1
     */
    EndDevice createEndDevice(FiniteStateMachine stateMachine, String amrId);

    EndDevice createEndDevice(String amrId, String name);

    /**
     * @since 1.1
     */
    EndDevice createEndDevice(FiniteStateMachine stateMachine, String amrId, String name);

    Optional<Meter> findMeter(String amrId);

    boolean is(KnownAmrSystem knownAmrSystem);

    /**
     * @since 1.1
     */
    Optional<Meter> lockMeter(String amrId);
}
