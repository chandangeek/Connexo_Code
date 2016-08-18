package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.CurrentStateExtractor;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.orm.CacheClearedEvent;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.streams.Functions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filters events that relate to {@link StandardStateTransitionEventType}s,
 * transforms them to {@link StateTransitionTriggerEvent}s
 * and finally delegates those to the {@link StateTransitionTriggerEventTopicHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (13:38)
 */
@Component(name="com.elster.jupiter.fsm.events.standard", service = Subscriber.class, immediate = true)
@SuppressWarnings("unused")
public class StandardEventHandler implements Subscriber {
    private volatile EventService eventService;
    private volatile ServerFiniteStateMachineService stateMachineService;
    private volatile List<CurrentStateExtractor> currentStateExtractors = new CopyOnWriteArrayList<>();
    private volatile Map<EventType, Optional<StandardStateTransitionEventType>> eventTypeCache = new ConcurrentHashMap<>();
    private volatile Map<StandardStateTransitionEventType, List<FiniteStateMachine>> finiteStateMachineCache = new ConcurrentHashMap<>();
    private volatile Clock clock;

    // For OSGi pruposes
    public StandardEventHandler() {
    }

    // For testing purposes or friendly components
    @Inject
    public StandardEventHandler(EventService eventService, ServerFiniteStateMachineService stateMachineService, Clock clock) {
        this();
        this.setEventService(eventService);
        this.setStateMachineService(stateMachineService);
        setClock(clock);
    }

    @Override
    public void handle(Object notification, Object... notificationDetails) {
        if (notification instanceof LocalEvent) {
            onEvent((LocalEvent) notification, notificationDetails);
        } else if (notification instanceof CacheClearedEvent) {
            handleClearCacheEvent((CacheClearedEvent) notification);
        }

    }

    private void handleClearCacheEvent(CacheClearedEvent event) {
        if (FiniteStateMachineService.COMPONENT_NAME.equals(event.getComponentName())) {
            eventTypeCache.clear();
            finiteStateMachineCache.clear();
        }
    }

    @Override
    public Class<?>[] getClasses() {
        return new Class<?>[]{LocalEvent.class, CacheClearedEvent.class};
    }


    private void onEvent(LocalEvent event, Object... eventDetails) {
        this.findStandardStateTransitionEventType(event.getType()).ifPresent(eventType -> this.handle(event, eventType));
    }

    private Optional<StandardStateTransitionEventType> findStandardStateTransitionEventType(EventType eventType) {
        if (eventType.isEnabledForUseInStateMachines()) {
            return retrieveStandardStateTransitionEventType(eventType);
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<StandardStateTransitionEventType> retrieveStandardStateTransitionEventType(EventType eventType) {
        if (!eventTypeCache.containsKey(eventType)) {
            eventTypeCache.put(eventType, this.stateMachineService.findStandardStateTransitionEventType(eventType));
        }
        return eventTypeCache.get(eventType);
    }

    private void handle(LocalEvent event, StandardStateTransitionEventType eventType) {
        List<StateTransitionTriggerEvent> triggerEvents = new ArrayList<>();
        for (FiniteStateMachine stateMachine : getFiniteStateMachinesForEventType(eventType)) {
            triggerEvents.addAll(this.currentStateExtractors.stream()
                    .map(e -> e.extractFrom(event, stateMachine))
                    .flatMap(Functions.asStream())
                    .map(cs -> this.newTriggerEvent(event, eventType, stateMachine, cs))
                    .collect(Collectors.toList()));
        }
        triggerEvents.forEach(StateTransitionTriggerEvent::publish);
    }

    private List<FiniteStateMachine> getFiniteStateMachinesForEventType(StandardStateTransitionEventType eventType) {
        if (!finiteStateMachineCache.containsKey(eventType)) {
            finiteStateMachineCache.put(eventType, this.stateMachineService.findFiniteStateMachinesUsing(eventType));
        }
        return finiteStateMachineCache.get(eventType);
    }

    private StateTransitionTriggerEventImpl newTriggerEvent(LocalEvent event, StandardStateTransitionEventType eventType, FiniteStateMachine stateMachine, CurrentStateExtractor.CurrentState cs) {
        return new StateTransitionTriggerEventImpl(this.eventService)
                .initialize(
                        eventType,
                        stateMachine,
                        cs.sourceId,
                        cs.sourceType,
                        Instant.now(clock),
                        propertiesOf(event),
                        cs.name);
    }

    private Map<String, Object> propertiesOf(LocalEvent event) {
        return this.propertiesOf(event.toOsgiEvent());
    }

    private Map<String, Object> propertiesOf(Event event) {
        return Stream.of(event.getPropertyNames())
                .collect(Collectors.toMap(
                        Function.identity(),
                        event::getProperty));
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setStateMachineService(ServerFiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCurrentStateExtractor(CurrentStateExtractor currentStateExtractor) {
        this.currentStateExtractors.add(currentStateExtractor);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @SuppressWarnings("unused")
    public void removeCurrentStateExtractor(CurrentStateExtractor currentStateExtractor) {
        this.currentStateExtractors.remove(currentStateExtractor);
    }

}