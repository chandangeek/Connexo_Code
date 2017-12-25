/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceevents.Status;
import ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage;
import com.energyict.obis.ObisCode;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EndDeviceEventsBuilder {
    private static final String ALARM_CLOSURE_COMMENT = "Alarm closed on %s call";

    private final MeteringService meteringService;
    private final LogBookService logBookService;
    private final DeviceService deviceService;
    private final EndPointConfigurationService endPointConfigurationService;

    private final DeviceAlarmService deviceAlarmService;
    private final IssueService issueService;
    private final ThreadPrincipalService threadPrincipalService;

    private final EndDeviceEventsFaultMessageFactory faultMessageFactory;
    private final Thesaurus thesaurus;

    @Inject
    public EndDeviceEventsBuilder(MeteringService meteringService,
                                  LogBookService logBookService,
                                  DeviceService deviceService,
                                  EndPointConfigurationService endPointConfigurationService,
                                  DeviceAlarmService deviceAlarmService,
                                  IssueService issueService,
                                  ThreadPrincipalService threadPrincipalService,
                                  EndDeviceEventsFaultMessageFactory faultMessageFactory,
                                  Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.logBookService = logBookService;
        this.deviceService = deviceService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.threadPrincipalService = threadPrincipalService;
        this.faultMessageFactory = faultMessageFactory;
        this.thesaurus = thesaurus;
    }

    PreparedEndDeviceEventBuilder prepareCreateFrom(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        // check severity
        String severity = extractSeverityOrThrowException(endDeviceEvent);

        // extract end device identifiers - Asset tag is used
        Optional<String> endDeviceMrid = extractDeviceMrid(endDeviceEvent);
        Optional<String> endDeviceName = extractDeviceName(endDeviceEvent);

        if (!endDeviceMrid.isPresent() && !endDeviceName.isPresent()) {
            faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, "EndDeviceEvents.EndDeviceEvent");
        }

        Optional<String> mrid = extractMrid(endDeviceEvent);
        Instant createdDate = extractCreatedDateOrThrowException(endDeviceEvent);
        String eventTypeCode = extractEndDeviceFunctionRefOrThrowException(endDeviceEvent);

        Optional<Status> status = extractStatus(endDeviceEvent);
        Optional<String> reason = extractReason(endDeviceEvent);
        Optional<String> issuerID = extractIssuerId(endDeviceEvent);
        Optional<String> issuerTrackingID = extractIssuerTrackingId(endDeviceEvent);
        Optional<Map<String, String>> eventData = extractProperties(endDeviceEvent);

        return () -> {
            EndDevice endDevice = endDeviceMrid.isPresent() ?
                    meteringService.findEndDeviceByMRID(endDeviceMrid.get())
                            .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_METER_WITH_MRID, endDeviceMrid.get())) :
                    meteringService.findEndDeviceByName(endDeviceName.get())
                            .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_METER_WITH_NAME, endDeviceName.get()));

            EndDeviceEventType eventType = meteringService.getEndDeviceEventType(eventTypeCode)
                    .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_DEVICE_EVENT_TYPE_WITH_REF, eventTypeCode));

            EndDeviceEventRecordBuilder builder = endDevice.addEventRecord(eventType, createdDate);

            builder.setLogBookId(getLogBook(endDevice).getId());
            mrid.ifPresent(builder::setmRID);
            builder.setSeverity(severity);

            reason.ifPresent(builder::setReason);
            issuerID.ifPresent(builder::setIssuerID);
            issuerTrackingID.ifPresent(builder::setIssuerTrackingID);
            eventData.ifPresent(value -> value.entrySet().stream().forEach(property -> builder.addProperty(property.getKey(), property.getValue())));
            status.ifPresent(value -> builder.setStatus(buildStatus(value)));

            return builder.create();
        };
    }

    PreparedEndDeviceEventBuilder prepareCloseFrom(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        // check severity
        String severity = extractSeverityOrThrowException(endDeviceEvent);

        // extract end device identifiers - Asset tag is used
        Optional<String> endDeviceMrid = extractDeviceMrid(endDeviceEvent);
        Optional<String> endDeviceName = extractDeviceName(endDeviceEvent);

        if (!endDeviceMrid.isPresent() && !endDeviceName.isPresent()) {
            faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, "EndDeviceEvents.EndDeviceEvent");
        }

        String eventTypeCode = extractEndDeviceFunctionRefOrThrowException(endDeviceEvent);

        return () -> {
            EndDevice endDevice = endDeviceMrid.isPresent() ?
                    meteringService.findEndDeviceByMRID(endDeviceMrid.get())
                            .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_METER_WITH_MRID, endDeviceMrid.get())) :
                    meteringService.findEndDeviceByName(endDeviceName.get())
                            .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_METER_WITH_NAME, endDeviceName.get()));

            LogBook logBook = getLogBook(endDevice);
            IssueStatus issueStatus = issueService.findStatus(IssueStatus.RESOLVED).get();
            User user = (User)threadPrincipalService.getPrincipal();
            List<HistoricalDeviceAlarm> closedAlarms = deviceAlarmService.findOpenAlarmByDeviceIdAndEventTypeAndLogBookId(endDevice.getId(), eventTypeCode, logBook.getId()).find()
                    .stream().map(alarm -> {
                        alarm.addComment(String.format(ALARM_CLOSURE_COMMENT, user.getName()), user);
                        return alarm.close(issueStatus);
                    }).collect(Collectors.toList());

            return null;
        };
    }

    @FunctionalInterface
    interface PreparedEndDeviceEventBuilder {

        @TransactionRequired
        com.elster.jupiter.metering.readings.EndDeviceEvent build() throws FaultMessage;
    }

    private Optional<Asset> extractAsset(EndDeviceEvent endDeviceEvent) {
        return Optional.ofNullable(endDeviceEvent.getAssets());
    }

    private Optional<String> extractDeviceMrid(EndDeviceEvent endDeviceEvent) {
        return extractAsset(endDeviceEvent)
                .map(Asset::getMRID)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractDeviceName(EndDeviceEvent endDeviceEvent) {
        return extractAsset(endDeviceEvent)
                .map(Asset::getNames)
                .map(names -> names.stream().map(Name::getName))
                .flatMap(Stream::findFirst);
    }

    private Optional<String> extractMrid(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private Instant extractCreatedDateOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getCreatedDateTime())
                .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,
                        "EndDeviceEvents.EndDeviceEvent.createdDateTime"));
    }

    private String extractSeverityOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getSeverity())
                .filter(severity -> !Checks.is(severity).emptyOrOnlyWhiteSpace() && severity.equalsIgnoreCase("alarm"))
                .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_SEVERITY));
    }

    private Optional<Status> extractStatus(EndDeviceEvent endDeviceEvent) {
        return Optional.ofNullable(endDeviceEvent.getStatus());
    }

    private com.elster.jupiter.cbo.Status buildStatus(Status status) {
        return com.elster.jupiter.cbo.Status.builder()
                .at(status.getDateTime())
                .reason(status.getReason())
                .remark(status.getRemark())
                .value(status.getValue())
                .build();
    }

    private Optional<String> extractReason(EndDeviceEvent endDeviceEvent) {
        return Optional.ofNullable(endDeviceEvent.getReason())
                .filter(reason -> !Checks.is(reason).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractIssuerId(EndDeviceEvent endDeviceEvent) {
        return Optional.ofNullable(endDeviceEvent.getIssuerID())
                .filter(issuerId -> !Checks.is(issuerId).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractIssuerTrackingId(EndDeviceEvent endDeviceEvent) {
        return Optional.ofNullable(endDeviceEvent.getIssuerTrackingID())
                .filter(issuerTrackingId -> !Checks.is(issuerTrackingId).emptyOrOnlyWhiteSpace());
    }

    private Optional<Map<String, String>> extractProperties(EndDeviceEvent endDeviceEvent) {
        return Optional.ofNullable(endDeviceEvent.getEndDeviceEventDetails())
                .map(list -> list.stream().filter(details -> !Checks.is(details.getName()).emptyOrOnlyWhiteSpace())
                        .collect(Collectors.toMap(EndDeviceEventDetail::getName, EndDeviceEventDetail::getValue)));
    }

    private String extractEndDeviceFunctionRefOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getEndDeviceEventType())
                .map(EndDeviceEvent.EndDeviceEventType::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,
                        "EndDeviceEvents.EndDeviceEvent[0].EndDeviceEventType.ref"));
    }

    private LogBook getLogBook(EndDevice endDevice) throws FaultMessage {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find().stream()
                .filter(ws -> ws.getWebServiceName().equalsIgnoreCase("CIM EndDeviceEvents"))
                .findFirst()
                .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS));

        List<EndPointProperty> properties = endPointConfiguration.getProperties();
        EndPointProperty property = properties.stream().filter(prop -> prop.getName().equalsIgnoreCase("EndDeviceEvents.ObisCode"))
                .findAny().orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_PROPERTIES_CONFIGURED));

        ObisCode logBookObisCode = ObisCode.fromString(property.getValue().toString());

        Device device = findDeviceForEndDevice(endDevice);

        return logBookService.findByDeviceAndObisCode(device, logBookObisCode)
                .orElseThrow(faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS,
                        MessageSeeds.NO_LOGBOOK_WITH_OBIS_CODE_AND_DEVICE, logBookObisCode, endDevice.getId()));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        long deviceId = Long.parseLong(endDevice.getAmrId());
        return deviceService.findDeviceById(deviceId).orElseThrow(NoSuchElementException.deviceWithIdNotFound(thesaurus, deviceId));
    }

}
