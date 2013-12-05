package com.energyict.protocolimpl.modbus.energyict;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.northerndesign.NDBaseRegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
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

    @Override
    protected final void doTheConnect() throws IOException {
    }

    @Override
    protected final void doTheDisConnect() throws IOException {
    }

    @Override
    protected final void doTheValidateProperties(final Properties properties) throws MissingPropertyException, InvalidPropertyException {
        this.setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "25").trim()));
        this.setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getProperty("FirstTimeDelay", "0").trim()));
    }

    @Override
    protected final List<String> doTheGetOptionalKeys() {
        return new ArrayList<String>();
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

        /**
         * Create a new instance.
         *
         * @param protocol
         */
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
    public String getProtocolDescription() {
        return "EnergyICT EIFlex Meter";
    }

    public final String getProtocolVersion() {
        return "$Date: 2013-04-15 16:48:48 +0200 (ma, 15 apr 2013) $";
    }

    @Override
    public final String getFirmwareVersion() throws IOException, UnsupportedException {
        return String.valueOf(this.getRegisterFactory().findRegister(FIRMWARE_VERSION_REGISTER_NAME).objectValueWithParser("value0"));
    }

    public final Date getTime() throws IOException {
        return new Date();
    }

    /**
     * This is used to test the protocol.
     *
     * @param 	args		The arguments.
     */
    public final static void main(String[] args) {
        try {
            final Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            String comport;

            if ((args == null) || (args.length < 1)) {
                comport = "COM1";
            } else {
                comport = args[0];
            }

            dialer.init(comport);
            dialer.getSerialCommunicationChannel().setParams(9600, SerialCommunicationChannel.DATABITS_8, SerialCommunicationChannel.PARITY_NONE, SerialCommunicationChannel.STOPBITS_1);

            dialer.connect();

            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "60");
            properties.setProperty(MeterProtocol.ADDRESS, "2");
            properties.setProperty("HalfDuplex", "-1");
            properties.setProperty("PhysicalLayer", "0");

            final EIMeterFlexSlaveModule eiMeter = new EIMeterFlexSlaveModule();
            eiMeter.setProperties(properties);
            eiMeter.setHalfDuplexController(dialer.getHalfDuplexController());
            eiMeter.init(dialer.getInputStream(), dialer.getOutputStream(), TimeZone.getTimeZone("GMT"), logger);

            eiMeter.connect();

            System.out.println(eiMeter.getFirmwareVersion());
            System.out.println(eiMeter.getRegisterFactory().findRegister(ObisCode.fromString("1.1.32.7.0.255")).value());
        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Error while testing protocol : [" + e.getMessage() + "]", e);
            }
        }
    }
}
