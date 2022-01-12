/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.time.Instant;
import java.util.Optional;

public class UtilitiesDeviceCreateMessage extends AbstractSapMessage {
    private static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesDeviceID";
    private static final String SERIAL_ID_XML_NAME = "SerialID";
    private static final String MATERIAL_ID_XML_NAME = "MaterialID";
    private static final String START_DATE_XML_NAME = "StartDate";

    private String requestId;
    private String uuid;
    private String serialId;
    private String deviceId;
    private String materialId;
    private Instant shipmentDate;
    private String manufacturer;
    private String manufacturerModel;
    private String manufacturerSerialId;
    private String activationGroupAMIFunctions;
    private String meterFunctionGroup;
    private String attributeMessage;
    private String characteristicsId;
    private String characteristicsValue;

    static UtilitiesDeviceCreateMessage.Builder builder() {
        return new UtilitiesDeviceCreateMessage().new Builder();
    }

    public boolean isValid() {
        return (requestId!= null || uuid != null) && serialId != null && deviceId != null && shipmentDate != null;

    }

    public String getRequestId() {
        return requestId;
    }

    public String getUuid() {
        return uuid;
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

    public String getManufacturerModel() {
        return manufacturerModel;
    }

    public String getManufacturerSerialId() {
        return manufacturerSerialId;
    }

    public String getActivationGroupAMIFunctions() {
        return activationGroupAMIFunctions;
    }

    public String getMeterFunctionGroup() {
        return meterFunctionGroup;
    }

    public String getAttributeMessage() {
        return attributeMessage;
    }

    public String getCharacteristicsId() {
        return characteristicsId;
    }

    public String getCharacteristicsValue() {
        return characteristicsValue;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg requestMessage) {
            setRequestId(getRequestId(requestMessage.getMessageHeader()));
            setUuid(getUuid(requestMessage.getMessageHeader()));
            Optional.ofNullable(requestMessage.getUtilitiesDevice())
                    .ifPresent(request -> {
                        setSerialId(getSerialId(request));
                        setDeviceId(getDeviceId(request));
                        setMaterialId(getMaterialId(request));

                        setShipmentDate(request.getStartDate());
                        setManufacturer(getManufacturer(request));
                        setManufacturerModel(getManufacturerModel(request));
                        setManufacturerSerialId(getManufacturerSerialId(request));
                        setActivationGroupAMIFunctions(getActivationGroupAMIFunctions(request));
                        setMeterFunctionGroup(getMeterFunctionGroup(request));
                        setAttributeMessage(getAttributeMessage(request));
                        setCharacteristicsId(getCharacteristicsId(request));
                        setCharacteristicsValue(getCharacteristicsValue(request));
                    });
            return this;
        }

        public UtilitiesDeviceCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg requestMessage) {
            setRequestId(getRequestId(requestMessage.getMessageHeader()));
            setUuid(getUuid(requestMessage.getMessageHeader()));
            Optional.ofNullable(requestMessage.getUtilitiesDevice())
                    .ifPresent(request -> {
                        setSerialId(getSerialId(request));
                        setDeviceId(getDeviceId(request));
                        setMaterialId(getMaterialId(request));

                        setShipmentDate(request.getStartDate());
                        setManufacturer(getManufacturer(request));
                        setManufacturerModel(getManufacturerModel(request));
                        setManufacturerSerialId(getManufacturerSerialId(request));
                    });
            return this;
        }

        public UtilitiesDeviceCreateMessage build(Thesaurus thesaurus) {
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            if (deviceId == null) {
                addMissingField(UTILITIES_DEVICE_ID_XML_NAME);
            }
            if (serialId == null) {
                addMissingField(SERIAL_ID_XML_NAME);
            }
            if (materialId == null) {
                addMissingField(MATERIAL_ID_XML_NAME);
            }
            if (shipmentDate == null) {
                addMissingField(START_DATE_XML_NAME);
            }
            return UtilitiesDeviceCreateMessage.this;
        }

        private void setRequestId(String requestId) {
            UtilitiesDeviceCreateMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            UtilitiesDeviceCreateMessage.this.uuid = uuid;
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

        private void setManufacturerModel(String manufacturerModel) {
            UtilitiesDeviceCreateMessage.this.manufacturerModel = manufacturerModel;
        }

        private void setManufacturerSerialId(String manufacturerSerialId) {
            UtilitiesDeviceCreateMessage.this.manufacturerSerialId = manufacturerSerialId;
        }

        private void setActivationGroupAMIFunctions(String activationGroupAMIFunctions) {
            UtilitiesDeviceCreateMessage.this.activationGroupAMIFunctions = activationGroupAMIFunctions;
        }

        private void setMeterFunctionGroup(String meterFunctionGroup) {
            UtilitiesDeviceCreateMessage.this.meterFunctionGroup = meterFunctionGroup;
        }

        private void setAttributeMessage(String attributeMessage) {
            UtilitiesDeviceCreateMessage.this.attributeMessage = attributeMessage;
        }

        private void setCharacteristicsId(String characteristicsId) {
            UtilitiesDeviceCreateMessage.this.characteristicsId = characteristicsId;
        }

        private void setCharacteristicsValue(String characteristicsValue) {
            UtilitiesDeviceCreateMessage.this.characteristicsValue = characteristicsValue;
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getID())
                    .map(id -> id.getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getID())
                    .map(id -> id.getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getSerialID())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
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

        private String getMaterialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getMaterialID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.ProductInternalID::getValue)
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

        private String getManufacturer(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.PartyInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getManufacturerModel(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
//            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.PartyInternalID::getValue)
//                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
//                    .orElse(null);
            //TODO use values from wsdl
            return "A1800";
        }

        private String getManufacturerModel(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
//            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.PartyInternalID::getValue)
//                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
//                    .orElse(null);
            //TODO use values from wsdl
            return "A1800";
        }

        private String getManufacturerSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getSerialID)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getManufacturerSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getSerialID)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getActivationGroupAMIFunctions(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            //TODO use values from wsdl
            return "AmiFunctions" + Math.random();
        }

        private String getMeterFunctionGroup(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            //TODO use values from wsdl
            return "FunctionGroup" + Math.random();
        }

        private String getAttributeMessage(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            //TODO use values from wsdl
            return "AttributeMessage" + Math.random();
        }

        private String getCharacteristicsId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            //TODO use values from wsdl
            return "CharacteristicsId" + Math.random();
        }

        private String getCharacteristicsValue(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce msg) {
            //TODO use values from wsdl
            return "CharacteristicsValue" + Math.random();
        }
    }

}
