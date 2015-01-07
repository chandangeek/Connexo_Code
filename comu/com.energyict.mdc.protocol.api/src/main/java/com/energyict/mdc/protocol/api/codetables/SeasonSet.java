package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.NamedBusinessObject;

import java.time.Instant;
import java.util.List;

public interface SeasonSet extends NamedBusinessObject {

    public List<Season> getSeasons();

    /**
     * returns true if the given date is contained in the given season('s period)
     *
     * @param date   the date to test
     * @param season the season to check
     * @return true if the given date is contained in the given season('s period)
     */
    public boolean isDateInSeason(Instant date, Season season);

    /**
     * returns the season the given date is contained in
     *
     * @param date the date to test
     * @return the season the given date is contained in
     */
    public Season getSeason(Instant date);

}