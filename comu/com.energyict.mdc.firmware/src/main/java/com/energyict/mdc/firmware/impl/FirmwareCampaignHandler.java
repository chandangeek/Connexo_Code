package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareCampaign;
import org.osgi.service.event.EventConstants;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class FirmwareCampaignHandler implements MessageHandler {

    private final JsonService jsonService;
    private final FirmwareCampaignHandlerContext handlerContext;

    public FirmwareCampaignHandler(JsonService jsonService, FirmwareCampaignHandlerContext context) {
        this.jsonService = jsonService;
        this.handlerContext = context;
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

    enum Handler {
        FIRMWARE_CAMPAIGN_CREATED(EventType.FIRMWARE_CAMPAIGN_CREATED.topic()){
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                long deviceGroupId = ((Number) properties.get("deviceGroupId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaign.isPresent()) {
                    Optional<EndDeviceGroup> deviceGroupRef = context.getMeteringGroupsService().findEndDeviceGroup(deviceGroupId);
                    if (deviceGroupRef.isPresent()){
                        List<Device> devices = Collections.emptyList();
                        EndDeviceGroup deviceGroup = deviceGroupRef.get();
                        if (deviceGroup instanceof QueryEndDeviceGroup){
                            Condition deviceQuery = ((QueryEndDeviceGroup) deviceGroup).getCondition();
                            deviceQuery = deviceQuery.and(where("deviceConfiguration.deviceType").isEqualTo(firmwareCampaign.get().getDeviceType()));
                            devices = context.getDeviceService().findAllDevices(deviceQuery).find();
                        } else {
                            devices = deviceGroup.getMembers(context.getClock().instant())
                                    .stream()
                                    .map(endDevice -> context.getDeviceService().findDeviceById(Long.parseLong(endDevice.getAmrId())))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .filter(device -> device.getDeviceConfiguration().getDeviceType().getId() == firmwareCampaign.get().getDeviceType().getId())
                                    .collect(Collectors.toList());
                        }
                        if (devices.isEmpty()){
                            firmwareCampaign.get().cancel();
                        } else {
                            for (Device device : devices) {
                                // Just a managed bean wrapper, no actual creation
                                DeviceInFirmwareCampaignImpl wrapper = context.getFirmwareService().getDataModel().getInstance(DeviceInFirmwareCampaignImpl.class);
                                wrapper.init(firmwareCampaign.get(), device);
                                context.getEventService().postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED.topic(), wrapper);
                            }
                        }
                    } else {
                        firmwareCampaign.get().cancel();
                    }
                }
            }
        },

        DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED.topic()){
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("firmwareCampaignId")).longValue();
                long deviceId = ((Number) properties.get("deviceId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaignRef = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                Optional<Device> deviceRef = context.getDeviceService().findDeviceById(deviceId);
                if (firmwareCampaignRef.isPresent() && deviceRef.isPresent()) {
                    DeviceInFirmwareCampaignImpl deviceInFirmwareCampaign = context.getFirmwareService().getDataModel().getInstance(DeviceInFirmwareCampaignImpl.class);
                    deviceInFirmwareCampaign.init(firmwareCampaignRef.get(), deviceRef.get());
                    context.getFirmwareService().getDataModel().persist(deviceInFirmwareCampaign);
                    deviceInFirmwareCampaign.startFirmwareProcess();
                }
            }
        },

        DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED.topic()){
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaign.isPresent()) {
                    FirmwareCampaignImpl firmwareCampaignImpl = (FirmwareCampaignImpl) firmwareCampaign.get();
                    Instant eventTimestamp = Instant.ofEpochMilli(((Number) properties.get(EventConstants.TIMESTAMP)).longValue());
                    if (!firmwareCampaignImpl.getModTime().isAfter(eventTimestamp)){
                        firmwareCampaignImpl.updateStatistic();
                    }
                }
            }
        },
        ;

        private String topic;

        Handler(String topic) {
            this.topic = topic;
        }

        public abstract void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context);

        public static Optional<Handler> getHandlerForTopic(String topic){
            return Arrays.stream(Handler.values())
                    .filter(candidate -> candidate.topic.equals(topic))
                    .findFirst();
        }
    }
}
