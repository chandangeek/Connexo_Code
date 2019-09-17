package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.SmartMeterMeterReadingDocumentERPCancellationRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationrequest.SmrtMtrMtrRdngDocERPCanclnReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class MeterReadingDocumentCancellationRequestEndpoint extends AbstractCancellationRequestEndpoint
        implements SmartMeterMeterReadingDocumentERPCancellationRequestCIn {

    @Inject
    MeterReadingDocumentCancellationRequestEndpoint(EndPointConfigurationService endPointConfigurationService, Thesaurus thesaurus,
                                                    ServiceCallService serviceCallService, Clock clock, OrmService ormService) {
        super(endPointConfigurationService, serviceCallService, thesaurus, clock, ormService);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void smartMeterMeterReadingDocumentERPCancellationRequestCIn(SmrtMtrMtrRdngDocERPCanclnReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            if (!isAnyActiveEndpoint(MeterReadingDocumentCancellationConfirmation.NAME)) {
                throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        MeterReadingDocumentCancellationConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(MeterReadingDocumentCancellationRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });
    }

}
