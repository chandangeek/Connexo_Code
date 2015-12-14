package com.energyict.mdc.protocol.api.codetables;

import java.time.Instant;

public interface SeasonTransition {

    int getId();

    int getSeasonId();

    Season getSeason();

    Instant getStartDate();

}