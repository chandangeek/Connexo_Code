package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.FindMultipleDevices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s Call Home ID to uniquely identify it.
 *
 * @author sva
 * @since 26/10/12 (11:26)
 */
public class DialHomeIdDeviceIdentifier implements DeviceIdentifier, FindMultipleDevices<BaseDevice<BaseChannel,BaseLoadProfile<BaseChannel>,BaseRegister>> {

    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private final String callHomeID;
    private BaseDevice<?,?,?> device;
    private List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> allDevices;

    public DialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> findDevice() {
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with callHomeID " + this.callHomeID + " not found");
            } else {
                if (this.allDevices.size() > 1) {
                    throw new DuplicateException(MessageSeeds.DUPLICATE_FOUND, BaseDevice.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> allDevices = new ArrayList<>();
        List<DeviceFactory> deviceFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class);
        for (DeviceFactory deviceFactory : deviceFactories) {
            allDevices.addAll(deviceFactory.findDevicesByNotInheritedProtocolProperty(new CallHomeIdPropertySpec(), this.getIdentifier()));
        }
        this.allDevices = allDevices;
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeID;
    }

    @Override
    public String getIdentifier() {
        return callHomeID;
    }

    @Override
    public List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> getAllDevices() {
        if(this.allDevices == null){
            return Collections.emptyList();
        }
        return this.allDevices;
    }

    private class CallHomeIdPropertySpec implements PropertySpec<String> {
        @Override
        public String getName() {
            return CALL_HOME_ID_PROPERTY_NAME;
        }

        @Override
        public ValueFactory<String> getValueFactory() {
            return new StringFactory();
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isReference() {
            return false;
        }

        @Override
        public boolean validateValue(String value) throws InvalidValueException {
            return true;
        }

        @Override
        public boolean validateValueIgnoreRequired(String value) throws InvalidValueException {
            return true;
        }

        @Override
        public PropertySpecPossibleValues<String> getPossibleValues() {
            return null;
        }
    }

}