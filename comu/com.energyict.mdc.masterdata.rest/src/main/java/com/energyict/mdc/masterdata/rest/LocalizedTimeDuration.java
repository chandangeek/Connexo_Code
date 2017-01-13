package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileIntervals;
import com.energyict.mdc.masterdata.rest.impl.TranslationKeys;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class LocalizedTimeDuration {

    public static final Map<Integer, LocalizedTimeDuration> intervals;

    static {
        intervals = new HashMap<>();
        for (LoadProfileIntervals interval : LoadProfileIntervals.values()) {
            intervals.put(interval.ordinal(), new LocalizedTimeDuration(interval.getTimeDuration(), TranslationKeys.getByKey(interval.unitName())));
        }
    }

    private TimeDuration timeDuration;
    private TranslationKeys localizedUnit;

    public LocalizedTimeDuration(TimeDuration timeDuration, TranslationKeys localizedUnit) {
        this.timeDuration = timeDuration;
        this.localizedUnit = localizedUnit;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }

    public String toString(Thesaurus thesaurus) {
        return thesaurus.getFormat(localizedUnit).format(timeDuration.getCount());
    }

    public static class TimeDurationInfo {
        public int id;
        public String name;
        public int asSeconds;
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
                if (durationEntry.getValue().getTimeDuration().equals(timeDuration)) {
                    TimeDurationInfo info = new TimeDurationInfo();
                    info.id = durationEntry.getKey();
                    info.asSeconds = durationEntry.getValue().getTimeDuration().getSeconds();
                    return info;
                }
            }
            return null;
        }
    }

}
