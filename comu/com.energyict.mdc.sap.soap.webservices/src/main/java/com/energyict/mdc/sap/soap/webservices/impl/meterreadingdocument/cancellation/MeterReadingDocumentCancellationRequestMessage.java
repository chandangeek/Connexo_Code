package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;

import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.SmrtMtrMtrRdngDocERPCanclnReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.SmrtMtrMtrRdngDocERPBulkCanclnReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeterReadingDocumentCancellationRequestMessage extends AbstractSapMessage {
    private final static String METER_DOCUMENT_XML_NAME = "UtilitiesDeviceSmartMeter";

    private String requestID;
    private String uuid;
    private boolean bulk;
    private List<String> meterReadingDocumentIds = new ArrayList<>();
    private Thesaurus thesaurus;

    private MeterReadingDocumentCancellationRequestMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public boolean isBulk() {
        return bulk;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getUuid() {
        return uuid;
    }

    public List<String> getMeterReadingDocumentIds() {
        return new ArrayList(meterReadingDocumentIds);
    }

    static MeterReadingDocumentCancellationRequestMessage.Builder builder(Thesaurus thesaurus) {
        return new MeterReadingDocumentCancellationRequestMessage(thesaurus).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingDocumentCancellationRequestMessage.Builder from(SmrtMtrMtrRdngDocERPCanclnReqMsg requestMessage) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            meterReadingDocumentIds.add(getMrdId(requestMessage));
            return this;
        }

        public MeterReadingDocumentCancellationRequestMessage.Builder from(SmrtMtrMtrRdngDocERPBulkCanclnReqMsg requestMessage) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            requestMessage.getSmartMeterMeterReadingDocumentERPBulkCancellationRequestMessage()
                    .forEach(message ->
                            meterReadingDocumentIds.add(getMrdId(message)));
            return this;
        }

        public MeterReadingDocumentCancellationRequestMessage build() {
            if (requestID == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            if (meterReadingDocumentIds.isEmpty()) {
                addMissingField(METER_DOCUMENT_XML_NAME);
            }
            return MeterReadingDocumentCancellationRequestMessage.this;
        }

        private void setRequestID(String requestID) {
            MeterReadingDocumentCancellationRequestMessage.this.requestID = requestID;
        }

        private void setUuid(String uuid) {
            MeterReadingDocumentCancellationRequestMessage.this.uuid = uuid;
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getMrdId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.SmrtMtrMtrRdngDocERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getMeterReadingDocument())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.SmrtMtrMtrRdngDocERPCanclnReqMtrRdngDoc::getID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getMrdId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.SmrtMtrMtrRdngDocERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getMeterReadingDocument())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.SmrtMtrMtrRdngDocERPCanclnReqMtrRdngDoc::getID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
