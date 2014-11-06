package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONException;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class ResourceHelper {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final MeteringService meteringService;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory, MeteringService meteringService) {
        super();
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.meteringService = meteringService;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Device device = deviceService.findByUniqueMrid(mRID);
        if (device == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID);
        }
        return device;
    }

    public Register findRegisterOrThrowException(Device device, long registerSpecId) {
        return device.getRegisters()
                .stream()
                .filter(r -> r.getRegisterSpec().getId() == registerSpecId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_REGISTER, registerSpecId));
    }

    public LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId) {
        return device.getLoadProfiles()
                .stream()
                .filter(lp -> lp.getId() == loadProfileId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, device.getmRID(), loadProfileId));
    }

    public Channel findChannelOrThrowException(LoadProfile loadProfile, long channelSpecId) {
        return loadProfile.getChannels()
                .stream()
                .filter(c -> c.getChannelSpec().getId() == channelSpecId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_LOAD_PROFILE, loadProfile.getId(), channelSpecId));
    }

    /*public Condition getQueryConditionForDevice(StandardParametersBean params) {
        ImmutableMap<String, BiFunction<String, StandardParametersBean, Condition>> keyToConditionFunction =
            ImmutableMap.of(
                    "mRID", (String k, StandardParametersBean p) -> toSimpleCondition(p, k),
                    "serialNumber", (String k, StandardParametersBean p) -> toSimpleCondition(p, k),
                    "deviceTypeName", (String k, StandardParametersBean p) -> toMultiValuedCondition(k, "deviceConfiguration.deviceType.name"),
                    "deviceConfigurationName", (String k, StandardParametersBean p) -> toMultiValuedCondition(k, "deviceConfiguration.name"));
        return keyToConditionFunction.entrySet()
            .stream()
            .filter(e -> params.containsKey(e.getKey()))
            .map(e -> e.getValue().apply(e.getKey(), params))
            .reduce(
                Condition.TRUE,
                (c1, c2) -> c1.and(c2));
    }*/

    public Condition getQueryConditionForDevice(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if (params.getQueryParameters().size() > 0) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String mRID = params.getFirst("mRID");
        if (mRID != null) {
            conditionDevice = !params.wasRegExp()
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = params.getFirst("serialNumber");
        if (serialNumber != null) {
            conditionDevice = !params.wasRegExp()
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = params.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType, "deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = params.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration, "deviceConfiguration.name"));
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }


    private Condition toSimpleCondition(StandardParametersBean params, String key) {
        String value = params.getFirst(key);
        if (params.wasRegExp()) {
            return where(key).likeIgnoreCase(value);
        }
        else {
            return where(key).isEqualTo(value);
        }
    }

    private Condition toMultiValuedCondition(String params, String conditionField) {
        return Stream
            .of(params.split(","))
            .map(v -> where(conditionField).isEqualTo(v.trim()))
            .reduce(
                Condition.FALSE,
                (c1, c2) -> c1.or(c2));
    }

    public Meter getMeterFor(Device device) {
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(1);
        Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
        if (!meterRef.isPresent()) {
            throw new IllegalArgumentException("Validation feature on device " + device.getmRID() +
                    " wasn't configured.");
        }
        return meterRef.get();
    }

    Meter getOrCreateMeterFor(Device device) {
        Meter meter = getMeterFor(device);
        if (meter != null) {
            return meter;
        }
        return createMeter(device);
    }

    private Meter createMeter(Device device) {
        Meter meter;
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter(String.valueOf(device.getId()), device.getmRID());
        meter.save();
        return meter;
    }

    public List<MeterActivation> getMeterActivationsMostCurrentFirst(Meter meter) {
        List<MeterActivation> activations = new ArrayList<>(meter.getMeterActivations());
        Collections.reverse(activations);
        return activations;
    }

    public Optional<com.elster.jupiter.metering.Channel> getRegisterChannel(Register register, Meter meter) {
        return this.getMeterActivationsMostCurrentFirst(meter)
                .stream()
                .map(ma -> getChannel(ma, register.getRegisterSpec().getRegisterType().getReadingType()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<com.elster.jupiter.metering.Channel> getLoadProfileChannel(Channel channel, Meter meter) {
        return this.getMeterActivationsMostCurrentFirst(meter)
                .stream()
                .map(ma -> getChannel(ma, channel.getReadingType()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        return meterActivation.getChannels()
                .stream()
                .filter(c -> c.getReadingTypes().contains(readingType))
                .findFirst();
    }

    public ConnectionTask<?, ?> findConnectionTaskOrThrowException(Device device, long connectionMethodId) {
        return device.getConnectionTasks()
                .stream()
                .filter(ct -> ct.getId() == connectionMethodId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_METHOD, device.getmRID(), connectionMethodId));
    }

    public Condition getQueryConditionForDevice(MultivaluedMap<String, String> uriParams) {
        Condition condition = Condition.TRUE;
        if (uriParams.containsKey("filter")) {
            condition = condition.and(addDeviceQueryCondition(uriParams));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(MultivaluedMap<String, String> uriParams) {
        try {
            Condition conditionDevice = Condition.TRUE;
            JsonQueryFilter filter = new JsonQueryFilter(new JSONArray(uriParams.getFirst("filter")));
            String mRID = filter.getProperty("mRID");
            if (mRID != null) {
                mRID = replaceRegularExpression(mRID);
                conditionDevice = !isRegularExpression(mRID)
                        ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                        : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
            }
            String serialNumber = filter.getProperty("serialNumber");
            if (serialNumber != null) {
                serialNumber = replaceRegularExpression(serialNumber);
                conditionDevice = !isRegularExpression(serialNumber)
                        ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                        : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
            }
            JSONArray deviceTypesJSONArray =  (JSONArray)filter.getProperty("deviceTypes");
            if (deviceTypesJSONArray != null) {
                List<String> deviceTypes = getValues(deviceTypesJSONArray);
                if (!deviceTypes.isEmpty()) {
                    conditionDevice = conditionDevice.and(createMultipleConditions(deviceTypes, "deviceConfiguration.deviceType.id"));
                }
            }
            JSONArray deviceConfigurationsJSONArray =  (JSONArray)filter.getProperty("deviceConfigurations");
            if (deviceConfigurationsJSONArray != null) {
                List<String> deviceConfigurations = getValues(deviceConfigurationsJSONArray);
                if (!deviceConfigurations.isEmpty()) {
                    conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfigurations, "deviceConfiguration.id"));
                }
            }
            return conditionDevice;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private Condition createMultipleConditions(List<String> params, String conditionField) {
        Condition condition = Condition.FALSE;
        for (String value : params) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }


    private List<String> getValues(JSONArray jsonArray) throws JSONException {
        int numberOfValues = jsonArray.length();
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < numberOfValues; i++) {
            String type = jsonArray.getString(i);
            type = type.trim();
            if (!type.equals("")) {
                values.add(type);
            }
        }
        return values;
    }

    private boolean isRegularExpression(String value) {
        if (value.contains("*")) {
            return true;
        }
        if (value.contains("?")) {
            return true;
        }
        if (value.contains("%")) {
            return true;
        }
        return false;
    }

    private String replaceRegularExpression(String value) {
        if (value.contains("*")) {
            value = value.replaceAll("\\*","%");
            return value;
        }
        if (value.contains("?")) {
            value = value.replaceAll("\\?","_");
            return value;
        }
        if (value.contains("%")) {
            return value;
        }
        return value;
    }

}
