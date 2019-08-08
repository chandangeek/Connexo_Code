/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import java.time.Instant;

public class MeasurementTaskAssignmentChangeRequestRole {
    public Instant getStartDateTime() {
        return startDateTime;
    }

    public Instant getEndDateTime() {
        return endDateTime;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getLrn() {
        return lrn;
    }

    Instant startDateTime;
    Instant endDateTime;
    String roleCode;
    String lrn;

    public MeasurementTaskAssignmentChangeRequestRole(Instant startDateTime, Instant endDateTime, String roleCode, String lrn) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.roleCode = roleCode;
        this.lrn = lrn;
    }
}
