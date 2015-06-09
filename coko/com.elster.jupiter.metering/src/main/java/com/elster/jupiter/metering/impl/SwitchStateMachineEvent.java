package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.MeteringService;

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

    static final String TOPIC = "com/elster/jupiter/metering/fsm/SWITCH";
    static final String SUBSCRIBER = "SwitchStateMachineEventSubsc";
    static final String SUBSCRIBER_TRANSLATION = "Change device state";
    static final String DESTINATION = "SwitchStateMachineDest";

    /**
     * The timestamp at which the change over to the new state should be recorded.
     */
    private final long now;

    /**
     * The unique identifier of the old {@link FiniteStateMachine}.
     */
    private final long oldFiniteStateMachineId;

    /**
     * The unique identifier of the old {@link FiniteStateMachine}.
     */
    private final long newFiniteStateMachineId;

    private final String deviceIds;

    public SwitchStateMachineEvent(long now, long oldFiniteStateMachineId, long newFiniteStateMachineId, List<Long> deviceIds) {
        super();
        this.now = now;
        this.oldFiniteStateMachineId = oldFiniteStateMachineId;
        this.newFiniteStateMachineId = newFiniteStateMachineId;
        this.deviceIds = deviceIds.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * Installs this event with the {@link EventService}.
     *
     * @param eventService The EventService
     */
    public static void install(EventService eventService) {
        eventService.buildEventTypeWithTopic(TOPIC)
                .name(SwitchStateMachineEvent.class.getSimpleName())
                .component(MeteringService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("now", ValueType.LONG, "now")
                .withProperty("oldFiniteStateMachineId", ValueType.LONG, "oldFiniteStateMachineId")
                .withProperty("newFiniteStateMachineId", ValueType.LONG, "newFiniteStateMachineId")
                .withProperty("deviceIds", ValueType.STRING, "deviceIds")
                .shouldPublish()
                .create()
                .save();
    }

    /**
     * Publishes this SwitchStateMachineEvent with the {@link EventService}.
     *
     * @param eventService The EventService
     */
    public void publish(EventService eventService) {
        eventService.postEvent(TOPIC, this);
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