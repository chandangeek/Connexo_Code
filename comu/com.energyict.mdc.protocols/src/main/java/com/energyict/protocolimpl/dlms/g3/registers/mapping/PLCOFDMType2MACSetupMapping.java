package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.PLCOFDMType2MACSetup;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:08 PM
 */
public class PLCOFDMType2MACSetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 22;

    public PLCOFDMType2MACSetupMapping(final DlmsSession session) {
        super(session);
    }

    public PLCOFDMType2MACSetupMapping(Logger logger, CosemObjectFactory cosemObjectFactory) {
        super(logger, cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return PLCOFDMType2MACSetup.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {

        AbstractDataType abstractDataType = readAttribute(obisCode, getCosemObjectFactory().getPLCOFDMType2MACSetup());

        return parse(obisCode, abstractDataType);

    }

    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws NoSuchRegisterException {
        switch (obisCode.getB()) {

            case 1:
                return new RegisterValue(obisCode, PLCOFDMType2MACSetup.getDefaultObisCode().toString());

            case 2:
            case 3:
            case 4:
            case 9:
            case 12:
            case 13:
            case 15:
            case 16:
            case 17:
            case 18:
            case 20:
            case 21:
            case 22:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            case 7:
                return new RegisterValue(obisCode, ((BitString) abstractDataType).toString());

            case 8:
            case 10:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.get(BaseUnit.MINUTE)));

            case 11:
                return new RegisterValue(obisCode, getShortDescription((Array) abstractDataType));

            case 14:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.get(BaseUnit.SECOND)));

            default:
                throw new NoSuchRegisterException("PLCOFDMType2MACSetup attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    private AbstractDataType readAttribute(ObisCode obisCode, PLCOFDMType2MACSetup macSetup) throws IOException {

        switch (obisCode.getB()) {

            case 1:
                return OctetString.fromObisCode(PLCOFDMType2MACSetup.getDefaultObisCode());

            case 2:
                return macSetup.readShortAddress();

            case 3:
                return macSetup.readRCCoord();

            case 4:
                return macSetup.readPanId();

            case 7:
                return macSetup.readToneMask();

            case 8:
                return macSetup.readTMRTTL();

            case 9:
                return macSetup.readMaxFrameRetries();

            case 10:
                return macSetup.readNeighbourTableEntryTTL();

            case 11:
                return macSetup.readNeighbourTable();

            case 12:
                return macSetup.readHighPriorityWindowSize();

            case 13:
                return macSetup.readCSMAFairnessLimit();

            case 14:
                return macSetup.readBeaconRandomizationWindowLength();

            case 15:
                return macSetup.readMacA();

            case 16:
                return macSetup.readMacK();

            case 17:
                return macSetup.readMinCWAttempts();

            case 18:
                return macSetup.readCenelecLegacyMode();

            case 20:
                return macSetup.readMaxBE();

            case 21:
                return macSetup.readMaxCSMABackoff();

            case 22:
                return macSetup.readMinBE();

            default:
                throw new NoSuchRegisterException("PLCOFDMType2MACSetup attribute [" + obisCode.getB() + "] not supported!");

        }
    }


}