package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;

import java.util.Optional;

public class CollectedRegisterReaderFinder {

    private final DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> specificReadableRegisters;
    private final DLMSReaderRegistry<CollectedRegister, OfflineRegister, DLMSClassId> dataClassReadableRegisters;

    public CollectedRegisterReaderFinder(DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> specificReadableRegisters, DLMSReaderRegistry<CollectedRegister, OfflineRegister, DLMSClassId> dataClassReadableRegisters) {
        this.specificReadableRegisters = specificReadableRegisters;
        this.dataClassReadableRegisters = dataClassReadableRegisters;
    }

    public Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?>> find(AbstractDlmsProtocol dlmsProtocol, OfflineRegister register) {
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?>> reader = specificReadableRegisters.from(register.getObisCode());
        if (!reader.isPresent()) {
            try {
                UniversalObject universalObject = dlmsProtocol.getDlmsSession().getMeterConfig().findObject(register.getObisCode());
                reader = dataClassReadableRegisters.from(universalObject.getDLMSClassId());
            } catch (NotInObjectListException e) {
                return Optional.empty();
            }
        }
        return reader;
    }

}
