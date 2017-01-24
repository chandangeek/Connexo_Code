package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.PLCOFDMType2PHYAndMACCounters;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:08 PM
 */
public class PLCOFDMType2PHYAndMACCountersMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 10;

    public PLCOFDMType2PHYAndMACCountersMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return PLCOFDMType2PHYAndMACCounters.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final PLCOFDMType2PHYAndMACCounters macCounters = getCosemObjectFactory().getPLCOFDMType2PHYAndMACCounters();
        return parse(obisCode, readAttribute(obisCode, macCounters));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, PLCOFDMType2PHYAndMACCounters macCounters) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(PLCOFDMType2PHYAndMACCounters.getDefaultObisCode());

            // TX data packet count
            case 2:
                return macCounters.readMacTxDataPacketCount();

            // RX data packet count
            case 3:
                return macCounters.readMacRxDataPacketCount();

            // TX command packet count
            case 4:
                return macCounters.readMacTxCmdPacketCount();

            // RX command packet count
            case 5:
                return macCounters.readMacRxCmdPacketCount();

            // CSMA fail count
            case 6:
                return macCounters.readMacCSMAFailCount();

            // CSMA no ACK count
            case 7:
                return macCounters.readMacNoAckCount();

            // Bad CRC count
            case 8:
                return macCounters.readMacBadCrcCount();

            // TX data broadcast count
            case 9:
                return macCounters.readMacTxDataBroadcastCount();

            // RX data broadcast count
            case 10:
                return macCounters.readMacRxDataBroadcastCount();

            default:
                throw new NoSuchRegisterException("PLCOFDMType2PHYAndMACCounters attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {


        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, PLCOFDMType2PHYAndMACCounters.getDefaultObisCode().toString());

            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.longValue(), Unit.getUndefined()));

            default:
                throw new NoSuchRegisterException("PLCOFDMType2PHYAndMACCounters attribute [" + obisCode.getB() + "] not supported!");

        }
    }
}
