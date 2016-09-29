package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.RelativePeriod;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/11/2014
 * Time: 9:52
 */
public class RelativePeriodInfos {

    public int total;
    public List<RelativePeriodInfo> data = new ArrayList<>();

    public RelativePeriodInfos() {
    }

    public RelativePeriodInfos(List<? extends RelativePeriod> periods) {
        periods.forEach(rp -> data.add(RelativePeriodInfo.withCategories(rp)));
    }

}