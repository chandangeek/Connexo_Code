package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.meterreadings.MeterReadingFaultMessageFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.meterreadings.ReadingSourceEnum;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.task.ReadMeterChangeMessageHandlerFactory;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceCallCommands {

    private final MeterReadingFaultMessageFactory faultMessageFactory;
    private final ServiceCallService serviceCallService;
    private final MessageService messageService;
    private final Thesaurus thesaurus;


    @Inject
    public ServiceCallCommands(MeterReadingFaultMessageFactory faultMessageFactory,
                               ServiceCallService serviceCallService, MessageService messageService,
                               Thesaurus thesaurus) {
        this.faultMessageFactory = faultMessageFactory;
        this.serviceCallService = serviceCallService;
        this.messageService = messageService;
        this.thesaurus = thesaurus;
    }

    @TransactionRequired
    public ServiceCall createParentGetMeterReadingsServiceCall(String source, String replyAddress,
                                                               DateTimeInterval timePeriod,
                                                               List<EndDevice> existedEndDevices,
                                                               List<ReadingType> existedReadingTypes) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                                                             ParentGetMeterReadingsServiceCallHandler.VERSION);
        ParentGetMeterReadingsDomainExtension parentGetMeterReadingsDomainExtension = new ParentGetMeterReadingsDomainExtension();
        parentGetMeterReadingsDomainExtension.setSource(source);
        parentGetMeterReadingsDomainExtension.setCallbackUrl(replyAddress);
        parentGetMeterReadingsDomainExtension.setTimePeriodStart(timePeriod.getStart());
        parentGetMeterReadingsDomainExtension.setTimePeriodEnd(timePeriod.getEnd());
        parentGetMeterReadingsDomainExtension.setReadingTypes(getReadingTypesString(existedReadingTypes));
        parentGetMeterReadingsDomainExtension.setEndDevices(getEndDevicesString(existedEndDevices));

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(parentGetMeterReadingsDomainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        parentServiceCall.requestTransition(DefaultState.PENDING);
        parentServiceCall.requestTransition(DefaultState.ONGOING);
        if (ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
            initiateReading(parentServiceCall);
            return parentServiceCall;
        }
        boolean meterReadingRunning = false;
        for (EndDevice endDevice: existedEndDevices) {
            if (endDevice instanceof Meter) {
                Meter meter = (Meter)endDevice;
                if (isMeterReadingRequired(source, meter, existedReadingTypes,  timePeriod.getEnd())) {
                    readMeter(parentServiceCall, meter, existedReadingTypes);
                    meterReadingRunning = true;
                }
            }
        }
        if (!meterReadingRunning) {
            initiateReading(parentServiceCall);
            return parentServiceCall;
        }
        parentServiceCall.requestTransition(DefaultState.WAITING);
        return parentServiceCall;
    }

    private void initiateReading(ServiceCall serviceCall) {
        serviceCall.requestTransition(DefaultState.PAUSED);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private void readMeter(ServiceCall parentServiceCall, Meter meter, List<ReadingType> readingTypes) throws FaultMessage {
        HeadEndInterface headEndInterface = meter.getHeadEndInterface()
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.NO_HEAD_END_INTERFACE_FOUND, meter.getMRID())
                );
        CompletionOptions completionOptions = headEndInterface.readMeter(meter, readingTypes, parentServiceCall);
        messageService.getDestinationSpec(ReadMeterChangeMessageHandlerFactory.DESTINATION)
                .ifPresent(destinationSpec ->
                        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(parentServiceCall.getId()),
                        destinationSpec));
    }

    private boolean isMeterReadingRequired(String source, Meter meter, List<ReadingType> readingTypes, Instant endTime) {
        if (ReadingSourceEnum.METER.getSource().equals(source)) {
            return true;
        }
        if (ReadingSourceEnum.HYBRID.getSource().equals(source)) {
            return meter.getChannelsContainers().stream().
                    anyMatch(container -> isChannelContainerReadOutRequired(container, readingTypes, endTime));
        }
        return false;
    }

    private boolean isChannelContainerReadOutRequired(ChannelsContainer channelsContainer, List<ReadingType> readingTypes, Instant endTime) {
        Set<String> readingTypeMRIDs = readingTypes.stream().map(ert -> ert.getMRID()).collect(Collectors.toSet());
        Range<Instant> range = channelsContainer.getInterval().toOpenClosedRange();
        if (!range.lowerEndpoint().isBefore(endTime)) {
            return false;
        }
        if (range.hasUpperBound()) {
            endTime = endTime.isBefore(range.upperEndpoint()) ? endTime : range.upperEndpoint();
        }
        for (Channel channel : channelsContainer.getChannels()) {
            Instant endInstant = channel.truncateToIntervalLength(endTime);
            for (ReadingType readingType : channel.getReadingTypes()) {
                if (readingTypeMRIDs.contains(readingType.getMRID())) {
                    Instant instant = channel.getLastDateTime();
                    if (instant == null || instant.isBefore(endInstant)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getReadingTypesString(List<ReadingType> existedReadingTypes) {
        return existedReadingTypes.stream().map(ert -> ert.getMRID()).collect(Collectors.joining(";"));
    }

    private String getEndDevicesString(List<EndDevice> existedEndDevices) {
        return existedEndDevices.stream().map(eed -> eed.getMRID()).collect(Collectors.joining(";"));
    }

    private ServiceCallType getServiceCallType(String handlerName, String version) {
        return serviceCallService.findServiceCallType(handlerName, version)
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(handlerName, version)));
    }
}
