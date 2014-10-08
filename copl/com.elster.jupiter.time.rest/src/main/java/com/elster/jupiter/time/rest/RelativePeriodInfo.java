package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.RelativePeriod;

public class RelativePeriodInfo {
    public Long id;
    public String name;
    public RelativeDateInfo from;
    public RelativeDateInfo to;

    public RelativePeriodInfo() {

    }

    public RelativePeriodInfo(RelativePeriod relativePeriod) {
        this.id = relativePeriod.getId();
        this.name = relativePeriod.getName();
        this.from = new RelativeDateInfo(relativePeriod.getRelativeDateFrom());
        this.to = new RelativeDateInfo(relativePeriod.getRelativeDateTo());
    }

    public RelativePeriodInfo(Long id, String name, RelativeDateInfo from, RelativeDateInfo to) {
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
    }
}
