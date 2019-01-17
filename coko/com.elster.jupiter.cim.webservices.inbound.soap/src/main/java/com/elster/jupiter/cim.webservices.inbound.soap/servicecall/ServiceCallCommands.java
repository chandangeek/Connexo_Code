package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.meterreadings.MeterReadingFaultMessageFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.GetMeterReadingsDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.GetMeterReadingsServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.getmeterreadings.EndDevice;
import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.Name;
import ch.iec.tc57._2011.getmeterreadings.ReadingType;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceCallCommands {

    private final MeterReadingFaultMessageFactory meterReadingFaultMessageFactory;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallCommands(MeterReadingFaultMessageFactory meterReadingFaultMessageFactory, ServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.meterReadingFaultMessageFactory = meterReadingFaultMessageFactory;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    @TransactionRequired
    public ServiceCall createParentGetMeterReadingsServiceCall(String source, String replyAddress, DateTimeInterval timePeriod,
                                                                      List<ReadingType> readingTypes, List<EndDevice> endDevices) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                                                             ParentGetMeterReadingsServiceCallHandler.VERSION);
        ParentGetMeterReadingsDomainExtension parentGetMeterReadingsDomainExtension = new ParentGetMeterReadingsDomainExtension();
        parentGetMeterReadingsDomainExtension.setSource(source);
        parentGetMeterReadingsDomainExtension.setCallbackUrl(replyAddress);
        parentGetMeterReadingsDomainExtension.setTimePeriodStart(timePeriod.getStart());
        parentGetMeterReadingsDomainExtension.setTimePeriodEnd(timePeriod.getEnd());
        parentGetMeterReadingsDomainExtension.setReadingTypes(getReadingTypes(readingTypes));

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(parentGetMeterReadingsDomainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();

        for (EndDevice endDevice: endDevices) {
            /// TODO crete child service call according to available communication tasks
            createGetMeterReadingsServiceCall(parentServiceCall, endDevice);
        }

        return parentServiceCall;
    }

    @TransactionRequired
    public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
        serviceCall.requestTransition(newState);
    }

    private ServiceCall createGetMeterReadingsServiceCall(ServiceCall parent, EndDevice endDevice) {
        ServiceCallType serviceCallType = getServiceCallType(GetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                                                             GetMeterReadingsServiceCallHandler.VERSION);
        GetMeterReadingsDomainExtension getMeterReadingsDomainExtension = new GetMeterReadingsDomainExtension();
        getMeterReadingsDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        getMeterReadingsDomainExtension.setEndDeviceMRID(endDevice.getMRID());
        getMeterReadingsDomainExtension.setEndeDeviceName(endDevice.getNames().stream().map(Name::getName).findFirst().get());
        getMeterReadingsDomainExtension.setRegisters(getRegisters(endDevice));
        getMeterReadingsDomainExtension.setChannels(getChannels(endDevice));
        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(getMeterReadingsDomainExtension);
        return serviceCallBuilder.create();
    }

    private String getReadingTypes(List<ReadingType> readingTypes) {
        return readingTypes.stream().map(reading -> reading.getMRID()).collect(Collectors.joining(";"));
    }

    private String getRegisters(EndDevice endDevice) {
        /// TODO
        return "nothing";
    }

    private String getChannels(EndDevice endDevice) {
        /// TODO
        return "nothing";
    }

    private ServiceCallType getServiceCallType(String handlerName, String version) {
        return serviceCallService.findServiceCallType(handlerName, version)
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(handlerName, version)));
    }

}
