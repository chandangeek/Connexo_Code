package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.IdBusinessObject;

import java.util.Date;


public interface SeasonTransition extends IdBusinessObject {

    public int getSeasonId();

    public Season getSeason();

    public Date getStartDate();

}