/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import org.osgi.service.component.annotations.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link StandardEventPredicate}
 * to enable the events of this bundle that relate to {@link com.elster.jupiter.metering.EndDevice}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-17 (16:09)
 */
@Component(name = "com.energyict.mdc.device.data.lifecycle.event", service = {StandardEventPredicate.class, CurrentStateExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleEventSupport implements StandardEventPredicate, CurrentStateExtractor {

    @Override
    public boolean isCandidate(com.elster.jupiter.events.EventType eventType) {
        return this.deviceRelatedEventTypes()
                .stream()
                .map(EventType::topic)
                .filter(t -> eventType.getTopic().equals(t))
                .findAny()
                .isPresent();
    }

    private Set<EventType> deviceRelatedEventTypes() {
        return EnumSet.of(
                EventType.COMTASKEXECUTION_CREATED,
                EventType.COMTASKEXECUTION_UPDATED,
                EventType.COMTASKEXECUTION_DELETED,
                EventType.CONNECTIONTASK_CREATED,
                EventType.CONNECTIONTASK_UPDATED,
                EventType.CONNECTIONTASK_DELETED,
                EventType.CONNECTIONTASK_SETASDEFAULT,
                EventType.CONNECTIONTASK_CLEARDEFAULT,
                EventType.DEVICE_CREATED,
                EventType.DEVICE_UPDATED,
                EventType.DEVICE_DELETED,
                EventType.DEVICEMESSAGE_CREATED,
                EventType.DEVICEMESSAGE_UPDATED,
                EventType.DEVICEMESSAGE_DELETED,
                EventType.PROTOCOLDIALECTPROPERTIES_CREATED,
                EventType.PROTOCOLDIALECTPROPERTIES_UPDATED,
                EventType.PROTOCOLDIALECTPROPERTIES_DELETED);
    }

    @Override
    public Optional<CurrentState> extractFrom(LocalEvent localEvent, FiniteStateMachine finiteStateMachine) {
        if (this.isCandidate(localEvent.getType())) {
            return Extractor.from(localEvent.getType()).extractFrom(localEvent, finiteStateMachine);
        }
        else {
            return Optional.empty();
        }
    }

    private enum Extractor {
        DEVICE_CRUD(EnumSet.of(EventType.DEVICE_CREATED, EventType.DEVICE_UPDATED, EventType.DEVICE_DELETED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
                return this.extractFrom((Device) event.getSource(), finiteStateMachine);
            }
        },

        DEVICEMESSAGE_CRUD(EnumSet.of(EventType.DEVICEMESSAGE_CREATED, EventType.DEVICEMESSAGE_UPDATED, EventType.DEVICEMESSAGE_DELETED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
                return this.extractFrom((DeviceMessage<Device>) event.getSource(), finiteStateMachine);
            }
        },

        PROTOCOLDIALECTPROPERTIES_CRUD(EnumSet.of(EventType.PROTOCOLDIALECTPROPERTIES_CREATED, EventType.PROTOCOLDIALECTPROPERTIES_UPDATED, EventType.PROTOCOLDIALECTPROPERTIES_DELETED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
                return this.extractFrom((ProtocolDialectProperties) event.getSource(), finiteStateMachine);
            }
        },

        COMTASKEXECUTION_CRUD(EnumSet.of(EventType.COMTASKEXECUTION_CREATED, EventType.COMTASKEXECUTION_UPDATED, EventType.COMTASKEXECUTION_DELETED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
                return this.extractFrom((ComTaskExecution) event.getSource(), finiteStateMachine);
            }
        },

        CONNECTIONTASK_CRUD(EnumSet.of(EventType.CONNECTIONTASK_CREATED, EventType.CONNECTIONTASK_UPDATED, EventType.CONNECTIONTASK_DELETED, EventType.CONNECTIONTASK_SETASDEFAULT, EventType.CONNECTIONTASK_CLEARDEFAULT)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
                return this.extractFrom((ConnectionTask) event.getSource(), finiteStateMachine);
            }
        };

        private EnumSet<EventType> eventTypes;
        Extractor(EnumSet<EventType> eventTypes) {
            this.eventTypes = eventTypes;
        }

        private Set<String> topics() {
            return this.eventTypes.stream().map(EventType::topic).collect(Collectors.toSet());
        }

        protected Optional<CurrentState> extractFrom(ComTaskExecution comTaskExecution, FiniteStateMachine finiteStateMachine) {
            return this.extractFrom(comTaskExecution.getDevice(), finiteStateMachine);
        }

        protected Optional<CurrentState> extractFrom(ConnectionTask connectionTask, FiniteStateMachine finiteStateMachine) {
            return this.extractFrom(connectionTask.getDevice(), finiteStateMachine);
        }

        protected Optional<CurrentState> extractFrom(ProtocolDialectProperties protocolDialectProperties, FiniteStateMachine finiteStateMachine) {
            return this.extractFrom(protocolDialectProperties.getDevice(), finiteStateMachine);
        }

        protected Optional<CurrentState> extractFrom(DeviceMessage<Device> deviceMessage, FiniteStateMachine finiteStateMachine) {
            return this.extractFrom(deviceMessage.getDevice(), finiteStateMachine);
        }

        protected Optional<CurrentState> extractFrom(Device device, FiniteStateMachine finiteStateMachine) {
            State state = device.getState();
            if (state.getFiniteStateMachine().getId() == finiteStateMachine.getId()) {
                CurrentState currentState = new CurrentState();
                currentState.sourceId = String.valueOf(device.getId());
                currentState.sourceType = Device.class.getName();
                currentState.name = state.getName();
                return Optional.of(currentState);
            }
            else {
                // Device's state is not managed by the specified state machine
                return Optional.empty();
            }
        }

        static Extractor from(com.elster.jupiter.events.EventType eventType) {
            return Stream
                    .of(values())
                    .filter(e -> e.topics().contains(eventType.getTopic()))
                    .findFirst()
                    .get();
        }

        public abstract Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine);

    }
}