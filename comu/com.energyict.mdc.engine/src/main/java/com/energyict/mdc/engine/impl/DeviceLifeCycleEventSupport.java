package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.events.DeviceTopologyChangedEvent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
@Component(name = "com.energyict.mdc.engine.device.lifecycle.event", service = {StandardEventPredicate.class, CurrentStateExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleEventSupport implements StandardEventPredicate, CurrentStateExtractor {

    private volatile DeviceService deviceService;

    // For OSGi purposes
    public DeviceLifeCycleEventSupport() {
        super();
    }

    // For testing purposes
    public DeviceLifeCycleEventSupport(DeviceService deviceService) {
        this();
        this.setDeviceService(deviceService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

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
                EventType.DEVICE_CONNECTION_FAILURE,
                EventType.DEVICE_CONNECTION_COMPLETION,
                EventType.DEVICE_TOPOLOGY_CHANGED);
    }

    @Override
    public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine) {
        if (this.isCandidate(event.getType())) {
            return Extractor.from(event.getType()).extractFrom(event, finiteStateMachine, this.deviceService);
        } else {
            return Optional.empty();
        }
    }

    private enum Extractor {
        DEVICE_CONNECTION(EnumSet.of(EventType.DEVICE_CONNECTION_FAILURE, EventType.DEVICE_CONNECTION_COMPLETION)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, DeviceService deviceService) {
                return this.currentStateFor((ConnectionTaskCompletionEventInfo) event.getSource(), finiteStateMachine, deviceService);
            }
        },
        DEVICE_TOPOLOGY_CHANGED(EnumSet.of(EventType.DEVICE_TOPOLOGY_CHANGED)) {
            @Override
            public Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, DeviceService deviceService) {
                return this.currentStateFor((DeviceTopologyChangedEvent) event.getSource(), finiteStateMachine, deviceService);
            }
        };

        private EnumSet<EventType> eventTypes;

        Extractor(EnumSet<EventType> eventTypes) {
            this.eventTypes = eventTypes;
        }

        static Extractor from(com.elster.jupiter.events.EventType eventType) {
            return Stream
                    .of(values())
                    .filter(e -> e.topics().contains(eventType.getTopic()))
                    .findFirst()
                    .get();
        }

        private Set<String> topics() {
            return this.eventTypes.stream().map(EventType::topic).collect(Collectors.toSet());
        }

        protected Optional<CurrentState> currentStateFor(ConnectionTaskCompletionEventInfo eventSource, FiniteStateMachine finiteStateMachine, DeviceService deviceService) {
            return this.currentStateFor(deviceService.findDeviceById(eventSource.getDeviceIdentifier()), finiteStateMachine, deviceService);
        }

        protected Optional<CurrentState> currentStateFor(DeviceTopologyChangedEvent eventSource, FiniteStateMachine finiteStateMachine, DeviceService deviceService) {
            Optional<Device> device = deviceService.findDeviceByIdentifier(eventSource.getMasterDevice());
            return this.currentStateFor(device, finiteStateMachine, deviceService);
        }

        private Optional<CurrentState> currentStateFor(Optional<Device> device, FiniteStateMachine finiteStateMachine, DeviceService deviceService) {
            if (device.isPresent()) {
                State state = device.get().getState();
                if (state.getFiniteStateMachine().getId() == finiteStateMachine.getId()) {
                    CurrentState currentState = new CurrentState();
                    currentState.sourceId = String.valueOf(device.get().getId());
                    currentState.sourceType = Device.class.getName();
                    currentState.name = state.getName();
                    return Optional.of(currentState);
                } else {
                    // Device's state is not managed by a life cycle or not managed by the specified state machine
                    return Optional.empty();
                }
            } else {
                // Device was deleted just after the event was produced
                return Optional.empty();
            }
        }

        public abstract Optional<CurrentState> extractFrom(LocalEvent event, FiniteStateMachine finiteStateMachine, DeviceService deviceService);

    }

}