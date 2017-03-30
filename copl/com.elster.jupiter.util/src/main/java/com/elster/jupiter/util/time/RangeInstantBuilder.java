/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import com.google.common.collect.Range;

import java.time.Instant;

public final class RangeInstantBuilder {
    private RangeInstantBuilder() {
    }

    /**
     * Creates an instant range.
     * <table>
     * <tr>
     * <td><b>Range</b></td>
     * <td><b>start</b></td>
     * <td><b>end</b></td>
     * </tr>
     * <tr>
     * <td>Range.all</td>
     * <td>NULL</td>
     * <td>NULL</td>
     * </tr>
     * <tr>
     * <td>Range.closedOpen</td>
     * <td>NOT NULL</td>
     * <td>NOT NULL</td>
     * </tr>
     * <tr>
     * <td>Range.atLeast</td>
     * <td>NOT NULL</td>
     * <td>NULL</td>
     * </tr>
     * <tr>
     * <td>Range.lessThan</td>
     * <td>NULL</td>
     * <td>NOT NULL</td>
     * </tr>
     * </table>
     *
     * @param start start of interval in milliseconds, inclusive,  can be <code>null</code>
     * @param end   end of interval in milliseconds, exclusive, can be <code>null</code>
     * @return a closed-open range
     */
    public static Range<Instant> closedOpenRange(Long start, Long end) {
        Range<Instant> range;
        if (start == null && end == null) {
            range = Range.all();
        } else if (start != null && end != null) {
            range = Range.closedOpen(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end));
        } else if (start != null) {
            range = Range.atLeast(Instant.ofEpochMilli(start));
        } else {
            range = Range.lessThan(Instant.ofEpochMilli(end));
        }
        return range;
    }
}
