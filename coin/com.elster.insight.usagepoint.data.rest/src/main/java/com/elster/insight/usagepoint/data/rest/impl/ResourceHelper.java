package com.elster.insight.usagepoint.data.rest.impl;

import javax.inject.Inject;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(MeteringService meteringService, ExceptionFactory exceptionFactory) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    public Meter findMeterByMrIdOrThrowException(String mRID) {
            return meteringService.findMeter(mRID).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_DEVICE_FOR_MRID, mRID));
    }

    public UsagePoint findUsagePointByMrIdOrThrowException(String mrid) {
       return meteringService.findUsagePoint(mrid).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mrid));
    }
    
//    public Register findRegister(Meter meter, long registerSpecId) {
//        
//        
////        return device.getRegisters()
////                .stream()
////                .filter(r -> r.getRegisterSpec().getId() == registerSpecId)
////                .findFirst()
////                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_REGISTER, registerSpecId));
//    }
//    
//
////    public Device findDeviceAndLock(long id, long version) {
////        return deviceService.findAndLockDeviceByIdAndVersion(id, version).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
////    }
//
//    public Register findRegisterOrThrowException(Device device, long registerSpecId) {
//        return device.getRegisters()
//                .stream()
//                .filter(r -> r.getRegisterSpec().getId() == registerSpecId)
//                .findFirst()
//                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_REGISTER, registerSpecId));
//    }
//
////    public LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId) {
////        return device.getLoadProfiles()
////                .stream()
////                .filter(lp -> lp.getId() == loadProfileId)
////                .findFirst()
////                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, device.getmRID(), loadProfileId));
////    }
//
//    public Channel findChannelOnDeviceOrThrowException(String mRID, long channelId){
//        Device device = this.findDeviceByMrIdOrThrowException(mRID);
//        return this.findChannelOnDeviceOrThrowException(device, channelId);
//    }
//
//    public Channel findChannelOnDeviceOrThrowException(Device device, long channelId){
//        return device.getLoadProfiles().stream()
//                .flatMap(lp -> lp.getChannels().stream())
//                .filter(c -> c.getId() == channelId)
//                .findFirst()
//                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_DEVICE, device.getmRID(), channelId));
//    }
//    
//    public List<Channel> findChannelsForMeter(String mRID) {
//        List<Channel> regularChannels = new ArrayList<Channel>();
//                
//        
//        Optional<? extends MeterActivation> currentActivation = meter.getCurrentMeterActivation();
//        if (currentActivation.isPresent()) {
//            List<Channel> channelCandidates = currentActivation.get().getChannels();
//            for (Channel channel : channelCandidates) {
//                if (channel.isRegular())
//                    regularChannels.add(channel);
//            }
//            
//        } else {
//            
//            //TODO: no activation, throw exception?
//            return null;
//        }
//        
//        return regularChannels;
//    }
//
////    public Condition getQueryConditionForDevice(StandardParametersBean params) {
////        Condition condition = Condition.TRUE;
////        if (params.getQueryParameters().size() > 0) {
////            condition = condition.and(addDeviceQueryCondition(params));
////        }
////        return condition;
////    }
//
////    private Condition addDeviceQueryCondition(StandardParametersBean params) {
////        Condition conditionDevice = Condition.TRUE;
////        String mRID = params.getFirst("mRID");
////        if (mRID != null) {
////            conditionDevice = conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
////        }
////        String serialNumber = params.getFirst("serialNumber");
////        if (serialNumber != null) {
////            conditionDevice = conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
////        }
////        String deviceType = params.getFirst("deviceTypeName");
////        if (deviceType != null) {
////            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType, "deviceConfiguration.deviceType.name"));
////        }
////        String deviceConfiguration = params.getFirst("deviceConfigurationName");
////        if (deviceConfiguration != null) {
////            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration, "deviceConfiguration.name"));
////        }
////        return conditionDevice;
////    }
//
//    private Condition createMultipleConditions(String params, String conditionField) {
//        Condition condition = Condition.FALSE;
//        String[] values = params.split(",");
//        for (String value : values) {
//            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
//        }
//        return condition;
//    }
//
//
//    public Condition getQueryConditionForDevice(MultivaluedMap<String, String> uriParams) {
//        Condition condition = Condition.TRUE;
//        if (uriParams.containsKey("filter")) {
//            condition = condition.and(addDeviceQueryCondition(uriParams));
//        }
//        return condition;
//    }
//
//    private Condition addDeviceQueryCondition(MultivaluedMap<String, String> uriParams) {
//        Condition conditionDevice = Condition.TRUE;
//        JsonQueryFilter filter = new JsonQueryFilter(uriParams.getFirst("filter"));
//        String mRID = filter.getString("mRID");
//        if (mRID != null) {
//            conditionDevice = conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
//        }
//        String serialNumber = filter.getString("serialNumber");
//        if (serialNumber != null) {
//            conditionDevice = conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
//        }
//        if (filter.hasProperty("deviceTypes")) {
//            List<Integer> deviceTypes = filter.getIntegerList("deviceTypes");
//            if (!deviceTypes.isEmpty()) {
//                conditionDevice = conditionDevice.and(createMultipleConditions(deviceTypes, "deviceConfiguration.deviceType.id"));
//            }
//        }
//        if (filter.hasProperty("deviceConfigurations")) {
//            List<Integer> deviceConfigurations = filter.getIntegerList("deviceConfigurations");
//            if (!deviceConfigurations.isEmpty()) {
//                conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfigurations, "deviceConfiguration.id"));
//            }
//        }
//        return conditionDevice;
//    }
//
//    private Condition createMultipleConditions(List<Integer> params, String conditionField) {
//        Condition condition = Condition.FALSE;
//        for (int value : params) {
//            condition = condition.or(where(conditionField).isEqualTo(value));
//        }
//        return condition;
//    }

}
