/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.time.Instant;
import java.util.Optional;

public class MeterChangeMessage extends AbstractSapMessage {

    private static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesDeviceID";
    private static final String SERIAL_ID_XML_NAME = "SerialID";
    private static final String MANUFACTURER = "PartyInternalID";
    private static final String MANUFACTURER_MODEL = "ManufacturerModelID";

    private String requestId;
    private String uuid;
    private String serialId;
    private String deviceId;
    private String materialId;
    private String manufacturer;
    private String manufacturerModel;
    private String manufacturerSerialId;
    private String activationGroupAMIFunctions;
    private String meterFunctionGroup;
    private String attributeMessage;
    private String characteristicsId;
    private String characteristicsValue;
    private Instant shipmentDate;


    static MeterChangeMessage.Builder builder() {
        return new MeterChangeMessage().new Builder();
    }

    public boolean isValid() {
        return (requestId != null || uuid != null) && serialId != null && deviceId != null && manufacturer != null && manufacturerModel != null;
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

    public Instant getShipmentDate() {
        return shipmentDate;
    }

    public class Builder {

        private Builder() {
        }

        public MeterChangeMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqMsg requestMessage) {
            setRequestId(getRequestId(requestMessage.getMessageHeader()));
            setUuid(getUuid(requestMessage.getMessageHeader()));
            Optional.ofNullable(requestMessage.getUtilitiesDevice())
                    .ifPresent(request -> {
                        setSerialId(getSerialId(request));
                        setDeviceId(getDeviceId(request));
                        setMaterialId(getMaterialId(request));
                        setManufacturer(getManufacturer(request));
                        setManufacturerModel(getManufacturerModel(request));
                        setManufacturerSerialId(getManufacturerSerialId(request));
                        setActivationGroupAMIFunctions(getActivationGroupAMIFunctions(request));
                        setMeterFunctionGroup(getMeterFunctionGroup(request));
                        setAttributeMessage(getAttributeMessage(request));
                        setCharacteristicsId(getCharacteristicsId(request));
                        setCharacteristicsValue(getCharacteristicsValue(request));
                        setShipmentDate(Instant.now());
                    });
            return this;
        }

        public MeterChangeMessage build(Thesaurus thesaurus) {
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }

            if (deviceId == null) {
                addMissingField(UTILITIES_DEVICE_ID_XML_NAME);
            }
            if (serialId == null) {
                addMissingField(SERIAL_ID_XML_NAME);
            }
            if (manufacturer == null) {
                addMissingField(MANUFACTURER);
            }
            if (manufacturerModel == null) {
                addMissingField(MANUFACTURER_MODEL);
            }
            return MeterChangeMessage.this;
        }

        private void setRequestId(String requestId) {
            MeterChangeMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            MeterChangeMessage.this.uuid = uuid;
        }

        private void setSerialId(String serialId) {
            MeterChangeMessage.this.serialId = serialId;
        }

        private void setDeviceId(String deviceId) {
            MeterChangeMessage.this.deviceId = deviceId;
        }

        private void setMaterialId(String materialId) {
            MeterChangeMessage.this.materialId = materialId;
        }

        private void setManufacturer(String manufacturer) {
            MeterChangeMessage.this.manufacturer = manufacturer;
        }

        private void setManufacturerModel(String manufacturerModel) {
            MeterChangeMessage.this.manufacturerModel = manufacturerModel;
        }

        private void setManufacturerSerialId(String manufacturerSerialId) {
            MeterChangeMessage.this.manufacturerSerialId = manufacturerSerialId;
        }

        private void setActivationGroupAMIFunctions(String activationGroupAMIFunctions) {
            MeterChangeMessage.this.activationGroupAMIFunctions = activationGroupAMIFunctions;
        }

        private void setMeterFunctionGroup(String meterFunctionGroup) {
            MeterChangeMessage.this.meterFunctionGroup = meterFunctionGroup;
        }

        private void setAttributeMessage(String attributeMessage) {
            MeterChangeMessage.this.attributeMessage = attributeMessage;
        }

        private void setCharacteristicsId(String characteristicsId) {
            MeterChangeMessage.this.characteristicsId = characteristicsId;
        }

        private void setCharacteristicsValue(String characteristicsValue) {
            MeterChangeMessage.this.characteristicsValue = characteristicsValue;
        }

        private void setShipmentDate(Instant shipmentDate) {
            MeterChangeMessage.this.shipmentDate = shipmentDate;
        }


        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getID())
                    .map(id -> id.getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getSerialID())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getMaterialId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getMaterialID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.ProductInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getManufacturer(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqIndivMatlMfrInfo::getPartyInternalID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.PartyInternalID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getManufacturerModel(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
//            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.PartyInternalID::getValue)
//                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
//                    .orElse(null);
            return "A1800";
        }


        private String getManufacturerSerialId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return Optional.ofNullable(msg.getIndividualMaterialManufacturerInformation())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqIndivMatlMfrInfo::getSerialID)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getActivationGroupAMIFunctions(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return "AmiFunctions" + Math.random();
        }

        private String getMeterFunctionGroup(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return "FunctionGroup" + Math.random();
        }

        private String getAttributeMessage(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return "AttributeMessage" + Math.random();
        }

        private String getCharacteristicsId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return "CharacteristicsId" + Math.random();
        }

        private String getCharacteristicsValue(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqUtilsDvce msg) {
            return "CharacteristicsValue" + Math.random();
        }

    }

}
