/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.Calendar;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class SeasonSetObject implements Serializable {

    private long id;
    private String name;
    private List<SeasonObject> seasons;

    public SeasonSetObject() {
    }

    public static SeasonSetObject from(Calendar calendar) {
        SeasonSetObject ss = new SeasonSetObject();
        ss.setId(calendar.getId());
        ss.setName(calendar.getName());
        ss.setSeasons(calendar.getPeriods().stream().map(p -> SeasonObject.from(p)).collect(Collectors.toList()));
        return ss;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeasonObject> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<SeasonObject> seasons) {
        this.seasons = seasons;
    }

    public SeasonObject getSeason(int period) throws IllegalStateException {
        for (SeasonObject season : seasons) {
            if (season.isPeriod(period)) {
                return season;
            }
        }
        throw new IllegalStateException("No season found for period [" + period + "]");
    }

    @Override
    public String toString() {
        return "SeasonSetObject" +
                "{id=" + id +
                ", name='" + name + '\'' +
                ", seasons=" + seasons +
                '}';
    }

}
