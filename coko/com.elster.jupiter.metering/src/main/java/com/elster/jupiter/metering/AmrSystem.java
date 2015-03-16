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
	Meter newMeter(String amrId);

    /**
     * Creates a new Meter whose state is managed and determined by the specified {@link FiniteStateMachine}.
     *
     * @param stateMachine The FiniteStateMachine
     * @param amrId The identifier in the AmrSystem that is creating this Meter
     * @return The newly created Meter
     * @since 1.1
     */
	Meter newMeter(FiniteStateMachine stateMachine, String amrId);

	Meter newMeter(String amrId, String mRID);
    /**
     * @since 1.1
     */
	Meter newMeter(FiniteStateMachine stateMachine, String amrId, String mRID);

    EndDevice newEndDevice(String amrId);
    /**
     * @since 1.1
     */
    EndDevice newEndDevice(FiniteStateMachine stateMachine, String amrId);

	EndDevice newEndDevice(String amrId, String mRID);
    /**
     * @since 1.1
     */
	EndDevice newEndDevice(FiniteStateMachine stateMachine, String amrId, String mRID);

	Optional<Meter> findMeter(String amrId);
	boolean is(KnownAmrSystem knownAmrSystem);
    /**
	* @since 1.1
	*/
	Optional<Meter> lockMeter(String amrId);
}