/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.energyict;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.northerndesign.NDBaseRegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Protocol class for reading out an EIMeter flex SM352 module.
 *
 * @author alex
 */
@SuppressWarnings("unchecked")
public class EIMeterFlexSlaveModule extends Modbus {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT EIFlex Meter Modbus";
    }

    /** The name of the register that contains the firmware version. */
    private static final String FIRMWARE_VERSION_REGISTER_NAME = "FirmwareVersion";

    /** The name of the register that contains the meter model. */
    private static final String METERMODEL_REGISTER_NAME = "MeterModel";
    private final Clock clock;

    public final DiscoverResult discover(final DiscoverTools discoverTools) {
        return null;
    }

    @Inject
    public EIMeterFlexSlaveModule(PropertySpecService propertySpecService, Clock clock) {
        super(propertySpecService);
        this.clock = clock;
    }

    @Override
    protected final void doTheConnect() {
    }

    @Override
    protected final void doTheDisConnect() {
    }

    @Override
    protected final void doTheValidateProperties(final Properties properties) {
        this.setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "25").trim()));
        this.setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getProperty("FirstTimeDelay", "0").trim()));
    }

    @Override
    protected final List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    @Override
    protected final void initRegisterFactory() {
        this.setRegisterFactory(new RegisterFactory(this));
    }

    /**
     * Register factory for the SM352 sub metering modules of the EIMeter Flex.
     * As the modules do not support harmonics, and have different registers for
     * the firmware version and such, we have a different register factory.
     *
     * @author alex
     */
    private static final class RegisterFactory extends NDBaseRegisterFactory {

        private RegisterFactory(final Modbus protocol) {
            super(protocol);
        }

        @Override
        protected final void init() {
            super.init();

            this.getRegisters().add(new HoldingRegister(3586, 1, FIRMWARE_VERSION_REGISTER_NAME));
            this.getRegisters().add(new HoldingRegister(3584, 1, METERMODEL_REGISTER_NAME));
        }

        protected final void initParsers() {
            super.initParsers();
        }
    }

    public final String getProtocolVersion() {
        return "$Date: 2013-04-15 16:48:48 +0200 (ma, 15 apr 2013) $";
    }

    @Override
    public final String getFirmwareVersion() throws IOException {
        return String.valueOf(this.getRegisterFactory().findRegister(FIRMWARE_VERSION_REGISTER_NAME).objectValueWithParser("value0"));
    }

    public final Date getTime() {
        return Date.from(this.clock.instant());
    }

}