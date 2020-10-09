/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.data.push.enddeviceevents;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import com.google.inject.Inject;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@LiteralSql
public class EndDeviceEventMessageHandler implements MessageHandler {
    private final EndDeviceEventMessageHandlerFactory factory;
    private final JsonService jsonService;
    private final MeteringService meteringService;
    private final EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;

    @Inject
    public EndDeviceEventMessageHandler(EndDeviceEventMessageHandlerFactory factory,
                                        JsonService jsonService,
                                        MeteringService meteringService,
                                        EndDeviceEventsServiceProvider endDeviceEventsServiceProvider) {
        this.factory = factory;
        this.jsonService = jsonService;
        this.meteringService = meteringService;
        this.endDeviceEventsServiceProvider = endDeviceEventsServiceProvider;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> jsonPayload = jsonService.deserialize(message.getPayload(), Map.class);
        String cimEventCode = (String) jsonPayload.get("endDeviceEventType");
        String deviceEventCode = (String) jsonPayload.get("deviceEventType");
        if (factory.getCimEventCodePattern().filter(pattern -> pattern.matcher(cimEventCode).matches()).isPresent()
                || deviceEventCode != null
                && factory.getDeviceEventCodePattern().filter(pattern -> pattern.matcher(deviceEventCode).matches()).isPresent()) {
            Set<EndPointConfiguration> endPointConfigurations = factory.getEndpoints();
            if (!endPointConfigurations.isEmpty()) {
                Number endDeviceId = (Number) jsonPayload.get("endDeviceId");
                if (!factory.needToFilterByDeviceGroups() || contain(factory.getDeviceGroups(), endDeviceId)) {
                    try(QueryStream<EndDeviceEventRecord> endDeviceEventRecordQueryStream = meteringService.streamEndDeviceEvents()) {
                        endDeviceEventRecordQueryStream.join(EndDevice.class)
                                .join(EndDeviceEventType.class)
                                .filter(Where.where("endDevice.id").isEqualTo(endDeviceId))
                                .filter(Where.where("createdDateTime").isEqualTo(Instant.ofEpochMilli(((Number) jsonPayload.get("eventTimestamp")).longValue())))
                                .filter(Where.where("eventType.mRID").isEqualTo(cimEventCode))
                                .filter(Where.where("deviceEventType").isEqualOrBothNull(deviceEventCode))
                                .findAny()
                                .ifPresent(eventRecord ->
                                        endDeviceEventsServiceProvider.call(eventRecord, endPointConfigurations.toArray(new EndPointConfiguration[endPointConfigurations.size()])));
                    }
                }
            }
        }
    }

    private boolean contain(Set<EndDeviceGroup> deviceGroups, Number endDeviceId) {
        return deviceGroups.stream()
                .anyMatch(group -> contains(group, endDeviceId));
    }

    private boolean contains(EndDeviceGroup deviceGroup, Number endDeviceId) {
        SqlBuilder sqlBuilder = new SqlBuilder("select 1 from dual where ");
        sqlBuilder.addLong(endDeviceId.longValue());
        sqlBuilder.append(" in (");
        sqlBuilder.add(deviceGroup.toSubQuery("id").toFragment());
        sqlBuilder.closeBracket();
        return factory.executeQuery(sqlBuilder, ResultSet::next);
    }
}
