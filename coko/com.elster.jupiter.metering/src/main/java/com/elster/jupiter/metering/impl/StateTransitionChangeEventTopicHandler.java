/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.DateTimeFormatGenerator;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Responds to {@link StateTransitionChangeEvent} and effectively changes the
 * {@link com.elster.jupiter.fsm.State} of the related {@link com.elster.jupiter.metering.EndDevice}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (17:34)
 */
@Component(name = "com.elster.jupiter.metering.fsm.state.change.handler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class StateTransitionChangeEventTopicHandler implements TopicHandler {

    private Logger logger = Logger.getLogger(StateTransitionChangeEventTopicHandler.class.getName());
    private volatile Clock clock;
    private volatile FiniteStateMachineService stateMachineService;
    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;

    // For OSGi purposes
    public StateTransitionChangeEventTopicHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public StateTransitionChangeEventTopicHandler(Clock clock, FiniteStateMachineService stateMachineService, MeteringService meteringService) {
        this();
        this.setClock(clock);
        this.setStateMachineService(stateMachineService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setStateMachineService(FiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        StateTransitionChangeEvent event = (StateTransitionChangeEvent) localEvent.getSource();
        if (event.getSourceType().contains("Device")) {
            this.handle(event);
        }
        else {
            this.logger.fine(() -> "Ignoring event for id '" + event.getSourceId() + "' because it does not relate to a device but to an obejct of type " + event.getSourceType());
        }
    }

    public void handle(StateTransitionChangeEvent event) {
        String deviceId = event.getSourceId();
        try {
            Query<EndDevice> endDeviceQuery = meteringService.getEndDeviceQuery();
            Condition condition = where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId()).and(where("id").isEqualTo(deviceId));
            endDeviceQuery.select(condition)
                    .stream()
                    .findFirst()
                    .filter(endDevice -> Objects.equals(event.getNewState()
                            .getFiniteStateMachine()
                            .getId(), endDevice.getFiniteStateMachine().get().getId()))
                    .ifPresent(d -> this.handle(event, (ServerEndDevice) d));
        }
        catch (NumberFormatException e) {
            this.logger.fine(() -> "Unable to parse end device id '" + deviceId + "' as a db identifier for an EndDevice from " + StateTransitionChangeEvent.class.getSimpleName());
        }
    }

    private void handle(StateTransitionChangeEvent event, ServerEndDevice endDevice) {
        Instant effectiveTimestamp = this.effectiveTimestampFrom(event);
        State newState = event.getNewState();

        this.effectiveTimestampAfterLastStateChange(effectiveTimestamp, newState, endDevice);
        endDevice.changeState(newState, effectiveTimestamp);
    }

    private void effectiveTimestampAfterLastStateChange(Instant effectiveTimestamp, State state, ServerEndDevice endDevice) {
        Optional<Instant> lastStateChangeTimestamp = this.getLastStateChangeTimestamp(endDevice);

        if(lastStateChangeTimestamp.isPresent()) {
            if (lastStateChangeTimestamp.isPresent() && !effectiveTimestamp.isAfter(lastStateChangeTimestamp.get())) {
                throw new StateTransitionChangeEventException.UnableToChangeDeviceStateDueToTimeNotAfterLastStateChangeException(thesaurus, getStateName(state), endDevice.getName(),
                        getFormattedInstant(getLongDateFormatForCurrentUser(), effectiveTimestamp),
                        getFormattedInstant(getLongDateFormatForCurrentUser(), lastStateChangeTimestamp.get()));
            }
        }
    }

    private Optional<Instant> getLastStateChangeTimestamp(ServerEndDevice endDevice) {
        List<StateTimeSlice> stateTimeSlices = endDevice.getStateTimeline().get().getSlices();
        return this.lastSlice(stateTimeSlices).map(lastSlice -> lastSlice.getPeriod().lowerEndpoint());
    }

    private Optional<StateTimeSlice> lastSlice(List<StateTimeSlice> stateTimeSlices) {
        if (stateTimeSlices.isEmpty()) {
            // MDC device always have at least one state
            return Optional.empty();
        } else {
            return Optional.of(stateTimeSlices.get(stateTimeSlices.size() - 1));
        }
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(ent->thesaurus.getFormat(ent).format())
                .orElseGet(state::getName);
    }

    private String getFormattedInstant(DateTimeFormatter formatter, Instant time){
        return formatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault()));
    }

    private DateTimeFormatter getLongDateFormatForCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        return DateTimeFormatGenerator.getDateFormatForUser(
                DateTimeFormatGenerator.Mode.LONG,
                DateTimeFormatGenerator.Mode.LONG,
                this.userService.getUserPreferencesService(),
                this.threadPrincipalService.getPrincipal());
    }

    private Instant effectiveTimestampFrom(StateTransitionChangeEvent event) {
        Instant effectiveTimestamp = event.getEffectiveTimestamp();
        if (effectiveTimestamp == null) {
            effectiveTimestamp = this.clock.instant();
        }
        return effectiveTimestamp;
    }

    private enum DefaultState implements TranslationKey {

        IN_STOCK("dlc.default.inStock", "In stock"),
        COMMISSIONING("dlc.default.commissioning", "Commissioning"),
        ACTIVE("dlc.default.active", "Active"),
        INACTIVE("dlc.default.inactive", "Inactive"),
        DECOMMISSIONED("dlc.default.decommissioned", "Decommissioned"),
        REMOVED("dlc.default.removed", "Removed");

        private final String key;
        private final String defaultFormat;

        DefaultState(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

        /**
         * Determines the DefaultState for the specified {@link State}.
         * Will return <code>Optional.empty()</code> when the State
         * is not standard or the symbolic name does not match
         * one of the standards.
         *
         * @param state The State
         * @return The DefaultState
         */
        public static Optional<DefaultState> from(State state) {
            if (state != null && !state.isCustom()) {
                String symbolicName = state.getName();
                return Stream
                        .of(DefaultState.values())
                        .filter(d -> d.getKey().equals(symbolicName))
                        .findFirst();
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public String getTopicMatcher() {
        return this.stateMachineService.stateTransitionChangeEventTopic();
    }

}