package com.energyict.mdc.engine;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public static final String DATA_LOGGER_DEVICE_TYPE_NAME = "DataLoggerType";
    public static final String DATA_LOGGER_DEVICE_CONFIGURATION_NAME = "DataLoggerConfig";
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
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
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
    public DeviceBuilderForTesting deviceTypeName(String deviceTypeName) {
        return state.deviceTypeName(deviceTypeName);
    }

    @Override
    public DeviceBuilderForTesting deviceConfigName(String deviceConfigName) {
        return state.deviceConfigName(deviceConfigName);
    }

    @Override
    public DeviceBuilderForTesting dataLoggerEnabled(boolean enabled) {
        return state.dataLoggerEnabled(enabled);
    }

    @Override
    public DeviceBuilderForTesting dataLoggerSlaveDevice() {
        return state.dataLoggerSlaveDevice();
    }

    @Override
    public DeviceBuilderForTesting registerType(RegisterType registerType) {
        return state.registerType(registerType);
    }

    @Override
    public Device create(Instant when) {
        this.device = state.create(when);
        this.state = COMPLETE;
        return device;
    }

    private class UnderConstruction implements DeviceBuilderForTesting {

        protected List<LoadProfileType> loadProfileTypes = new ArrayList<>();
        private String name;
        private String mRDI;
        private RegisterType registerType;
        private List<LogBookType> logBookTypes = new ArrayList<>();
        private String deviceTypeName = DEVICE_TYPE_NAME;
        private String deviceConfigName = DEVICE_CONFIGURATION_NAME;
        private DeviceType deviceType;
        private DeviceConfiguration deviceConfiguration;
        private boolean dataLoggerEnabled;
        private DeviceTypePurpose deviceTypePurpose = DeviceTypePurpose.REGULAR;

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
        public DeviceBuilderForTesting registerType(RegisterType registerType) {
            this.registerType = registerType;
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
        public DeviceBuilderForTesting deviceTypeName(String deviceTypeName) {
            this.deviceTypeName = deviceTypeName;
            return DeviceCreator.this;
        }

        @Override
        public DeviceBuilderForTesting deviceConfigName(String deviceConfigName) {
            this.deviceConfigName = deviceConfigName;
            return DeviceCreator.this;
        }

        @Override
        public DeviceBuilderForTesting dataLoggerEnabled(boolean enabled) {
            this.dataLoggerEnabled = enabled;
            return DeviceCreator.this;
        }

        @Override
        public DeviceBuilderForTesting dataLoggerSlaveDevice() {
            this.deviceTypePurpose = DeviceTypePurpose.DATALOGGER_SLAVE;
            return DeviceCreator.this;
        }

        @Override
        public Device create(Instant when) {
            getDeviceType();
            DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
            return deviceService.newDevice(deviceConfiguration, name, mRDI, when);
        }

        private DeviceConfiguration getDeviceConfiguration() {
            if (this.deviceConfiguration == null) {
                this.deviceConfiguration = configBuilder().add();
                this.deviceConfiguration.activate();
            }
            return deviceConfiguration;
        }

        private DeviceType getDeviceType() {
            if (this.deviceType == null) {
                Optional<DeviceType> type = deviceConfigurationService.findDeviceTypeByName(deviceTypeName);
                if (type.isPresent()) {
                    this.deviceType = type.get();
                } else {
                    this.deviceType = deviceConfigurationService.newDeviceType(deviceTypeName, deviceProtocolPluggableClass);
                    if (this.deviceTypePurpose == DeviceTypePurpose.DATALOGGER_SLAVE) {
                        deviceType.setDeviceTypePurpose(this.deviceTypePurpose);
                    }
                    for (LoadProfileType loadProfileType : loadProfileTypes) {
                        this.deviceType.addLoadProfileType(loadProfileType);
                    }
                    for (LogBookType logBookType : logBookTypes) {
                        this.deviceType.addLogBookType(logBookType);
                    }
                    if (this.registerType != null) {
                        this.deviceType.addRegisterType(this.registerType);
                    }
                    this.deviceType.update();
                }
            }
            return deviceType;
        }

        protected DeviceType.DeviceConfigurationBuilder configBuilder() {
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = getDeviceType().newConfiguration(deviceConfigName);
            for (LoadProfileType loadProfileType : loadProfileTypes) {
                LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
                for (ChannelType channelType : loadProfileType.getChannelTypes()) {
                    ChannelSpec.ChannelSpecBuilder channelSpecBuilder = deviceConfigurationBuilder.newChannelSpec(channelType, loadProfileSpecBuilder);
                    channelSpecBuilder.overflow(BigDecimal.valueOf(CHANNEL_OVERFLOW_VALUE));
                }
            }
            if (this.registerType != null) {
                deviceConfigurationBuilder.newNumericalRegisterSpec(registerType).numberOfFractionDigits(0).overflowValue(BigDecimal.valueOf(999999999999999L));
            }
            for (LogBookType logBookType : logBookTypes) {
                deviceConfigurationBuilder.newLogBookSpec(logBookType);
            }
            if (this.dataLoggerEnabled) {
                deviceConfigurationBuilder.dataloggerEnabled(true);
            }
            return deviceConfigurationBuilder;
        }
    }

}
