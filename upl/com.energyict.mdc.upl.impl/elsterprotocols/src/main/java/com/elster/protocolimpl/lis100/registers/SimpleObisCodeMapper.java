/**
 *
 */
package com.elster.protocolimpl.lis100.registers;

import com.elster.protocolimpl.lis100.ChannelData;
import com.elster.protocolimpl.lis100.DeviceData;
import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Basic functionality for Register reading support
 */
public class SimpleObisCodeMapper {

    private final RegisterMap registers;
    private final DeviceData deviceData;

    /**
     * Constructor with the {@link com.energyict.protocolimpl.iec1107.ProtocolLink}
     *
     * @param registers- an array of Lis100Register
     * @param deviceData - lis100 device data object
     */
    public SimpleObisCodeMapper(RegisterMap registers, DeviceData deviceData) {
        this.registers = registers;
        this.deviceData = deviceData;
    }

    /**
     * Read the {@link com.energyict.protocol.RegisterValue} from the device
     *
     * @param obisCode - the obisCode to read
     * @return the {@link com.energyict.protocol.RegisterValue}
     * @throws java.io.IOException - in case of an error
     */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        Lis100Register reg = registers.forObisCode(obisCode);
        if (reg == null) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }

        if ((deviceData.getNumberOfChannels() > 1) &&
                (deviceData.getChannels().size() != deviceData.getNumberOfChannels())) {
            for (int i = 0; i < deviceData.getNumberOfChannels(); i++) {
                deviceData.getChannelData(i, null, null);
            }
        }

        ChannelData channelData = deviceData.getChannelData(reg.getChannel(), null, null);
        if (channelData == null) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }

        double d;
        String value;
        Quantity quantity;

        switch (reg.getIdent()) {
            case Lis100Register.STATUS_REGISTER:
                int stateSummary = 0;
                for (ChannelData cd : deviceData.getChannels()) {
                    stateSummary |= cd.getStateRegister();
                }
                value = "0x" + Integer.toHexString(stateSummary);
                return new RegisterValue(obisCode, value);
            case Lis100Register.SOFTWARE_VERSION:
                value = deviceData.getObjectFactory().getSoftwareVersionObject().getValue();
                return new RegisterValue(obisCode, value);
            case Lis100Register.SENSOR_NUMBER:
                value = channelData.getSensorNumber();
                return new RegisterValue(obisCode, value);
            case Lis100Register.H1:
                d = channelData.getH1().getReading() * channelData.getMeterReadingFactor();
                quantity = new Quantity(d, channelData.getEISUnit());
                return new RegisterValue(obisCode, quantity, null, channelData.getH1().getDate());
            case Lis100Register.H2:
                d = channelData.getH2().getReading() * channelData.getMeterReadingFactor();
                quantity = new Quantity(d, channelData.getEISUnit());
                return new RegisterValue(obisCode, quantity, null, channelData.getH1().getDate());
            case Lis100Register.H2BOM:
                d = channelData.getH2Bom().getReading() * channelData.getMeterReadingFactor();
                quantity = new Quantity(d, channelData.getEISUnit());
                return new RegisterValue(obisCode, quantity, null, channelData.getH2Bom().getDate());
            default:
                return null;
        }

/*
				result = new RegisterValue(obisCode, val.toQuantity(), mdo
						.getMaxDate(), mdo.getBillingFrom(bod), mdo
						.getBillingTo(bod), tst, 0, "");
*/
    }

    public Lis100Register getRegister(ObisCode obisCode) {
        return registers.forObisCode(obisCode);
    }
}
