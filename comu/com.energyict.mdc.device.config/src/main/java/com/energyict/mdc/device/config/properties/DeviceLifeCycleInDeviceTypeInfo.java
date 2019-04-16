package com.energyict.mdc.device.config.properties;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DeviceLifeCycleInDeviceTypeInfo extends HasIdAndName {

    private DeviceType deviceType;
    private List<State> states;
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    static final String NAME = "BasicDataCollectionRuleTemplate";
    private static final String SEPARATOR = ":";
    private static final Logger LOG = Logger.getLogger(DeviceLifeCycleInDeviceTypeInfo.class.getName());

    public DeviceLifeCycleInDeviceTypeInfo(DeviceType deviceType, List<State> states, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceType = deviceType;
        this.states = new CopyOnWriteArrayList<>(states);
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }


    @Override
    public String getId() {
        return deviceType.getId() + SEPARATOR + deviceType.getDeviceLifeCycle().getId() + SEPARATOR + states.stream().map(HasId::getId).map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public String getName() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("deviceTypeName", deviceType.getName());
            jsonObj.put("lifeCycleStateName", states.stream().map(state -> getStateName(state) + " (" + deviceType.getDeviceLifeCycle().getName() + ")").collect(Collectors.toList()));
            return jsonObj.toString();
        } catch (JSONException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return "";
    }

    private DeviceType getDeviceType() {
        return deviceType;
    }

    public long getDeviceTypeId() { return deviceType.getId(); }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(state::getName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceLifeCycleInDeviceTypeInfo)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DeviceLifeCycleInDeviceTypeInfo that = (DeviceLifeCycleInDeviceTypeInfo) o;

        if (!deviceType.equals(that.deviceType)) {
            return false;
        }
        return states.equals(that.states);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + deviceType.hashCode();
        result = 31 * result + states.hashCode();
        return result;
    }
}