/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqUtilsTmeSers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

public class MeasurementTaskAssignmentChangeRequestMessage {

    private String id;
    private String profileId;
    private List<MeasurementTaskAssignmentChangeRequestRole> roles;

    private MeasurementTaskAssignmentChangeRequestMessage() {
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public List<MeasurementTaskAssignmentChangeRequestRole> getRoles() {
        return roles;
    }

    public boolean hasValidId() {
        return id != null;
    }

    public boolean arePeriodsValid() {
        for (MeasurementTaskAssignmentChangeRequestRole role : roles) {
            if (!role.getStartDateTime().isBefore(role.getEndDateTime())) {
                return false;
            }
        }
        return true;
    }

    public static Builder builder() {
        return new MeasurementTaskAssignmentChangeRequestMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(UtilsTmeSersERPMsmtTskAssgmtChgReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(header -> {
                        setId(getId(header));
                    });

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

        private String getId(BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getProfileId(UtilsTmeSersERPMsmtTskAssgmtChgReqUtilsTmeSers changeRequest) {
            return Optional.ofNullable(changeRequest.getID())
                    .map(UtilitiesTimeSeriesID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private List<MeasurementTaskAssignmentChangeRequestRole> getRoles(UtilsTmeSersERPMsmtTskAssgmtChgReqUtilsTmeSers changeRequest) {
            return changeRequest.getMeasurementTaskAssignmentRole().stream().map(r -> new MeasurementTaskAssignmentChangeRequestRole(
                    r.getStartTime().toNanoOfDay() > 0 ? r.getStartDate().plus(1, DAYS) : r.getStartDate(),
                    r.getEndTime().toNanoOfDay() > 0 ? r.getEndDate().plus(1, DAYS) : r.getEndDate(),
                    r.getUtilitiesTimeSeriesAssignmentRoleCode().getValue(),
                    r.getUtilitiesMeasurementTaskID().getValue()))
                    .collect(Collectors.toList());
        }
    }
}
