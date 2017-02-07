/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.Optional;

/**
 * Models the result of breaking down counters of
 * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * or {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
 * by a specified {@link BreakdownType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-06 (09:06)
 */
final class BreakdownResult {
    private BreakdownType type;
    private ServerComTaskStatus status;
    private Optional<Long> breakdownTargetId;
    private long count;

    static BreakdownResult noBreakdown(ServerComTaskStatus status, long count) {
        BreakdownResult result = new BreakdownResult();
        result.type = BreakdownType.None;
        result.status = status;
        result.breakdownTargetId = Optional.empty();
        result.count = count;
        return result;
    }

    static BreakdownResult from(BreakdownType type, ServerComTaskStatus status, long targetId, long count) {
        BreakdownResult result = new BreakdownResult();
        result.type = type;
        result.status = status;
        result.breakdownTargetId = Optional.of(targetId);
        result.count = count;
        return result;
    }

    /**
     * Processes this BreakdownResult with the specified {@link BreakdownResultProcessor}.
     * This is part of a three way dispatch mechanism between
     * BreakdownResult, BreakdownType and BreakdownResultProcessor.
     *
     * @param processor The BreakdownResultProcessor
     */
    void processWith(BreakdownResultProcessor processor) {
        this.type.process(this, processor);
    }

    TaskStatus getStatus() {
        return this.status.getPublicStatus();
    }

    long getCount() {
        return this.count;
    }

    long getBreakdownTargetId() {
        return this.breakdownTargetId.orElseThrow(() -> new UnsupportedOperationException("BreakdownType#None does not support breaking down counters by target type"));
    }

}