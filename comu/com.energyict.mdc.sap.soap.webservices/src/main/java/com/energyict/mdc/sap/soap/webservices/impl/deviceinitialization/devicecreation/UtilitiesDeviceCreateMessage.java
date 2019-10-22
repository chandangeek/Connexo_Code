/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.util.Checks;

import java.time.Instant;
import java.util.Optional;

public class UtilitiesDeviceCreateMessage {
    private String serialId;
    private String deviceId;
    private String materialId;
    private Instant shipmentDate;
    private String manufacturer;
    private String modelNumber;

    static UtilitiesDeviceCreateMessage.Builder builder() {
        return new UtilitiesDeviceCreateMessage().new Builder();
    }

    public boolean isValid() {
        return serialId != null && deviceId != null &&
                materialId != null && shipmentDate != null;
    }

    public String getSerialId() {
        return serialId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getMaterialId() {
        return materialId;
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

        public UtilitiesDeviceCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            setSerialId(getSerialId(requestMessage));
            setDeviceId(getDeviceId(requestMessage));
            setMaterialId(getMaterialId(requestMessage));

            setShipmentDate(requestMessage.getStartDate());
            setManufacturer(getManufacturer(requestMessage));
            setModelNumber(getModelNumber(requestMessage));
            return this;
        }

        public UtilitiesDeviceCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            setSerialId(getSerialId(requestMessage));
            setDeviceId(getDeviceId(requestMessage));
            setMaterialId(getMaterialId(requestMessage));

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

        private void setMaterialId(String materialId) {
            UtilitiesDeviceCreateMessage.this.materialId = materialId;
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

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getSerialID())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getSerialID())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getMaterialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getMaterialID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.ProductInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getMaterialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getMaterialID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.ProductInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getManufacturer(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.PartyInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getManufacturer(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.PartyInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getModelNumber(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getSerialID)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getModelNumber(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getSerialID)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

}
