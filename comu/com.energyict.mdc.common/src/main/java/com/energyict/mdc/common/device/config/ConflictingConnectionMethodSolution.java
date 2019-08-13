/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * Defines a solution for mapping a conflicting ConnectionMethod.
 * A solution will either be to
 * <ul>
 * <li>Remove the origin ConnectionMethod</li>
 * <li>Map the origin ConnectionMethod to the destination ConnectionMethod</li>
 * </ul>
 */
public interface ConflictingConnectionMethodSolution extends ConflictingSolution<PartialConnectionTask> {
}
