/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeConfirmationMessage;

public interface MeasurementTaskAssignmentChangeConfirmation {
    String SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATION = "SAP UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmation_C_Out";

    /**
     * Invoked by the service call when the SAP measurement task assignment change request is proceeded
     */
    void call(MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage);
}
