/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileIntervals;
import com.energyict.mdc.masterdata.rest.impl.TranslationKeys;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalizedTimeDuration {

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

    public static List<TimeDurationInfo> getAllInfos(Thesaurus thesaurus){
        List<TimeDurationInfo> all = new ArrayList<>();
        for (LoadProfileIntervals interval : LoadProfileIntervals.values()) {
            all.add(new TimeDurationInfo(interval, thesaurus));
        }
        return all;
    }

    public static class TimeDurationInfo {
        public int id;
        public String name;
        public int asSeconds;

        public TimeDurationInfo(){}

        TimeDurationInfo(LoadProfileIntervals loadProfileIntervals, Thesaurus thesaurus){
            this();
            this.id = loadProfileIntervals.ordinal();
            if (thesaurus != null)
                this.name = thesaurus.getFormat(TranslationKeys.getByKey(loadProfileIntervals.unitName())).format(loadProfileIntervals.getTimeDuration().getCount());
            else
                this.name =  loadProfileIntervals.name();

            this.asSeconds = loadProfileIntervals.getTimeDuration().getSeconds();
        }
    }

    public static class Adapter extends XmlAdapter<TimeDurationInfo, TimeDuration> {

        @Override
        public TimeDuration unmarshal(TimeDurationInfo info) throws Exception {
            return LoadProfileIntervals.values()[info.id].getTimeDuration();
        }

        @Override
        public TimeDurationInfo marshal(TimeDuration timeDuration) throws Exception {
            Optional<LoadProfileIntervals> interval = LoadProfileIntervals.fromTimeDuration(timeDuration);
            if (interval.isPresent()){
                return new TimeDurationInfo(interval.get(), null);
            }
            return null;
        }
    }

}
