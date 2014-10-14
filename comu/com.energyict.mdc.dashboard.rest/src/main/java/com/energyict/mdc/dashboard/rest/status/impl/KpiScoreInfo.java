package com.energyict.mdc.dashboard.rest.status.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn on 10/10/14.
 */
class KpiScoreInfo {
    public String name;
    public List<List<Object>> data;

    public KpiScoreInfo() {
    }

    public KpiScoreInfo(String name) {
        this.name = name;
        data = new ArrayList<>();
    }

    public void addKpi(Date date, BigDecimal value) {
        List<Object> objects = new ArrayList<>(2);
        objects.add(date.getTime());
        objects.add(value);
        data.add(objects);
    }
}
