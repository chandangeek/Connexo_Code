package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileIntervals;
import com.energyict.mdc.masterdata.rest.impl.TranslationKeys;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LocalizedTimeDuration {

//    public static final Map<Integer, LocalizedTimeDuration> intervals;
//
//    static {
//        int i = 0;
//        intervals = new HashMap<>();
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES), TranslationKeys.TIME_MINUTES));
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(10, TimeDuration.TimeUnit.MINUTES), TranslationKeys.TIME_MINUTES));
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), TranslationKeys.TIME_MINUTES));
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(30, TimeDuration.TimeUnit.MINUTES), TranslationKeys.TIME_MINUTES));
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.TimeUnit.HOURS), TranslationKeys.TIME_HOUR));
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.TimeUnit.DAYS), TranslationKeys.TIME_DAY));
//        intervals.put(i++, new LocalizedTimeDuration(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), TranslationKeys.TIME_MONTH));
//    }

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
            return Stream.of(LoadProfileIntervals.values())
                    .filter(lpi -> lpi.ordinal() == info.id)
                    .findAny()
                    .map(LoadProfileIntervals::getTimeDuration).orElse(null);
        }

        @Override
        public TimeDurationInfo marshal(TimeDuration timeDuration) throws Exception {
            return Stream.of(LoadProfileIntervals.values())
                    .filter(lpi -> lpi.getTimeDuration().equals(timeDuration))
                    .findAny()
                    .map(loadProfileIntervals -> {
                        TimeDurationInfo info = new TimeDurationInfo();
                        info.id = loadProfileIntervals.ordinal();
                        info.asSeconds = loadProfileIntervals.getTimeDuration().getSeconds();
                        return info;
                    }).orElse(null);
        }
    }

}
