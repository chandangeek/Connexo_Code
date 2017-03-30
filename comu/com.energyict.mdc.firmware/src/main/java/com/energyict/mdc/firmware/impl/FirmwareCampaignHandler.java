/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;

import org.osgi.service.event.EventConstants;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class FirmwareCampaignHandler implements MessageHandler {

    private static final int ORA_UNIQUE_CONSTRAINT = 1;
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
        if (handler.isPresent()) {
            handler.get().handle(messageProperties, this.handlerContext);
        }
    }

    enum Handler {
        FIRMWARE_CAMPAIGN_CREATED(EventType.FIRMWARE_CAMPAIGN_CREATED.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                long deviceGroupId = ((Number) properties.get("deviceGroupId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaignOpt = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaignOpt.isPresent()) {
                    FirmwareCampaign firmwareCampaign = firmwareCampaignOpt.get();
                    Optional<EndDeviceGroup> deviceGroup = context.getMeteringGroupsService().findEndDeviceGroup(deviceGroupId);
                    if (deviceGroup.isPresent()) {
                        UpdatableHolder<Boolean> hasDevices = new UpdatableHolder<>(false);
                        UpdatableHolder<Integer> count = new UpdatableHolder<>(0);
                        getDevicesOfGroupAndType(deviceGroup.get(), firmwareCampaign.getDeviceType(), context)
                                .filter(filterDevicesByAllowedStates())
                                .forEach(device -> {
                                    // Just a managed bean wrapper, no actual creation
                                    List<DeviceInFirmwareCampaignImpl> devicesInFirmwareCampaign = context.getFirmwareService().getDeviceInFirmwareCampaignsFor(device);
                                    Optional<DeviceInFirmwareCampaignImpl> existingPendingOrOngoingDeviceInFWCampaign = devicesInFirmwareCampaign.stream()
                                            .filter(DeviceInFirmwareCampaignImpl::hasNonFinalStatus)
                                            .findAny();
                                    if (!existingPendingOrOngoingDeviceInFWCampaign.isPresent()) {
                                        hasDevices.update(true);
                                        count.update(count.get() + 1);
                                        DeviceInFirmwareCampaignImpl wrapper = getDeviceInFirmwareCampaignWrapper(context, firmwareCampaign, device);
                                        context.getEventService().postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED.topic(), wrapper);
                                    }
                                });
                        ((FirmwareCampaignImpl) firmwareCampaign).updateNumberOfDevices(count.get());
                        if (!hasDevices.get()) {
                            cancelCampaign(context, firmwareCampaign);
                        }
                    } else {
                        cancelCampaign(context, firmwareCampaign);
                    }
                }
            }

            private Stream<Device> getDevicesOfGroupAndType(EndDeviceGroup deviceGroup, DeviceType deviceType, FirmwareCampaignHandlerContext context) {
                if (deviceGroup instanceof QueryEndDeviceGroup) {
                    Condition deviceCondition = ListOperator.IN.contains(((QueryEndDeviceGroup) deviceGroup)::toFragment, "id");
                    Condition deviceTypeCondition = where("deviceConfiguration.deviceType").isEqualTo(deviceType);
                    return context.getDeviceService().findAllDevices(deviceCondition.and(deviceTypeCondition)).stream();
                } else {
                    return deviceGroup.getMembers(context.getClock().instant())
                            .stream()
                            .map(endDevice -> context.getDeviceService().findDeviceById(Long.parseLong(endDevice.getAmrId())))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(device -> device.getDeviceType().getId() == deviceType.getId());
                }
            }

            private void cancelCampaign(FirmwareCampaignHandlerContext context, FirmwareCampaign firmwareCampaign) {
                context.getFirmwareService().cancelFirmwareCampaign(firmwareCampaign);
            }
        },
        FIRMWARE_CAMPAIGN_UPDATED(EventType.FIRMWARE_CAMPAIGN_TIMEBOUNDARIES_UPDATED.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                context.getFirmwareService().getDevicesForFirmwareCampaign(firmwareCampaign.get()).stream()
                        .forEach(deviceInFirmwareCampaign -> {
                                    DeviceInFirmwareCampaignImpl wrapper = getDeviceInFirmwareCampaignWrapper(context, firmwareCampaign.get(), deviceInFirmwareCampaign.getDevice());
                                    context.getEventService().postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATE_TIME_BOUNDARIES.topic(), wrapper);
                                }
                        );
            }
        },
        FIRMWARE_CAMPAIGN_CANCELLED(EventType.FIRMWARE_CAMPAIGN_CANCELLED.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                context.getFirmwareService().getDevicesForFirmwareCampaign(firmwareCampaign.get()).stream()
                        .map(deviceInFirmwareCampaign1 -> ((DeviceInFirmwareCampaignImpl) deviceInFirmwareCampaign1))
                        .filter(DeviceInFirmwareCampaignImpl::hasNonFinalStatus)
                        .forEach(deviceInFirmwareCampaign -> {
                                    DeviceInFirmwareCampaignImpl wrapper = getDeviceInFirmwareCampaignWrapper(context, firmwareCampaign.get(), deviceInFirmwareCampaign.getDevice());
                                    context.getEventService().postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CANCEL.topic(), wrapper);
                                }
                        );
            }
        },

        DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("firmwareCampaignId")).longValue();
                long deviceId = ((Number) properties.get("deviceId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaignRef = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                Optional<Device> deviceRef = context.getDeviceService().findDeviceById(deviceId);
                if (firmwareCampaignRef.isPresent() && deviceRef.isPresent()) {
                    FirmwareCampaign firmwareCampaign = firmwareCampaignRef.get();
                    try {
                        DeviceInFirmwareCampaignImpl deviceInFirmwareCampaign = context.getFirmwareService().getDataModel().getInstance(DeviceInFirmwareCampaignImpl.class);
                        deviceInFirmwareCampaign.init(firmwareCampaign, deviceRef.get());
                        context.getFirmwareService().getDataModel().persist(deviceInFirmwareCampaign);
                        deviceInFirmwareCampaign.startFirmwareProcess();
                    } catch (UnderlyingSQLFailedException e) {
                        Throwable cause = e.getCause();
                        if (cause != null && cause instanceof SQLException) {
                            if (((SQLException) cause).getErrorCode() == ORA_UNIQUE_CONSTRAINT) {
                                //The device_in_campaign was already created, as part of another campaign.
                                //It cannot be created again for this campaign. Ignore and move on.
                                firmwareCampaign.decreaseCount();
                                firmwareCampaign.save();
                            } else {
                                throw e;
                            }
                        } else {
                            throw e;
                        }
                    }
                    ((FirmwareCampaignImpl) firmwareCampaign).updateStatistic();
                }
            }
        },

        DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATED.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("id")).longValue();
                Optional<FirmwareCampaign> firmwareCampaign = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                if (firmwareCampaign.isPresent()) {
                    FirmwareCampaign firmwareCampaignImpl = firmwareCampaign.get();
                    Instant eventTimestamp = Instant.ofEpochMilli(((Number) properties.get(EventConstants.TIMESTAMP)).longValue());
                    updateFirmwareStatistics(context, firmwareCampaignImpl);
                }
            }
        },

        DEVICE_IN_FIRMWARE_CAMPAIGN_CANCEL(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CANCEL.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("firmwareCampaignId")).longValue();
                long deviceId = ((Number) properties.get("deviceId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaignRef = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                Optional<Device> deviceRef = context.getDeviceService().findDeviceById(deviceId);
                if (firmwareCampaignRef.isPresent() && deviceRef.isPresent()) {
                    Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = context.getFirmwareService().getDeviceInFirmwareCampaignsForDevice(firmwareCampaignRef.get(), deviceRef.get());
                    deviceInFirmwareCampaign.ifPresent(deviceInFirmwareCampaign1 -> {
                        Device device = deviceInFirmwareCampaign1.getDevice();
                        context.getFirmwareService().cancelFirmwareUploadForDevice(device);
                        deviceInFirmwareCampaign1.cancel();
                    });
                }
            }
        },
        DEVICE_IN_FIRMWARE_CAMPAIGN_TIMEBOUNDARIES_UPDATED(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_UPDATE_TIME_BOUNDARIES.topic()) {
            @Override
            public void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context) {
                long firmwareCampaignId = ((Number) properties.get("firmwareCampaignId")).longValue();
                long deviceId = ((Number) properties.get("deviceId")).longValue();
                Optional<FirmwareCampaign> firmwareCampaignRef = context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignId);
                Optional<Device> deviceRef = context.getDeviceService().findDeviceById(deviceId);
                if (firmwareCampaignRef.isPresent() && deviceRef.isPresent()) {
                    Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = context.getFirmwareService().getDeviceInFirmwareCampaignsForDevice(firmwareCampaignRef.get(), deviceRef.get());
                    deviceInFirmwareCampaign.ifPresent(DeviceInFirmwareCampaign::updateTimeBoundaries);
                }
            }
        };

        private String topic;

        Handler(String topic) {
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

        public abstract void handle(Map<String, Object> properties, FirmwareCampaignHandlerContext context);

        public static Optional<Handler> getHandlerForTopic(String topic) {
            return Arrays.stream(Handler.values())
                    .filter(candidate -> candidate.topic.equals(topic))
                    .findFirst();
        }

        DeviceInFirmwareCampaignImpl getDeviceInFirmwareCampaignWrapper(FirmwareCampaignHandlerContext context, FirmwareCampaign firmwareCampaign, Device device) {
            DeviceInFirmwareCampaignImpl wrapper = context.getFirmwareService().getDataModel().getInstance(DeviceInFirmwareCampaignImpl.class);
            wrapper.init(firmwareCampaign, device);
            return wrapper;
        }

        void updateFirmwareStatistics(FirmwareCampaignHandlerContext context, FirmwareCampaign firmwareCampaignImpl) {
            boolean retry = true;
            while (retry) {
                try {
                    ((FirmwareCampaignImpl) firmwareCampaignImpl).updateStatistic();
                    retry = false;
                } catch (OptimisticLockException e) {
                    firmwareCampaignImpl = (context.getFirmwareService().getFirmwareCampaignById(firmwareCampaignImpl.getId()).get());
                    retry = !(firmwareCampaignImpl.getStatus().equals(FirmwareCampaignStatus.COMPLETE) ||
                            firmwareCampaignImpl.getStatus().equals(FirmwareCampaignStatus.CANCELLED));
                }
            }
        }
    }

    private static Predicate<Device> filterDevicesByAllowedStates() {
        return device -> !DefaultState.DECOMMISSIONED.getKey().equals(device.getState().getName());
    }
}
