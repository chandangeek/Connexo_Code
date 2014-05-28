package com.elster.jupiter.issue.datacollection.impl;

import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import org.joda.time.DateTimeConstants;

public enum TrendPeriodUnit {
    DAYS(1, MessageSeeds.TREND_PERIOD_UNIT_DAYS, DateTimeConstants.MILLIS_PER_DAY),
    HOURS(2, MessageSeeds.TREND_PERIOD_UNIT_HOURS, DateTimeConstants.MILLIS_PER_HOUR)
    ;

    private int id;
    private MessageSeed title;
    private long multiplier;

    private TrendPeriodUnit(int id, MessageSeed title, long multiplier) {
        this.id = id;
        this.title = title;
        this.multiplier = multiplier;
    }

    public int getId() {
        return id;
    }

    public String getTitle(Thesaurus thesaurus){
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        return thesaurus.getString(title.getKey(), title.getDefaultFormat());
    }

    public long getStartMillisForTrendPeriod(long trendPeriod){
        return System.currentTimeMillis() - trendPeriod * this.multiplier;
    }

    public static TrendPeriodUnit getById(int id){
        for (TrendPeriodUnit candidate : TrendPeriodUnit.values()) {
            if (candidate.id == id){
                return candidate;
            }
        }
        return null;
    }
}
