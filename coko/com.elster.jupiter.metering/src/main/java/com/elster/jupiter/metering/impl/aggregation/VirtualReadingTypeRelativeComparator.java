/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.Comparator;

/**
 * Compares {@link VirtualReadingType}s relatively to a target VirtualReadingType.
 * That means that two VirtualReadingType may sort differently
 * when another target VirtualReadingType is involved.
 * <br>
 * Note that it is assumed that all VirtualReadingType that are compared
 * were obtained from a matching algorithm against the target VirtualReadingType
 * and that they are in some way or another compatible with the target VirtualReadingType
 * <br>
 * When x and y both use the same unit (with same multiplier) as the target,
 * the sort order is determined by the interval length, i.e.
 * smaller interval lengths sort first.<br>
 * When x and y use the same unit but different multiplier as the target,
 * the sort order is first determined by the calculation errors introduced
 * by the multiplier conversion and then by the interval length.
 * Bigger multiplier require division and that may introduce rounding errors.<br>
 * When x and y use different but compatible units as the target,
 * the sort order is first determined by the calculation errors introduced
 * by the unit conversion, then by the calculation errors introduced by the
 * multiplier conversion and then by the interval length.
 * Flow to volume conversion (e.g. W to Wh) requires division and that may
 * introduce rounding errors.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-03 (08:34)
 */
class VirtualReadingTypeRelativeComparator implements Comparator<VirtualReadingType> {
    private final VirtualReadingType target;

    VirtualReadingTypeRelativeComparator(VirtualReadingType target) {
        this.target = target;
    }

    @Override
    public int compare(VirtualReadingType x, VirtualReadingType y) {
        if (x.equals(this.target)) {
            if (y.equals(this.target)) {
                return 0;
            } else {
                return -1;
            }
        } else if (y.equals(this.target)) {
            return 1;
        } else if (x.equalsIgnoreCommodity(this.target)) {
            if (y.equalsIgnoreCommodity(this.target)) {
                return this.compareCommodity(x, y);
            } else {
                return -1;
            }
        } else if (y.equalsIgnoreCommodity(this.target)) {
            return 1;
        } else if (this.bothSameUnitAndMultiplierAsTarget(x, y)) {
            return comparetToIntervalThenCommodity(x, y);
        } else if (this.bothSameUnitAsTarget(x, y)) {
            return this.compareToMultiplierThenIntervalThenCommodity(x, y);
        } else {
            /* Remember the assumption that x and y have been produced by a matching
             * algorithm against the target so they are in some way compatible with the target. */
            if (this.target.isFlowRelated()) {
                if (x.isFlowRelated()) {
                    if (y.isFlowRelated()) {
                        // Both are flow related, consider them equal
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (y.isFlowRelated()) {
                    /* Was already established that x is not flow related
                     * but now that we are sure y is flow related
                     * we favour y over x to avoid volume to flow conversion. */
                    return 1;
                } else {
                    /* Neither of the two are flow related but they are somehow
                     * combined in a formula that may end up being flow related.
                     * No preference in sorting. */
                    return 0;
                }
            } else if (this.target.isVolumeRelated()) {
                // Remember that flow to volume conversion requires division
                if (x.isVolumeRelated()) {
                    if (y.isVolumeRelated()) {
                        // Both are volume related, consider them equal
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (y.isVolumeRelated()) {
                    /* Was already established that x is not volume related
                     * but now that we are sure y is volume related
                     * we favour y over x to avoid flow to volume conversion. */
                    return 1;
                } else {
                    /* Neither of the two are flow related but they are somehow
                     * combined in a formula that may end up being volumne related.
                     * No preference in sorting. */
                    return 0;
                }
            } else {
                /* Target is neither flow nor volume related
                 * but if the matching algorithm produces
                 * compatible reading types, we can assume
                 * both reading types have the same dimension.
                 * Calculation errors will relate to unit conversion
                 * and multiplier conversion. The latter is the
                 * only one that we consider for now. */
                return this.compareToMultiplierThenIntervalThenCommodity(x, y);
            }
        }
    }

    private int compareToMultiplierThenIntervalThenCommodity(VirtualReadingType x, VirtualReadingType y) {
        int targetMultiplier = this.target.getUnitMultiplier().getMultiplier();
        int xToTarget = x.getUnitMultiplier().getMultiplier() - targetMultiplier;
        int yToTarget = y.getUnitMultiplier().getMultiplier() - targetMultiplier;
        int multiplierCompareResult = Integer.compare(xToTarget, yToTarget);
        if (multiplierCompareResult == 0) {
            return this.comparetToIntervalThenCommodity(x, y);
        } else {
            return multiplierCompareResult;
        }
    }

    private int comparetToIntervalThenCommodity(VirtualReadingType x, VirtualReadingType y) {
        int intervalLengthCompareResult = x.getIntervalLength().compareTo(y.getIntervalLength());
        if (intervalLengthCompareResult == 0) {
            // Favour the primary metered one
            return this.compareCommodity(x, y);
        } else {
            return intervalLengthCompareResult;
        }
    }

    private int compareCommodity(VirtualReadingType x, VirtualReadingType y) {
        if (x.isPrimaryMetered()) {
            if (y.isPrimaryMetered()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (y.isPrimaryMetered()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private boolean bothSameUnitAndMultiplierAsTarget(VirtualReadingType x, VirtualReadingType y) {
        return this.sameUnitAndMultiplierAsTarget(x) && this.sameUnitAndMultiplierAsTarget(y);
    }

    private boolean sameUnitAndMultiplierAsTarget(VirtualReadingType other) {
        return this.sameUnitAsTarget(other)
                && other.getUnitMultiplier().equals(this.target.getUnitMultiplier());
    }

    private boolean bothSameUnitAsTarget(VirtualReadingType x, VirtualReadingType y) {
        return this.sameUnitAsTarget(x) && this.sameUnitAsTarget(y);
    }

    private boolean sameUnitAsTarget(VirtualReadingType other) {
        return other.getUnit().equals(this.target.getUnit());
    }

}