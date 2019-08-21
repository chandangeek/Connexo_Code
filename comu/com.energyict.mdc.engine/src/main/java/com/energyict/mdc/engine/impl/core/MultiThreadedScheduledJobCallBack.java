/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

public interface MultiThreadedScheduledJobCallBack {

    void notifyJobExecutorFinished(MultiThreadedScheduledJobExecutorImpl jobExecutor);

    void notifyJobExecutorFinished(ParallelWorkerScheduledJob parallelWorkerScheduledJob);
}
