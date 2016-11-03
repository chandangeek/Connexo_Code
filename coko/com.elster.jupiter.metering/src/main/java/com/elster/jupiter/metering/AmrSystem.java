package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.util.HasName;

import java.util.Optional;

public interface AmrSystem extends HasName {
    int getId();

    /**
     * Initializes {@link MeterBuilder} that is able to create new {@link Meter}.
     *
     * @param amrId Meter identifier in the AmrSystem that is creating this Meter.
     * @param name A unique name for new Meter.
     * @return {@link MeterBuilder} initialized with this AmrSystem and provided arguments.
     */
    MeterBuilder newMeter(String amrId, String name);

    EndDevice createEndDevice(String amrId, String name);

    EndDevice createEndDevice(FiniteStateMachine stateMachine, String amrId, String name);

    Optional<Meter> findMeter(String amrId);

    boolean is(KnownAmrSystem knownAmrSystem);

    Optional<Meter> lockMeter(String amrId);
}
