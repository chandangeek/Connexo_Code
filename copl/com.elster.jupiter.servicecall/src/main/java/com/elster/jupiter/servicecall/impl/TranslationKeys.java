/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;

/**
 * Created by bvn on 2/4/16.
 */
public enum TranslationKeys implements TranslationKey {

    SERVICE_CALL_SUBSCRIBER(ServiceCallService.SERVICE_CALLS_SUBSCRIBER_NAME, "Handle service calls"),
    SERVICE_CALL_ISSUE_SUBSCRIBER(ServiceCallService.SERVICE_CALLS_ISSUE_SUBSCRIBER_NAME, "Handle service call issues"),

    DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME("scs.standard.service.call.life.cycle", "Default service call life cycle"),

    TRANSITION_FROM_PENDING_TO_ONGOING(DefaultState.PENDING.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Activate handler"),
    TRANSITION_FROM_PENDING_TO_CANCELLED(DefaultState.PENDING.getKey() + DefaultCustomStateTransitionEventType.CANCELLED.getSymbol(), "Cancel"),
    TRANSITION_FROM_PAUSED_TO_ONGOING(DefaultState.PAUSED.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Continue"),
    TRANSITION_FROM_PAUSED_TO_CANCELLED(DefaultState.PAUSED.getKey() + DefaultCustomStateTransitionEventType.CANCELLED.getSymbol(), "Cancel"),
    TRANSITION_FROM_WAITING_TO_ONGOING(DefaultState.WAITING.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Ready to continue"),
    TRANSITION_FROM_WAITING_TO_CANCELLED(DefaultState.WAITING.getKey() + DefaultCustomStateTransitionEventType.CANCELLED.getSymbol(), "Cancel"),
    TRANSITION_FROM_ONGOING_TO_WAITING(DefaultState.ONGOING.getKey() + DefaultCustomStateTransitionEventType.WAITING.getSymbol(), "Wait for event"),
    TRANSITION_FROM_ONGOING_TO_PAUSED(DefaultState.ONGOING.getKey() + DefaultCustomStateTransitionEventType.PAUSED.getSymbol(), "Pause"),
    TRANSITION_FROM_ONGOING_TO_SUCCESSFUL(DefaultState.ONGOING.getKey() + DefaultCustomStateTransitionEventType.SUCCESSFUL
            .getSymbol(), "Successful"),
    TRANSITION_FROM_ONGOING_TO_PARTIAL_SUCCESS(DefaultState.ONGOING.getKey() + DefaultCustomStateTransitionEventType.PARTIAL_SUCCESS.getSymbol(), "partially successful"),
    TRANSITION_FROM_ONGOING_TO_FAILED(DefaultState.ONGOING.getKey() + DefaultCustomStateTransitionEventType.SCHEDULED.getSymbol(), "Schedule"),
    TRANSITION_FROM_SCHEDULED_TO_PENDING(DefaultState.SCHEDULED.getKey() + DefaultCustomStateTransitionEventType.PENDING.getSymbol(), "Enqueue for pickup"),
    TRANSITION_FROM_SCHEDULED_TO_CANCELLED(DefaultState.SCHEDULED.getKey() + DefaultCustomStateTransitionEventType.CANCELLED.getSymbol(), "Cancel"),
    TRANSITION_FROM_CREATED_TO_PENDING(DefaultState.CREATED.getKey() + DefaultCustomStateTransitionEventType.PENDING.getSymbol(), "Enqueue for pickup"),
    TRANSITION_FROM_CREATED_TO_CANCELLED(DefaultState.CREATED.getKey() + DefaultCustomStateTransitionEventType.CANCELLED.getSymbol(), "Cancel"),
    TRANSITION_FROM_CREATED_TO_SCHEDULED(DefaultState.CREATED.getKey() + DefaultCustomStateTransitionEventType.SCHEDULED.getSymbol(), "Schedule"),
    TRANSITION_FROM_CREATED_TO_REJECTED(DefaultState.CREATED.getKey() + DefaultCustomStateTransitionEventType.REJECTED.getSymbol(), "Reject"),
    TRANSITION_FROM_FAILED_TO_SCHEDULED(DefaultState.FAILED.getKey() + DefaultCustomStateTransitionEventType.SCHEDULED.getSymbol(), "Reschedule"),
    TRANSITION_FROM_PARTIAL_SUCCESS_TO_SCHEDULED(DefaultState.PARTIAL_SUCCESS.getKey() + DefaultCustomStateTransitionEventType.SCHEDULED.getSymbol(), "Reschedule"),
    TRANSITION_FROM_ONGOING_TO_CANCELLED(DefaultState.ONGOING.getKey() + DefaultCustomStateTransitionEventType.CANCELLED.getSymbol(), "Cancel"),
    TRANSITION_FROM_CANCELLED_TO_PENDING(DefaultState.CANCELLED.getKey() + DefaultCustomStateTransitionEventType.PENDING.getSymbol(), "Retry"),
    TRANSITION_FROM_FAILED_TO_PENDING(DefaultState.FAILED.getKey() + DefaultCustomStateTransitionEventType.PENDING.getSymbol(), "Retry"),
    TRANSITION_FROM_CREATED_TO_ONGOING(DefaultState.CREATED.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Activate"),
    TRANSITION_FROM_REJECT_TO_PENDING(DefaultState.REJECTED.getKey() + DefaultCustomStateTransitionEventType.PENDING.getSymbol(), "Retry"),
    TRANSITION_FROM_REJECT_TO_FAILED(DefaultState.REJECTED.getKey() + DefaultCustomStateTransitionEventType.FAILED.getSymbol(), "Failed"),
    TRANSITION_FROM_PENDING_TO_REJECT(DefaultState.PENDING.getKey() + DefaultCustomStateTransitionEventType.REJECTED.getSymbol(), "Reject"),
    TRANSITION_FROM_PENDING_TO_SUCCESS(DefaultState.PENDING.getKey() + DefaultCustomStateTransitionEventType.SUCCESSFUL.getSymbol(), "Successful"),

    TRANSITION_FROM_FAILED_TO_ONGOING(DefaultState.FAILED.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Retry"),
    TRANSITION_FROM_PARTIAL_SUCCESS_TO_ONGOING(DefaultState.PARTIAL_SUCCESS.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Retry"),
    TRANSITION_FROM_SUCCESSFUL_TO_ONGOING(DefaultState.SUCCESSFUL.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Retry"),
    TRANSITION_FROM_REJECT_TO_ONGOING(DefaultState.REJECTED.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Retry"),
    TRANSITION_FROM_CANCELLED_TO_ONGOING(DefaultState.CANCELLED.getKey() + DefaultCustomStateTransitionEventType.ONGOING.getSymbol(), "Retry"),
    SERVICE_CALL_DOMAIN_NAME("com.elster.jupiter.servicecall.ServiceCall", "Service call");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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
}
