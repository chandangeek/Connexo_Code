package com.energyict.mdc.protocol.api.codetables;

import java.time.Instant;
import java.util.List;

public interface Season {

    String getName();

    int getId();

    SeasonSet getSeasonSet();

    int getSeasonSetId();

    List<? extends SeasonTransition> getTransitions();

    /**
     * Returns true if this date is included in this season('s period)
     *
     * @param date date to test
     * @return true if this date is included in this season('s period)
     */
    boolean contains(Instant date);

}