package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.NamedBusinessObject;

import java.util.Date;
import java.util.List;

public interface Season extends NamedBusinessObject {

    public SeasonSet getSeasonSet();

    public int getSeasonSetId();

    public List<? extends SeasonTransition> getTransitions();

    /**
     * Returns true if this date is included in this season('s period)
     *
     * @param date date to test
     * @return true if this date is included in this season('s period)
     */
    public boolean contains(Date date);

}