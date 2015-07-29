package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.json.JsonService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the event that will be handled in the background
 * to switch the {@link FiniteStateMachine} of a collection of
 * {@link com.elster.jupiter.metering.EndDevice}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-20 (08:58)
 */
public class SwitchStateMachineEvent {

    static final String SUBSCRIBER = "SwitchStateMachineEventSubsc";
    static final String SUBSCRIBER_TRANSLATION = "Change device state";
    static final String DESTINATION = "SwitchStateMachineDest";

    /**
     * The timestamp at which the change over to the new state should be recorded.
     */
    private long now;

    /**
     * The unique identifier of the old {@link FiniteStateMachine}.
     */
    private long oldFiniteStateMachineId;

    /**
     * The unique identifier of the old {@link FiniteStateMachine}.
     */
    private long newFiniteStateMachineId;

    private String deviceIds;

    public SwitchStateMachineEvent() {
        super();
    }

    public SwitchStateMachineEvent(long now, long oldFiniteStateMachineId, long newFiniteStateMachineId, List<Long> deviceIds) {
        super();
        this.now = now;
        this.oldFiniteStateMachineId = oldFiniteStateMachineId;
        this.newFiniteStateMachineId = newFiniteStateMachineId;
        this.deviceIds = deviceIds.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * Publishes this SwitchStateMachineEvent on the {@link DestinationSpec}.
     *
     * @param destinationSpec The DestinationSpec
     */
    public void publish(DestinationSpec destinationSpec, JsonService jsonService) {
        destinationSpec.message(jsonService.serialize(this)).send();
    }

    public long getNow() {
        return now;
    }

    public long getOldFiniteStateMachineId() {
        return oldFiniteStateMachineId;
    }

    public long getNewFiniteStateMachineId() {
        return newFiniteStateMachineId;
    }

    public String getDeviceIds() {
        return deviceIds;
    }

}