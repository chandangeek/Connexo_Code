package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@XmlRootElement
public class IntervalFieldInfos {
    public int total;
    public List<IntervalFieldInfo> intervals = new ArrayList<>();

    private void add(Integer time, String name, Integer macro) {
        IntervalFieldInfo intervalFieldInfo = new IntervalFieldInfo();
        intervalFieldInfo.name = name;
        intervalFieldInfo.time = time;
        intervalFieldInfo.macro = macro;
        intervals.add(intervalFieldInfo);
        total++;
    }

    public IntervalFieldInfos from(Set<TimeAttribute> timeAttributes, Set<MacroPeriod> macroPeriods, Thesaurus thesaurus) {
        add(TimeAttribute.NOTAPPLICABLE.getId(), thesaurus.getString(TranslationKeys.Keys.TIME_ATTRIBUTE_KEY_PREFIX + TimeAttribute.NOTAPPLICABLE.getId(),
                TimeAttribute.NOTAPPLICABLE.getDescription()), MacroPeriod.NOTAPPLICABLE.getId());
        timeAttributes.stream()
                .sorted((ta1, ta2) -> Integer.compare(ta1.getMinutes(), ta2.getMinutes()))
                .filter(ta -> ta.getId() != 0)
                .forEach(ta -> this.add(ta.getId(), thesaurus.getString(TranslationKeys.Keys.TIME_ATTRIBUTE_KEY_PREFIX + ta.getId(), ta.getDescription()), null));
        macroPeriods.stream()
                .sorted((mp1, mp2) -> mp1.getDescription().compareTo(mp2.getDescription()))
                .filter(mp -> mp.getId() != 0)
                .forEach(mp -> this.add(null, thesaurus.getString(TranslationKeys.Keys.MACRO_PERIOD_KEY_PREFIX + mp.getId(), mp.getDescription()), mp.getId()));
        return this;
    }
}