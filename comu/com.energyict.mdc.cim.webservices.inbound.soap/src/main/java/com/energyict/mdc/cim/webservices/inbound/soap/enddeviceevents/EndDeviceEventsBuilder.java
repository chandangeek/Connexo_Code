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
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
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

import static com.elster.jupiter.util.conditions.Where.where;

public class EndDeviceEventsBuilder {
    private static final String ALARM_SEVERITY = "alarm";

    private static final String DEVICE_PROTOCOL_CODE_LABEL = "DeviceProtocolCode";
    private static final String END_DEVICE_EVENTS_ITEM = "EndDeviceEvents";
    private static final String END_DEVICE_EVENT_ITEM = END_DEVICE_EVENTS_ITEM + ".EndDeviceEvent";
    private static final String END_DEVICE_EVENT_ASSETS_ITEM = END_DEVICE_EVENT_ITEM + ".Assets";
    private static final String END_DEVICE_EVENT_CREATED_DATE_ITEM = END_DEVICE_EVENT_ITEM + ".createdDateTime";
    private static final String END_DEVICE_EVENT_TYPE_ITEM = END_DEVICE_EVENT_ITEM + ".EndDeviceEventType.ref";

    private static final String OBIS_CODE_PROPERTY = "endDeviceEvents.obisCode";

    private static final String WEBSERVICE_NAME = "CIM EndDeviceEvents";
    private static final String NULL_CIM_CODE = "0.0.0.0";

    private final DeviceAlarmService deviceAlarmService;
    private final DeviceService deviceService;
    private final EndPointConfigurationService endPointConfigurationService;
    private final IssueService issueService;
    private final LogBookService logBookService;
    private final MeteringService meteringService;
    private final ThreadPrincipalService threadPrincipalService;

    private final EndDeviceEventsFactory endDeviceEventsFactory;
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
                                  EndDeviceEventsFactory endDeviceEventsFactory,
                                  EndDeviceEventsFaultMessageFactory faultMessageFactory,
                                  Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.logBookService = logBookService;
        this.deviceService = deviceService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.threadPrincipalService = threadPrincipalService;
        this.endDeviceEventsFactory = endDeviceEventsFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.thesaurus = thesaurus;
    }

    PreparedEndDeviceEventBuilder prepareCreateFrom(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        // check severity
        String severity = extractSeverityOrThrowException(endDeviceEvent);

        // extract end device identifiers - Asset tag is used
        Asset asset = extractAssetOrThrowException(endDeviceEvent);
        Optional<String> endDeviceMrid = extractDeviceMrid(asset);
        Optional<String> endDeviceName = extractDeviceName(asset);

        if (!endDeviceMrid.isPresent() && !endDeviceName.isPresent()) {
            throw faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_EVENT_ITEM).get();
        }

        Optional<String> mrid = extractMrid(endDeviceEvent);
        Instant createdDate = extractCreatedDateOrThrowException(endDeviceEvent);
        String eventTypeCode = extractEndDeviceFunctionRefOrThrowException(endDeviceEvent);
        Optional<String> name = extractName(endDeviceEvent);
        Optional<Status> status = extractStatus(endDeviceEvent);
        Optional<String> reason = extractReason(endDeviceEvent);
        Optional<String> issuerID = extractIssuerId(endDeviceEvent);
        Optional<String> issuerTrackingID = extractIssuerTrackingId(endDeviceEvent);
        Optional<Map<String, String>> eventData = extractProperties(endDeviceEvent);

        return () -> {
            EndDevice endDevice = getEndDevice(endDeviceMrid, endDeviceName);
            EndDeviceEventType eventType = meteringService.getEndDeviceEventType(eventTypeCode)
                    .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_DEVICE_EVENT_TYPE_WITH_REF, eventTypeCode));

            EndDeviceEventRecordBuilder builder = endDevice.addEventRecord(eventType, createdDate, getLogBook(endDevice).getId());

            mrid.ifPresent(builder::setmRID);
            builder.setSeverity(severity);
            name.ifPresent(builder::setName);

            reason.ifPresent(value -> {
                builder.setReason(value);
                builder.setDescription(value);
            });
            issuerID.ifPresent(builder::setIssuerID);
            issuerTrackingID.ifPresent(builder::setIssuerTrackingID);
            eventData.ifPresent(value -> value.entrySet().stream().forEach(property -> {
                if (!property.getKey().equals(DEVICE_PROTOCOL_CODE_LABEL)) {
                    builder.addProperty(property.getKey(), property.getValue());
                }
            }));
            status.ifPresent(value -> builder.setStatus(buildStatus(value)));
            builder.setDeviceEventType(eventData.get().get(DEVICE_PROTOCOL_CODE_LABEL));

            return endDeviceEventsFactory.asEndDeviceEvents(builder.create(), endDevice);
        };
    }

    PreparedEndDeviceEventBuilder prepareCloseFrom(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        // check severity
        String severity = extractSeverityOrThrowException(endDeviceEvent);

        // extract end device identifiers - Asset tag is used
        Asset asset = extractAssetOrThrowException(endDeviceEvent);
        Optional<String> endDeviceMrid = extractDeviceMrid(asset);
        Optional<String> endDeviceName = extractDeviceName(asset);

        if (!endDeviceMrid.isPresent() && !endDeviceName.isPresent()) {
            throw faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_EVENT_ITEM).get();
        }

        String eventTypeCode = extractEndDeviceFunctionRefOrThrowException(endDeviceEvent);

        return () -> {
            EndDevice endDevice = getEndDevice(endDeviceMrid, endDeviceName);
            IssueStatus issueStatus = issueService.findStatus(IssueStatus.RESOLVED).get();
            User user = (User) threadPrincipalService.getPrincipal();

            EndDeviceEventType eventType = meteringService.getEndDeviceEventType(eventTypeCode)
                    .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_DEVICE_EVENT_TYPE_WITH_REF, eventTypeCode));
            Optional<String> endDeviceEventType = getEndDeviceEventType(endDeviceEvent);

            Condition condition = getCondition(endDevice, eventType, endDeviceEventType);

            List<HistoricalDeviceAlarm> closedAlarms = deviceAlarmService.findOpenDeviceAlarms(condition).find()
                    .stream().map(alarm -> {
                        alarm.addComment(thesaurus.getFormat(TranslationKeys.ALARM_CLOSURE_COMMENT).format(user.getName()), user);
                        return alarm.close(issueStatus);
                    }).collect(Collectors.toList());

            return endDeviceEventsFactory.asEndDeviceEvents(closedAlarms);
        };
    }

    private Condition getCondition(EndDevice endDevice, EndDeviceEventType eventType, Optional<String> endDeviceEventType) {
        Condition condition;
        if(endDeviceEventType.isPresent() && eventType.getMRID().equals(NULL_CIM_CODE)) {
            condition = where("deviceAlarmRelatedEvents.endDeviceId").isEqualTo(endDevice.getId())
                    .and(where("deviceAlarmRelatedEvents.eventTypeCode").isEqualTo(eventType.getMRID())).and(where("deviceAlarmRelatedEvents.deviceCode").isEqualTo(endDeviceEventType.get()));
        } else {
            condition = where("deviceAlarmRelatedEvents.endDeviceId").isEqualTo(endDevice.getId())
                    .and(where("deviceAlarmRelatedEvents.eventTypeCode").isEqualTo(eventType.getMRID()));
        }
        return condition;
    }

    private Optional<String> getEndDeviceEventType(EndDeviceEvent endDeviceEvent) {
        Optional<Map<String, String>> optionalofEndDeviceEventDetailsMap = extractProperties(endDeviceEvent);
        if(optionalofEndDeviceEventDetailsMap.isPresent()) {
            Map<String, String> endDevEventDetailMap = optionalofEndDeviceEventDetailsMap.get();
            if (endDevEventDetailMap.containsKey(DEVICE_PROTOCOL_CODE_LABEL)) {
              return  Optional.of(endDevEventDetailMap.get(DEVICE_PROTOCOL_CODE_LABEL));
            }
        }
        return Optional.empty();
    }

    @FunctionalInterface
    interface PreparedEndDeviceEventBuilder {

        @TransactionRequired
        EndDeviceEvents build() throws FaultMessage;
    }

    private Asset extractAssetOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getAssets())
                .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, END_DEVICE_EVENT_ASSETS_ITEM));
    }

    private Optional<String> extractDeviceMrid(Asset asset) {
        return Optional.ofNullable(asset.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractDeviceName(Asset asset) {
        return Optional.ofNullable(asset.getNames())
                .map(names -> names.stream().filter(name -> !Checks.is(name.getName()).emptyOrOnlyWhiteSpace())
                        .map(Name::getName))
                .flatMap(Stream::findFirst);
    }

    private Optional<String> extractMrid(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractName(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getNames())
                .map(names -> names.stream().map(Name::getName))
                .flatMap(Stream::findFirst);
    }

    private Instant extractCreatedDateOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getCreatedDateTime())
                .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, END_DEVICE_EVENT_CREATED_DATE_ITEM));
    }

    private String extractSeverityOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getSeverity())
                .filter(severity -> !Checks.is(severity).emptyOrOnlyWhiteSpace() && severity.equalsIgnoreCase(ALARM_SEVERITY))
                .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_SEVERITY));
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
                .map(list -> list.stream().filter(details -> !Checks.is(details.getName()).emptyOrOnlyWhiteSpace() && details.getName().equals(DEVICE_PROTOCOL_CODE_LABEL))
                        .collect(Collectors.toMap(EndDeviceEventDetail::getName, EndDeviceEventDetail::getValue)));
    }

    private String extractEndDeviceFunctionRefOrThrowException(EndDeviceEvent endDeviceEvent) throws FaultMessage {
        return Optional.ofNullable(endDeviceEvent.getEndDeviceEventType())
                .map(EndDeviceEvent.EndDeviceEventType::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, END_DEVICE_EVENT_TYPE_ITEM));
    }

    private EndDevice getEndDevice(Optional<String> endDeviceMrid, Optional<String> endDeviceName) throws FaultMessage {
        return endDeviceMrid.isPresent() ?
                meteringService.findEndDeviceByMRID(endDeviceMrid.get())
                        .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_DEVICE_WITH_MRID, endDeviceMrid.get())) :
                meteringService.findEndDeviceByName(endDeviceName.get())
                        .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_DEVICE_WITH_NAME, endDeviceName.get()));
    }

    private LogBook getLogBook(EndDevice endDevice) throws FaultMessage {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find().stream()
                .filter(ws -> ws.getWebServiceName().equalsIgnoreCase(WEBSERVICE_NAME))
                .findFirst()
                .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_POINT_WITH_WEBSERVICE_NAME, WEBSERVICE_NAME));

        List<EndPointProperty> properties = endPointConfiguration.getProperties();
        EndPointProperty property = properties.stream().filter(prop -> prop.getName().equalsIgnoreCase(OBIS_CODE_PROPERTY))
                .findAny().orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_OBIS_CODE_CONFIGURED));

        ObisCode logBookObisCode = ObisCode.fromString(property.getValue().toString());

        Device device = findDeviceForEndDevice(endDevice);

        return logBookService.findByDeviceAndObisCode(device, logBookObisCode)
                .orElseThrow(faultMessageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.NO_LOGBOOK_WITH_OBIS_CODE_AND_DEVICE,
                        logBookObisCode, endDevice.getId()));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        long deviceId = Long.parseLong(endDevice.getAmrId());
        return deviceService.findDeviceById(deviceId).orElseThrow(NoSuchElementException.deviceWithIdNotFound(thesaurus, deviceId));
    }

}
