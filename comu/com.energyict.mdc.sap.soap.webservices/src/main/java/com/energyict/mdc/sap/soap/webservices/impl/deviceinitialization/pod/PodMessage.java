/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.pod;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.util.Optional;

public class PodMessage extends AbstractSapMessage {
    private static final String POD_ID_XML_NAME = "UtilitiesPointOfDeliveryPartyID";

    private String deviceId;
    private String podId;

    private PodMessage() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getPodId() {
        return podId;
    }

    static PodMessage.Builder builder() {
        return new PodMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public PodMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg podNotifMsg) {
            Optional.ofNullable(podNotifMsg.getUtilitiesMeasurementTask())
                    .ifPresent(utilitiesMeasurementTask -> {
                        setDeviceId(getDeviceId(utilitiesMeasurementTask));
                        setPodId(getPodId(utilitiesMeasurementTask));
                    });
            return this;
        }

        public PodMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg podNotifMsg) {
            Optional.ofNullable(podNotifMsg.getUtilitiesMeasurementTask())
                    .ifPresent(utilitiesMeasurementTask -> {
                        setDeviceId(getDeviceId(utilitiesMeasurementTask));
                        setPodId(getPodId(utilitiesMeasurementTask));
                    });
            return this;
        }

        public PodMessage build() {
            if (deviceId == null) {
                addMissingField(UTILITIES_DEVICE_ID_XML_NAME);
            }
            if (podId == null) {
                addMissingField(POD_ID_XML_NAME);
            }
            return PodMessage.this;
        }

        private void setDeviceId(String deviceId) {
            PodMessage.this.deviceId = deviceId;
        }

        private void setPodId(String podId) {
            PodMessage.this.podId = podId;
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk utilsMsmtTsk) {
            return Optional.ofNullable(utilsMsmtTsk.getUtilitiesDevice())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsDvce::getUtilitiesDeviceID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getPodId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk utilsMsmtTsk) {
            return Optional.ofNullable(utilsMsmtTsk.getUtilitiesPointOfDeliveryAssignment())
                    .flatMap(pod -> pod.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv::getUtilitiesPointOfDeliveryPartyID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.UtilitiesPointOfDeliveryPartyID::getValue)
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk utilsMsmtTsk) {
            return Optional.ofNullable(utilsMsmtTsk.getUtilitiesDevice())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsDvce::getUtilitiesDeviceID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getPodId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk utilsMsmtTsk) {
            return Optional.ofNullable(utilsMsmtTsk.getUtilitiesPointOfDeliveryAssignment())
                    .flatMap(pod -> pod.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv::getUtilitiesPointOfDeliveryPartyID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UtilitiesPointOfDeliveryPartyID::getValue)
                    .orElse(null);
        }
    }
}
