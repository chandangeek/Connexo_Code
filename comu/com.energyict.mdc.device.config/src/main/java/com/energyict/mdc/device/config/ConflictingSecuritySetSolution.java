package com.energyict.mdc.device.config;

/**
 * Defines a solution for mapping a conflicting SecuritySet.
 * A solution will be either to
 * <ul>
 *     <li>Remove the origin SecuritySet</li>
 *     <li>Add the destination SecuritySet</li>
 *     <li>Map the origin SecuritySet to the destination SecuritySet</li>
 * </ul>
 */
public interface ConflictingSecuritySetSolution extends ConflictingSolution<SecurityPropertySet> {
}
