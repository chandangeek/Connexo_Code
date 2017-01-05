package com.energyict.protocolimpl.modbus.energyict;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.northerndesign.NDBaseRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Protocol class for reading out an EIMeter flex SM352 module.
 *
 * @author alex
 */
@SuppressWarnings("unchecked")
public class EIMeterFlexSlaveModule extends Modbus {

    /** Logger instance. */
    private static final Logger logger = Logger.getLogger(EIMeterFlexSlaveModule.class.getName());

    /** The name of the register that contains the firmware version. */
    private static final String FIRMWARE_VERSION_REGISTER_NAME = "FirmwareVersion";

    /** The name of the register that contains the meter model. */
    private static final String METERMODEL_REGISTER_NAME = "MeterModel";

    public final DiscoverResult discover(final DiscoverTools discoverTools) {
        return null;
    }

    public EIMeterFlexSlaveModule(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected final void doTheConnect() {
    }

    @Override
    protected final void doTheDisConnect() {
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        this.setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "25").trim()));
        this.setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getTypedProperty(PK_FIRST_TIME_DELAY, "0").trim()));
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

    @Override
    public final String getProtocolVersion() {
        return "$Date: 2013-04-15 16:48:48 +0200 (ma, 15 apr 2013) $";
    }

    @Override
    public final String getFirmwareVersion() throws IOException {
        return String.valueOf(this.getRegisterFactory().findRegister(FIRMWARE_VERSION_REGISTER_NAME).objectValueWithParser("value0"));
    }

    @Override
    public final Date getTime() {
        return new Date();
    }

}