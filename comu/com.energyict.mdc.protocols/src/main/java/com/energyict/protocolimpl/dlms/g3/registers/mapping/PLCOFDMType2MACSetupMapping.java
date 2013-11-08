package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.PLCOFDMType2MACSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:08 PM
 */
public class PLCOFDMType2MACSetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 9;

    public PLCOFDMType2MACSetupMapping(final DlmsSession session) {
        super(session);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return PLCOFDMType2MACSetup.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {

        final PLCOFDMType2MACSetup macSetup = getCosemObjectFactory().getPLCOFDMType2MACSetup();

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, obisCode.toString());

            // MAC short address
            case 2:
                return new RegisterValue(obisCode, new Quantity(macSetup.readShortAddress(), Unit.getUndefined()));

            // Associated PAN coordinator
            case 3:
                return new RegisterValue(obisCode, String.valueOf(macSetup.readAssociatedPanCoord()));

            // Coordinator short address
            case 4:
                return new RegisterValue(obisCode, new Quantity(macSetup.readCoordinatorShortAddress(), Unit.getUndefined()));

            // PAN id
            case 5:
                return new RegisterValue(obisCode, new Quantity(macSetup.readPanId(), Unit.getUndefined()));

            // Number of hops
            case 6:
                return new RegisterValue(obisCode, new Quantity(macSetup.readNumberOfHops(), Unit.getUndefined()));

            // Max orphan timer
            case 7:
                return new RegisterValue(obisCode, new Quantity(macSetup.readMaxOrphanTimer(), Unit.get("s")));

            // MAC neighbour table
            case 8:
                return new RegisterValue(obisCode, getShortDescription(macSetup.readNeighborTable()));

            // MAC security activation
            case 9:
                return new RegisterValue(obisCode, macSetup.readSecurityActivation().toString());

            default:
                throw new NoSuchRegisterException("PLCOFDMType2MACSetup attribute [" + obisCode.getB() + "] not supported!");

        }

    }


}
