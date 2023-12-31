/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.common.device.config.ConflictingSolution;
import com.energyict.mdc.common.device.config.SecurityPropertySet;

/**
 * Defines a solution for mapping a conflicting SecuritySet.
 * A solution will be either to
 * <ul>
 *     <li>Remove the origin SecuritySet</li>
 *     <li>Map the origin SecuritySet to the destination SecuritySet</li>
 * </ul>
 */
@Deprecated // This class was saved just for ORM
public interface ConflictingSecuritySetSolution extends ConflictingSolution<SecurityPropertySet> {
}
