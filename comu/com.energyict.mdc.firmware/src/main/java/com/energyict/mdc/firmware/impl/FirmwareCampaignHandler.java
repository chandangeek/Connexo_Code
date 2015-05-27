package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import org.osgi.service.event.EventConstants;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class FirmwareCampaignHandler implements MessageHandler {

    private final JsonService jsonService;
    private final HandlerContext handlerContext;

    public FirmwareCampaignHandler(JsonService jsonService, FirmwareServiceImpl firmwareService, MeteringGroupsService meteringGroupsService) {
        this.jsonService = jsonService;
        this.handlerContext = new HandlerContext(firmwareService, meteringGroupsService);
    }

    @Override
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        Optional<Handler> handler = Handler.getHandlerForTopic(topic);
        if (handler.isPresent()){
            handler.get().handle(messageProperties, this.handlerContext);
        }
    }

    private static class HandlerContext {
        private final FirmwareServiceImpl firmwareService;
        private final MeteringGroupsService meteringGroupsService;

        public HandlerContext(FirmwareServiceImpl firmwareService, MeteringGroupsService meteringGroupsService) {
            this.firmwareService = firmwareService;
            this.meteringGroupsService = meteringGroupsService;
        }

        public FirmwareServiceImpl getFirmwareService() {
            return firmwareService;
        }

        public MeteringGroupsService getMeteringGroupsService() {
            return meteringGroupsService;
        }
    }

    private enum Handler {
        FIRMWARE_CAMPAIGN_CREATED(EventType.FIRMWARE_CAMPAIGN_CREATED.topic()){
            @Override
            public void handle(Map<String, Object> properties, HandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                long deviceGroupId = ((Number) properties.get("deviceGroupId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaign.isPresent()) {
                    Optional<EndDeviceGroup> deviceGroup = context.getMeteringGroupsService().findEndDeviceGroup(deviceGroupId);
                    if (deviceGroup.isPresent()){
                        ((FirmwareCampaignImpl) firmwareCampaign.get()).cloneDeviceList(deviceGroup.get());
                    } else {
                        firmwareCampaign.get().cancel();
                    }
                }
            }
        },

        FIRMWARE_CAMPAIGN_PROCESSED(EventType.FIRMWARE_CAMPAIGN_PROCESSED.topic()){
            @Override
            public void handle(Map<String, Object> properties, HandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaign.isPresent()) {
                    FirmwareCampaignImpl firmwareCampaignImpl = (FirmwareCampaignImpl) firmwareCampaign.get();
                    firmwareCampaignImpl.start();
                    firmwareCampaignImpl.updateStatus();
                }
            }
        },

        DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED.topic()){
            @Override
            public void handle(Map<String, Object> properties, HandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaign.isPresent()) {
                    FirmwareCampaignImpl firmwareCampaignImpl = (FirmwareCampaignImpl) firmwareCampaign.get();
                    Instant eventTimestamp = Instant.ofEpochMilli(((Number) properties.get(EventConstants.TIMESTAMP)).longValue());
                    if (!firmwareCampaignImpl.getModTime().isAfter(eventTimestamp)){
                        firmwareCampaignImpl.updateStatus();
                    } else {
                        System.out.println(" == HANDLER DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED event was before modification");
                    }
                }
            }
        },


        ;

        private String topic;

        Handler(String topic) {
            this.topic = topic;
        }

        public abstract void handle(Map<String, Object> properties, HandlerContext context);

        public static Optional<Handler> getHandlerForTopic(String topic){
            return Arrays.stream(Handler.values())
                    .filter(candidate -> candidate.topic.equals(topic))
                    .findFirst();
        }
    }
}
