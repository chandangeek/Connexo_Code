package com.energyict.mdc.engine;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides functionality to create an serve a Device for testing purposes.
 * <p>
 * Copyrights EnergyICT
 * Date: 22/05/14
 * Time: 14:49
 */
public final class DeviceCreator implements DeviceBuilderForTesting {

    public static final int CHANNEL_OVERFLOW_VALUE = 999999;
    static final String DEVICE_TYPE_NAME = DeviceCreator.class.getName() + "Type";

    static final String DEVICE_CONFIGURATION_NAME = DeviceCreator.class.getName() + "Config";

    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    private final DeviceBuilderForTesting COMPLETE = (DeviceBuilderForTesting) Proxy.newProxyInstance(DeviceBuilderForTesting.class.getClassLoader(), new Class<?>[]{DeviceBuilderForTesting.class}, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            throw new IllegalStateException("The builder is finished, you cannot change anything anymore ...");
        }
    });

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private final DeviceProtocol deviceProtocol;
    private DeviceBuilderForTesting state;
    private Device device;

    public DeviceCreator(DeviceConfigurationService deviceConfigurationService, DeviceService deviceService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.deviceProtocol = mock(DeviceProtocol.class);
        this.deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        initializeMocks();
        state = new UnderConstruction();
    }

    private void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
    }

    @Override
    public DeviceBuilderForTesting name(String name) {
        return state.name(name);
    }

    @Override
    public DeviceBuilderForTesting mRDI(String mRDI) {
        return state.mRDI(mRDI);
    }

    @Override
    public DeviceBuilderForTesting loadProfileTypes(LoadProfileType... loadProfilesTypes) {
        return state.loadProfileTypes(loadProfilesTypes);
    }

    @Override
    public DeviceBuilderForTesting logBookTypes(LogBookType... logBookTypes) {
        return state.logBookTypes(logBookTypes);
    }

    @Override
    public Device create() {
        this.device = state.create();
        this.state = COMPLETE;
        return device;
    }

    private class UnderConstruction implements DeviceBuilderForTesting {

        private String name;
        private String mRDI;
        private List<LoadProfileType> loadProfileTypes = new ArrayList<>();
        private List<LogBookType> logBookTypes = new ArrayList<>();
        private DeviceType deviceType;
        private DeviceConfiguration deviceConfiguration;

        @Override
        public DeviceBuilderForTesting name(String name) {
            this.name = name;
            return DeviceCreator.this;
        }

        @Override
        public DeviceBuilderForTesting mRDI(String mRDI) {
            this.mRDI = mRDI;
            return DeviceCreator.this;
        }

        @Override
        public DeviceBuilderForTesting loadProfileTypes(LoadProfileType... loadProfileTypes) {
            this.loadProfileTypes = Arrays.asList(loadProfileTypes);
            return DeviceCreator.this;
        }

        @Override
        public DeviceBuilderForTesting logBookTypes(LogBookType... logBookTypes) {
            this.logBookTypes = Arrays.asList(logBookTypes);
            return DeviceCreator.this;
        }

        @Override
        public Device create() {
            DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
            Device device = deviceService.newDevice(deviceConfiguration, name, mRDI);
            device.save();
            return device;
        }

        private DeviceConfiguration getDeviceConfiguration() {
            if (this.deviceConfiguration == null) {
                DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = getDeviceType().newConfiguration(DEVICE_CONFIGURATION_NAME);
                for (LoadProfileType loadProfileType : loadProfileTypes) {
                    LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
                    for (ChannelType channelType : loadProfileType.getChannelTypes()) {
                        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = deviceConfigurationBuilder.newChannelSpec(channelType, loadProfileSpecBuilder);
                        channelSpecBuilder.overflow(BigDecimal.valueOf(CHANNEL_OVERFLOW_VALUE));
                    }
                }
                for (LogBookType logBookType : logBookTypes) {
                    deviceConfigurationBuilder.newLogBookSpec(logBookType);
                }
                this.deviceConfiguration = deviceConfigurationBuilder.add();
                getDeviceType().save();
                this.deviceConfiguration.activate();
            }
            return deviceConfiguration;
        }

        private DeviceType getDeviceType() {
            if (this.deviceType == null) {
                this.deviceType = deviceConfigurationService.newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
                for (LoadProfileType loadProfileType : loadProfileTypes) {
                    this.deviceType.addLoadProfileType(loadProfileType);
                }
                for (LogBookType logBookType : logBookTypes) {
                    this.deviceType.addLogBookType(logBookType);
                }
                this.deviceType.save();
            }
            return deviceType;
        }
    }

}
