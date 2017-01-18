/*
 *
 */
package com.energyict.protocolimpl.modbus.eimeter;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.northerndesign.NDBaseRegisterFactory;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author fbo
 *         Changes:
 *         JME	|06042009|	Added harmonics registers 2 to 15 for V1, V2, V3, I1, I2 and I3.
 *         JME	|27042009|	Added instantaneous power registers, per phase.
 */
public class EIMeter extends Modbus {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT EIMeter Modbus";
    }

    @Inject
    public EIMeter(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doTheConnect() throws IOException { /* relax */ }

    protected void doTheDisConnect() throws IOException { /* relax */ }

    protected void doTheValidateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "25").trim()));
        setInfoTypeMeterFirmwareVersion(properties.getProperty("MeterFirmwareVersion", "1.07"));
        if (Float.parseFloat(getInfoTypeMeterFirmwareVersion())>=1.07) {
            setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getProperty("FirstTimeDelay", "0").trim()));
        }
        else {
            setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getProperty("FirstTimeDelay", "400").trim()));
        }
    }

    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();

    }

    public String getFirmwareVersion() throws IOException {
        return "" + getRegisterFactory().findRegister("firmwareVersion").objectValueWithParser("value0");

    }

    public String getProtocolVersion() {
        return "$Date: 2012-03-27 09:33:50 +0200 (di, 27 mrt 2012) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    public Date getTime() throws IOException {
        return new Date();
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }

    /*******************************************************************************************
     M e s s a g e P r o t o c o l  i n t e r f a c e  d e l e g a t e d  m e t h o d s
     *******************************************************************************************/
    protected MessageResult doQueryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<TestMessage")) {
                getLogger().info(messageEntry.getContent());
                return MessageResult.createSuccess(messageEntry);
            }
            else {
                return MessageResult.createUnknown(messageEntry);
            }
        }
        catch(Exception e) {
            return MessageResult.createFailed(messageEntry);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<MessageCategorySpec> doGetMessageCategories(List theCategories) {
        MessageCategorySpec cat = new MessageCategorySpec(getDeviceName() + " messages");
        MessageSpec msgSpec = addBasicMsg("Test message", "TestMessage", false);
        cat.addMessageSpec(msgSpec);
        theCategories.add(cat);
        return theCategories;
    }

    protected String getDeviceName() {
        return "EIMeter";
    }

    /**
     * Register factory for the {@link com.energyict.protocolimpl.modbus.eimeter.EIMeter} protocol. This adds harmonics and model specifics.
     *
     * @author alex
     */
    private static final class RegisterFactory extends NDBaseRegisterFactory {

        private RegisterFactory(final Modbus protocol) {
            super(protocol);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final void init() {
            super.init();

            /* V1 Harmonics registers */
            getRegisters().add(new HoldingRegister(7936, 1, toObis("1.1.32.7.2.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7937, 1, toObis("1.1.32.7.3.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7938, 1, toObis("1.1.32.7.4.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7939, 1, toObis("1.1.32.7.5.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7940, 1, toObis("1.1.32.7.6.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7941, 1, toObis("1.1.32.7.7.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7942, 1, toObis("1.1.32.7.8.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7943, 1, toObis("1.1.32.7.9.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7944, 1, toObis("1.1.32.7.10.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7945, 1, toObis("1.1.32.7.11.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7946, 1, toObis("1.1.32.7.12.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7947, 1, toObis("1.1.32.7.13.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7948, 1, toObis("1.1.32.7.14.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(7949, 1, toObis("1.1.32.7.15.255"), Unit.get("%")));

            /* V2 Harmonics registers */
            getRegisters().add(new HoldingRegister(8192, 1, toObis("1.1.52.7.2.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8193, 1, toObis("1.1.52.7.3.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8194, 1, toObis("1.1.52.7.4.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8195, 1, toObis("1.1.52.7.5.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8196, 1, toObis("1.1.52.7.6.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8197, 1, toObis("1.1.52.7.7.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8198, 1, toObis("1.1.52.7.8.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8199, 1, toObis("1.1.52.7.9.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8200, 1, toObis("1.1.52.7.10.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8201, 1, toObis("1.1.52.7.11.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8202, 1, toObis("1.1.52.7.12.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8203, 1, toObis("1.1.52.7.13.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8204, 1, toObis("1.1.52.7.14.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8205, 1, toObis("1.1.52.7.15.255"), Unit.get("%")));

            /* V3 Harmonics registers */
            getRegisters().add(new HoldingRegister(8448, 1, toObis("1.1.72.7.2.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8449, 1, toObis("1.1.72.7.3.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8450, 1, toObis("1.1.72.7.4.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8451, 1, toObis("1.1.72.7.5.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8452, 1, toObis("1.1.72.7.6.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8453, 1, toObis("1.1.72.7.7.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8454, 1, toObis("1.1.72.7.8.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8455, 1, toObis("1.1.72.7.9.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8456, 1, toObis("1.1.72.7.10.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8457, 1, toObis("1.1.72.7.11.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8458, 1, toObis("1.1.72.7.12.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8459, 1, toObis("1.1.72.7.13.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8460, 1, toObis("1.1.72.7.14.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8461, 1, toObis("1.1.72.7.15.255"), Unit.get("%")));

            /* I1 Harmonics registers */
            getRegisters().add(new HoldingRegister(8704, 1, toObis("1.1.31.7.2.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8705, 1, toObis("1.1.31.7.3.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8706, 1, toObis("1.1.31.7.4.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8707, 1, toObis("1.1.31.7.5.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8708, 1, toObis("1.1.31.7.6.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8709, 1, toObis("1.1.31.7.7.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8710, 1, toObis("1.1.31.7.8.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8711, 1, toObis("1.1.31.7.9.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8712, 1, toObis("1.1.31.7.10.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8713, 1, toObis("1.1.31.7.11.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8714, 1, toObis("1.1.31.7.12.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8715, 1, toObis("1.1.31.7.13.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8716, 1, toObis("1.1.31.7.14.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8717, 1, toObis("1.1.31.7.15.255"), Unit.get("%")));

            /* I2 Harmonics registers */
            getRegisters().add(new HoldingRegister(8960, 1, toObis("1.1.51.7.2.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8961, 1, toObis("1.1.51.7.3.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8962, 1, toObis("1.1.51.7.4.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8963, 1, toObis("1.1.51.7.5.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8964, 1, toObis("1.1.51.7.6.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8965, 1, toObis("1.1.51.7.7.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8966, 1, toObis("1.1.51.7.8.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8967, 1, toObis("1.1.51.7.9.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8968, 1, toObis("1.1.51.7.10.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8969, 1, toObis("1.1.51.7.11.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8970, 1, toObis("1.1.51.7.12.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8971, 1, toObis("1.1.51.7.13.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8972, 1, toObis("1.1.51.7.14.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(8973, 1, toObis("1.1.51.7.15.255"), Unit.get("%")));

            /* I3 Harmonics registers */
            getRegisters().add(new HoldingRegister(9216, 1, toObis("1.1.71.7.2.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9217, 1, toObis("1.1.71.7.3.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9218, 1, toObis("1.1.71.7.4.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9219, 1, toObis("1.1.71.7.5.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9220, 1, toObis("1.1.71.7.6.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9221, 1, toObis("1.1.71.7.7.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9222, 1, toObis("1.1.71.7.8.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9223, 1, toObis("1.1.71.7.9.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9224, 1, toObis("1.1.71.7.10.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9225, 1, toObis("1.1.71.7.11.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9226, 1, toObis("1.1.71.7.12.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9227, 1, toObis("1.1.71.7.13.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9228, 1, toObis("1.1.71.7.14.255"), Unit.get("%")));
            getRegisters().add(new HoldingRegister(9229, 1, toObis("1.1.71.7.15.255"), Unit.get("%")));

            getRegisters().add(new HoldingRegister(3592, 1, "firmwareVersion"));
            getRegisters().add(new HoldingRegister(3590, 1, "MeterModel"));
        }
    }
}
