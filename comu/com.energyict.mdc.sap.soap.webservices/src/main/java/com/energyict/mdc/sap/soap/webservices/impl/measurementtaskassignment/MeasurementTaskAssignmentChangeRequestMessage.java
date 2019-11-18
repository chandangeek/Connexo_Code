/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilitiesMeasurementTaskID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilitiesTimeSeriesAssignmentRoleCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqMsmtTskAssgmtRole;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqUtilsTmeSers;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

public class MeasurementTaskAssignmentChangeRequestMessage {

    private String id;
    private String uuid;
    private String profileId;
    private List<MeasurementTaskAssignmentChangeRequestRole> roles;
    private Thesaurus thesaurus;

    private MeasurementTaskAssignmentChangeRequestMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProfileId() {
        return profileId;
    }

    public List<MeasurementTaskAssignmentChangeRequestRole> getRoles() {
        return roles;
    }

    public boolean isValid() {
        return id != null || uuid != null;
    }

    public boolean arePeriodsValid() {
        for (MeasurementTaskAssignmentChangeRequestRole role : roles) {
            if (!role.getStartDateTime().isBefore(role.getEndDateTime())) {
                return false;
            }
        }
        return true;
    }

    public static Builder builder(Thesaurus thesaurus) {
        return new MeasurementTaskAssignmentChangeRequestMessage(thesaurus).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(UtilsTmeSersERPMsmtTskAssgmtChgReqMsg requestMessage, String id, String uuid) {
            setId(id);
            setUuid(uuid);
            Optional.ofNullable(requestMessage.getUtilitiesTimeSeries())
                    .ifPresent(сhangeRequest -> {
                        setProfileId(getProfileId(сhangeRequest));
                    });

            Optional.ofNullable(requestMessage.getUtilitiesTimeSeries())
                    .ifPresent(сhangeRequest -> {
                        setRoles(getRoles(сhangeRequest));
                    });
            return this;
        }

        public Builder setId(String id) {
            MeasurementTaskAssignmentChangeRequestMessage.this.id = id;
            return this;
        }

        public Builder setUuid(String uuid) {
            MeasurementTaskAssignmentChangeRequestMessage.this.uuid = uuid;
            return this;
        }

        public Builder setProfileId(String profileId) {
            MeasurementTaskAssignmentChangeRequestMessage.this.profileId = profileId;
            return this;
        }

        public Builder setRoles(List<MeasurementTaskAssignmentChangeRequestRole> roles) {
            MeasurementTaskAssignmentChangeRequestMessage.this.roles = roles;
            return this;
        }

        public MeasurementTaskAssignmentChangeRequestMessage build() {
            return MeasurementTaskAssignmentChangeRequestMessage.this;
        }

        private String getProfileId(UtilsTmeSersERPMsmtTskAssgmtChgReqUtilsTmeSers changeRequest) {
            return Optional.ofNullable(changeRequest.getID())
                    .map(UtilitiesTimeSeriesID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "UtilitiesTimeSeriesID"));
        }

        private List<MeasurementTaskAssignmentChangeRequestRole> getRoles(UtilsTmeSersERPMsmtTskAssgmtChgReqUtilsTmeSers changeRequest) {
            return changeRequest.getMeasurementTaskAssignmentRole().stream()
                    .map(r -> getRole(r))
                    .collect(Collectors.toList());
        }

        private MeasurementTaskAssignmentChangeRequestRole getRole(UtilsTmeSersERPMsmtTskAssgmtChgReqMsmtTskAssgmtRole role) {
            LocalTime startTime = Optional.ofNullable(role.getStartTime())
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "StartTime"));
            Instant startDate = Optional.ofNullable(role.getStartDate())
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "StartDate"));
            LocalTime endTime = Optional.ofNullable(role.getEndTime())
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "EndTime"));
            Instant endDate = Optional.ofNullable(role.getEndDate())
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "EndDate"));
            String roleCode = getRoleCode(role.getUtilitiesTimeSeriesAssignmentRoleCode());
            String lrn = getLrn(role.getUtilitiesMeasurementTaskID());
            return new MeasurementTaskAssignmentChangeRequestRole(
                    startTime.toNanoOfDay() > 0 ? startDate.plus(1, DAYS) : startDate,
                    endTime.toNanoOfDay() > 0 ? endDate.plus(1, DAYS) : endDate,
                    roleCode, lrn);
        }

        private String getRoleCode(UtilitiesTimeSeriesAssignmentRoleCode roleCode) {
            return Optional.ofNullable(roleCode)
                    .map(UtilitiesTimeSeriesAssignmentRoleCode::getValue)
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "UtilitiesTimeSeriesAssignmentRoleCode"));
        }

        private String getLrn(UtilitiesMeasurementTaskID taskId) {
            return Optional.ofNullable(taskId)
                    .map(UtilitiesMeasurementTaskID::getValue)
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.MISSING_REQUIRED_TAG, "UtilitiesMeasurementTaskID"));
        }
    }
}
