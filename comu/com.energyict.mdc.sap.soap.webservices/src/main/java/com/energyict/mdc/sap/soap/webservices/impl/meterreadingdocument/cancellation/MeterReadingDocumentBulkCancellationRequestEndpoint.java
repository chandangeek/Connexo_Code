package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;

import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.SmartMeterMeterReadingDocumentERPBulkCancellationRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationrequest.SmrtMtrMtrRdngDocERPBulkCanclnReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class MeterReadingDocumentBulkCancellationRequestEndpoint extends AbstractCancellationRequestEndpoint
        implements SmartMeterMeterReadingDocumentERPBulkCancellationRequestCIn {

    @Inject
    MeterReadingDocumentBulkCancellationRequestEndpoint(EndPointConfigurationService endPointConfigurationService, Thesaurus thesaurus,
                                                        ServiceCallService serviceCallService, Clock clock, OrmService ormService) {
        super(endPointConfigurationService, serviceCallService, thesaurus, clock, ormService);
    }

    @Override
    public void smartMeterMeterReadingDocumentERPBulkCancellationRequestCIn(SmrtMtrMtrRdngDocERPBulkCanclnReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            if (!isAnyActiveEndpoint(MeterReadingDocumentBulkCancellationConfirmation.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        MeterReadingDocumentBulkCancellationConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(MeterReadingDocumentCancellationRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });
    }
}
