package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKMacCounters;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Created by iulian on 5/12/2016.
 */
public class IDISGatewaySFSKMacCountersMapping extends RegisterMapping{
    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 8;

    public IDISGatewaySFSKMacCountersMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(ObisCode obisCode) {
        final ObisCode defaultObisCode = SFSKMacCounters.getDefaultObisCode();
        return ProtocolTools.equalsIgnoreBField(defaultObisCode, obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(ObisCode obisCode) throws IOException {
        AbstractDataType abstractDataType = readAttribute(obisCode, getCosemObjectFactory().getSFSKMacCounters());
        return parse(obisCode, abstractDataType);
    }

    private AbstractDataType readAttribute(ObisCode obisCode, AbstractCosemObject abstractCosemObject) throws NoSuchRegisterException {
        SFSKMacCounters sfskMacCounters = (SFSKMacCounters)abstractCosemObject;

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(sfskMacCounters.getDefaultObisCode());
            case 2:
                return sfskMacCounters.getSynchronizationRegister();
            case 3:
                return sfskMacCounters.getDesynchronizationListing();
            case 4:
                return sfskMacCounters.getBroadcastFramesCounter();
            case 5:
                return sfskMacCounters.getRepetitionsCounter();
            case 6:
                return sfskMacCounters.getTransmissionsCounter();
            case 7:
                return sfskMacCounters.getCrcOkFramesCounter();
            case 8:
                return sfskMacCounters.getCrcNOkFramesCounter();

            default:
                throw new NoSuchRegisterException("SFSKMacCountersMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    /**
     * Using the mapping found at https://confluence.eict.vpdc/display/RTUSRV/DLMS+Server
     *
     * @param obisCode
     * @param abstractDataType
     * @return
     * @throws IOException
     */
    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {
        switch (obisCode.getB()) {

            case 1:
                return new RegisterValue(obisCode, SFSKMacCounters.getDefaultObisCode().toString());

            /**
             * synchronization_listing			array		"array of synchronization-couples
             *
             This variable counts the number of synchronization processes performed by the system. For more details, see Blue Book.
             In current implementation, emty array is always returned."
             */
            case 2:
                // In current implementation, emty array is always returned
                return new RegisterValue(obisCode, "n/a");

            /**
             * desynchronization_listing
             "This variable counts the number of desynchronizations that occurred depending on their cause. For more details, see Blue Book.
             In current implementation, all-zero values are returned."
             */
            case 3:
                // all-zero values are returned
                return new RegisterValue(obisCode, "n/a");

            /**
             * broadcast_frames_counter
             "Counts the broadcast frames received by the server system and issued from a client system. For more details, see Blue Book.
             In current implementation, empty array is returned."
             */
            case 4:
                //In current implementation, empty array is returned.
                return new RegisterValue(obisCode, "n/a");

            //repetitions_counter
            case 5:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // retransmisions_counter
            case 6:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // CRC_OK_frames_counter
            case 7:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // CRC_NOK_frames_counter
            case 8:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            default:
                throw new NoSuchRegisterException("SFSK MacCounters attribute [" + obisCode.getB() + "] not supported!");
        }
    }
}
