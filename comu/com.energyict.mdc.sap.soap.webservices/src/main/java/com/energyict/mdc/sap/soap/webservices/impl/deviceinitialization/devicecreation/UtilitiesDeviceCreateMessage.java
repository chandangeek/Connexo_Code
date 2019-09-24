/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.PartyInternalID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.ProductInternalID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce;

import java.time.Instant;
import java.util.Optional;

public class UtilitiesDeviceCreateMessage {
    private String serialId;
    private String deviceId;
    private String deviceType;
    private Instant shipmentDate;
    private String manufacturer;
    private String modelNumber;

    static UtilitiesDeviceCreateMessage.Builder builder() {
        return new UtilitiesDeviceCreateMessage().new Builder();
    }

    public boolean isValid() {
        return serialId != null && deviceId != null &&
                deviceType != null && shipmentDate != null;
    }

    public String getSerialId() {
        return serialId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Instant getShipmentDate() {
        return shipmentDate;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceCreateMessage.Builder from(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            setSerialId(getSerialId(requestMessage));
            setDeviceId(getDeviceId(requestMessage));
            setDeviceType(getDeviceType(requestMessage));
            setShipmentDate(requestMessage.getStartDate());
            setManufacturer(getManufacturer(requestMessage));
            setModelNumber(getModelNumber(requestMessage));
            return this;
        }

        public UtilitiesDeviceCreateMessage build() {
            return UtilitiesDeviceCreateMessage.this;
        }

        private void setSerialId(String serialId) {
            UtilitiesDeviceCreateMessage.this.serialId = serialId;
        }

        private void setDeviceId(String deviceId) {
            UtilitiesDeviceCreateMessage.this.deviceId = deviceId;
        }

        private void setDeviceType(String deviceType) {
            UtilitiesDeviceCreateMessage.this.deviceType = deviceType;
        }

        private void setShipmentDate(Instant deviceId) {
            UtilitiesDeviceCreateMessage.this.shipmentDate = deviceId;
        }

        private void setManufacturer(String manufacturer) {
            UtilitiesDeviceCreateMessage.this.manufacturer = manufacturer;
        }

        private void setModelNumber(String modelNumber) {
            UtilitiesDeviceCreateMessage.this.modelNumber = modelNumber;
        }

        private String getDeviceId(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getID())
                    .map(UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getSerialId(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getSerialID())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceType(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getMaterialID())
                    .map(ProductInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getManufacturer(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
                    .map(UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
                    .map(PartyInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getModelNumber(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
                    .map(UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getSerialID)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

}
