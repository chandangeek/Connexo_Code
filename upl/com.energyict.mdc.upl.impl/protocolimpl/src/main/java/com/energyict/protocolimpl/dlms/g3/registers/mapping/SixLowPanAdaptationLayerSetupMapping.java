package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.SixLowPanAdaptationLayerSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:08 PM
 */
public class SixLowPanAdaptationLayerSetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 12;

    public SixLowPanAdaptationLayerSetupMapping(final DlmsSession session) {
        super(session);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return SixLowPanAdaptationLayerSetup.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {

        final SixLowPanAdaptationLayerSetup sixLowPanSetup = getCosemObjectFactory().getSixLowPanAdaptationLayerSetup();

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, SixLowPanAdaptationLayerSetup.getDefaultObisCode().toString());

            // Max hops
            case 2:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpMaxHops(), Unit.getUndefined()));

            // Weak LQI value
            case 3:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpWeakLQIValue(), Unit.getUndefined()));

            // PAN conflict wait
            case 4:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpPanConflictWait(), Unit.get("s")));

            // MAX pan conflict count
            case 5:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpMaxPanConflictCount(), Unit.getUndefined()));

            // Active scan duration
            case 6:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpActiveScanDuration(), Unit.get("s")));

            // Tone mask
            case 7:
                return new RegisterValue(obisCode, sixLowPanSetup.readAdpToneMask().toString());

            // Discover attempts speed
            case 8:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpDiscoveryAttemptsSpeed(), Unit.get("s")));

            // Routing configuration
            case 9:
                return new RegisterValue(obisCode, sixLowPanSetup.readAdpRoutingConfiguration().toString());

            // Broadcast log table entry TTL
            case 10:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpBroadcastLogTableEntryTTL(), Unit.get("s")));

            // Max age time
            case 11:
                return new RegisterValue(obisCode, new Quantity(sixLowPanSetup.readAdpMaxAgeTime(), Unit.get("s")));

            // Routing table
            case 12:
                return new RegisterValue(obisCode, getShortDescription(sixLowPanSetup.readAdpRoutingTable()));

            default:
                throw new NoSuchRegisterException("PLCOFDMType2PHYAndMACCounters attribute [" + obisCode.getB() + "] not supported!");

        }

    }
}