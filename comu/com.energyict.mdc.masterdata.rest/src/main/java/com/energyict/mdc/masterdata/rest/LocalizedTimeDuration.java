package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.masterdata.rest.impl.MessageSeeds;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class LocalizedTimeDuration {

    public static final Map<Integer, LocalizedTimeDuration> intervals;

    static {
        Integer i = 0;
        intervals = new HashMap<>(11);
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTE));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(2, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(3, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(5, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(10, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(15, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(20, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(30, TimeDuration.MINUTES), MessageSeeds.TIME_MINUTES));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.HOURS), MessageSeeds.TIME_HOUR));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.DAYS), MessageSeeds.TIME_DAY));
        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.WEEKS), MessageSeeds.TIME_WEEK));
    }

    private TimeDuration timeDuration;
    private MessageSeeds localizedUnit;

    public LocalizedTimeDuration(TimeDuration timeDuration, MessageSeeds localizedUnit) {
        this.timeDuration = timeDuration;
        this.localizedUnit = localizedUnit;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }

    public String toString(Thesaurus thesaurus) {
        return String.format(thesaurus.getString(localizedUnit.getKey(), localizedUnit.getDefaultFormat()), timeDuration.getCount());
    }

    public static class TimeDurationInfo {
        public int id;
        public String name;
    }

    public static class Adapter extends XmlAdapter<TimeDurationInfo, TimeDuration> {

        @Override
        public TimeDuration unmarshal(TimeDurationInfo info) throws Exception {
            LocalizedTimeDuration duration = intervals.get(info.id);
            return duration != null ? duration.getTimeDuration() : null;
        }

        @Override
        public TimeDurationInfo marshal(TimeDuration timeDuration) throws Exception {
            for (Map.Entry<Integer, LocalizedTimeDuration> durationEntry : intervals.entrySet()) {
                if (durationEntry.getValue().getTimeDuration().equals(timeDuration)){
                    TimeDurationInfo info = new TimeDurationInfo();
                    info.id = durationEntry.getKey();
                    return info;
                }
            }
            return null;
        }
    }

}
