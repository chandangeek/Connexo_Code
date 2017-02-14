/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

interface DataQualityKpiMemberType {

    String getName();

}

enum FixedDataQualityKpiMemberType implements DataQualityKpiMemberType {
    CHANNEL("CHANNEL"),
    REGISTER("REGISTER"),
    SUSPECT("SUSPECT"),
    INFORMATIVE("INFORMATIVE"),
    ADDED("ADDED"),
    EDITED("EDITED"),
    REMOVED("REMOVED"),
    ESTIMATED("ESTIMATED"),
    CONFIRMED("CONFIRMED");

    private final String name;

    FixedDataQualityKpiMemberType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class NamedDataQualityKpiMemberType implements DataQualityKpiMemberType {

    private String name;

    public NamedDataQualityKpiMemberType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NamedDataQualityKpiMemberType that = (NamedDataQualityKpiMemberType) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "KpiMemberType: " + this.name;
    }
}
