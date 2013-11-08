package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.PLCOFDMType2PHYAndMACCounters;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:08 PM
 */
public class PLCOFDMType2PHYAndMACCountersMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 10;

    public PLCOFDMType2PHYAndMACCountersMapping(final DlmsSession session) {
        super(session);
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

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, PLCOFDMType2PHYAndMACCounters.getDefaultObisCode().toString());

            // TX data packet count
            case 2:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacTxDataPacketCount(), Unit.getUndefined()));

            // RX data packet count
            case 3:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacRxDataPacketCount(), Unit.getUndefined()));

            // TX command packet count
            case 4:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacTxCmdPacketCount(), Unit.getUndefined()));

            // RX command packet count
            case 5:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacRxCmdPacketCount(), Unit.getUndefined()));

            // CSMA fail count
            case 6:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacCSMAFailCount(), Unit.getUndefined()));

            // CSMA collision count
            case 7:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacCSMACollisionCount(), Unit.getUndefined()));

            // Bad CRC count
            case 8:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacBadCrcCount(), Unit.getUndefined()));

            // Broadcast count
            case 9:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacBroadcastCount(), Unit.getUndefined()));

            // Multicast count
            case 10:
                return new RegisterValue(obisCode, new Quantity(macCounters.readMacMulticastCount(), Unit.getUndefined()));

            default:
                throw new NoSuchRegisterException("PLCOFDMType2PHYAndMACCounters attribute [" + obisCode.getB() + "] not supported!");

        }

    }

}
