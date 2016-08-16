package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.ExecutionTimerService;

public interface IExecutionTimerService extends ExecutionTimerService {
    void deactivated(ExecutionTimerImpl executionTimer);
}
