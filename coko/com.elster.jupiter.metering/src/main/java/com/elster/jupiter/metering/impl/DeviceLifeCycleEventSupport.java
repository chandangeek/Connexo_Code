/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link StandardEventPredicate}
 * to enable the events of this bundle that relate {@link com.elster.jupiter.metering.EndDevice}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (11:17)
 */
@Component(name = "com.elster.jupiter.metering.fsm.event", service = {StandardEventPredicate.class, CurrentStateExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleEventSupport implements StandardEventPredicate, CurrentStateExtractor {

    private volatile MeteringService meteringService;

    // For OSGi purposes
    public DeviceLifeCycleEventSupport() {
        super();
    }

    // For testing purposes
    public DeviceLifeCycleEventSupport(MeteringService meteringService) {
        this();
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public boolean isCandidate(com.elster.jupiter.events.EventType eventType) {
        return this.deviceRelatedEventTypes()
                .stream()
                .map(EventType::topic)
                .anyMatch(t -> eventType.getTopic().equals(t));
    }

    private Set<EventType> deviceRelatedEventTypes() {
        return EnumSet.of(
                EventType.METER_CREATED,
                EventType.METER_UPDATED,
                EventType.METER_DELETED,
                EventType.METERREADING_CREATED,
                EventType.READINGS_DELETED);
    }

    @Override
    public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
        if (this.isCandidate(event.getType())) {
            return Extractor.from(event.getType()).extractFrom(event, finiteStateMachine, this.meteringService);
        }
        else {
            return Optional.empty();
        }
    }

    private enum Extractor {
        METER_CRUD(EnumSet.of(EventType.METER_CREATED, EventType.METER_UPDATED, EventType.METER_DELETED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, MeteringService meteringService) {
                return this.currentStateFor((EndDevice) event.getSource(), finiteStateMachine);
            }
        },
        METER_READING(EnumSet.of(EventType.METERREADING_CREATED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, MeteringService meteringService) {
                return this.currentStateFor((MeterReadingStorer.EventSource) event.getSource(), finiteStateMachine, meteringService);
            }
        },
        DELETE_READING(EnumSet.of(EventType.READINGS_DELETED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, MeteringService meteringService) {
                return this.currentStateFor((Channel.ReadingsDeletedEvent) event.getSource(), finiteStateMachine, meteringService);
            }
        };

        private EnumSet<EventType> eventTypes;
        Extractor(EnumSet<EventType> eventTypes) {
            this.eventTypes = eventTypes;
        }

        private Set<String> topics() {
            return this.eventTypes.stream().map(EventType::topic).collect(Collectors.toSet());
        }

        protected Optional<CurrentState> currentStateFor(EndDevice endDevice, FiniteStateMachine finiteStateMachine) {
            Optional<State> actualState = endDevice.getState();
            if (actualState.isPresent() && actualState.get().getFiniteStateMachine().getId() == finiteStateMachine.getId()) {
                CurrentState currentState = new CurrentState();
                currentState.sourceId = endDevice.getAmrId();
                currentState.sourceType = EndDevice.class.getName();
                currentState.name = actualState.map(State::getName).get();
                return Optional.of(currentState);
            }
            else {
                // Device's state is not managed by a life cycle or not managed by the specified state machine
                return Optional.empty();
            }
        }

        protected Optional<CurrentState> currentStateFor(MeterReadingStorer.EventSource eventSource, FiniteStateMachine finiteStateMachine, MeteringService meteringService) {
            return this.currentStateFor(meteringService.findMeterById(eventSource.getMeterId()), finiteStateMachine);
        }

        protected Optional<CurrentState> currentStateFor(Optional<Meter> meter, FiniteStateMachine finiteStateMachine) {
            if (meter.isPresent()) {
                return this.currentStateFor(meter.get(), finiteStateMachine);
            }
            else {
                // Meter does not exist, funny because we just received an event for it
                return Optional.empty();
            }
        }

        protected Optional<CurrentState> currentStateFor(Channel.ReadingsDeletedEvent eventSource, FiniteStateMachine finiteStateMachine, MeteringService meteringService) {
            return this.currentStateFor(eventSource.getChannel().getChannelsContainer().getMeter(), finiteStateMachine);
        }

        static Extractor from(com.elster.jupiter.events.EventType eventType) {
            return Stream
                    .of(values())
                    .filter(e -> e.topics().contains(eventType.getTopic()))
                    .findFirst()
                    .get();
        }

        public abstract Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, MeteringService meteringService);

    }

}
