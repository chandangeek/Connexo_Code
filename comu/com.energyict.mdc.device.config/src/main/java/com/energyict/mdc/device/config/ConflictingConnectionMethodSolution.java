/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

/**
 * Defines a solution for mapping a conflicting ConnectionMethod.
 * A solution will either be to
 * <ul>
 *     <li>Remove the origin ConnectionMethod</li>
 *     <li>Map the origin ConnectionMethod to the destination ConnectionMethod</li>
 * </ul>
 */
public interface ConflictingConnectionMethodSolution extends ConflictingSolution<PartialConnectionTask>{
}