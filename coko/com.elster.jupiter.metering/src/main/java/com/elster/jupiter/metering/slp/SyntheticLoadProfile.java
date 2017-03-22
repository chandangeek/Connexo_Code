/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.slp;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Unit;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface SyntheticLoadProfile extends HasId, HasName{
    String getDescription();

    Duration getInterval();

    ReadingType getReadingType();

    /**
     * Convenience method to access the Unit from the ReadingType.
     * Should be identical to <code>getReadingType().getUnit().getUnit()</code>.
     *
     * @return The Unit from the ReadingType
     */
    Unit getUnitOfMeasure();

    Instant getStartTime();

    Period getDuration();

    void addValues(Map<Instant, BigDecimal> values);

    Optional<BigDecimal> getValue(Instant date);

    Map<Instant, BigDecimal> getValues(Range<Instant> range);

    void delete();

    /**
     * Returns a SqlFragment that selects the requested raw data of this SyntheticLoadProfile
     * for the specified period in time.
     * The specification of the fields of interest is done with the {@link Pair} class.
     * The Pair's first value is the name of the field.
     * The Pair's last value is the alias name for that field.
     *
     * @param interval The period in time
     * @param fieldSpecAndAliasNames The names of the raw data fields along with an alias name that allows you to refer to the fields in surrounding SQL constructs
     * @return The SqlFragment
     */
    SqlFragment getRawValuesSql(Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames);

}