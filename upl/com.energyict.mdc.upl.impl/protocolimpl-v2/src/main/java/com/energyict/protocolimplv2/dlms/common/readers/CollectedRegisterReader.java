package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectedRegisterReader<T extends AbstractDlmsProtocol> implements DeviceRegisterSupport {


    private final CollectedRegisterReaderFinder<T> collectedRegisterReaderFinder;

    private final T dlmsProtocol;
    private final CollectedRegisterBuilder collectedRegisterBuilder;
    public CollectedRegisterReader(CollectedRegisterReaderFinder<T> collectedRegisterReaderFinder, T dlmsProtocol, CollectedRegisterBuilder collectedRegisterBuilder) {
        this.collectedRegisterReaderFinder = collectedRegisterReaderFinder;
        this.dlmsProtocol = dlmsProtocol;
        this.collectedRegisterBuilder = collectedRegisterBuilder;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>(registers.size());
        for (OfflineRegister register : registers) {
            Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?, T>> optionalReadableRegisterObisCode = collectedRegisterReaderFinder.find(dlmsProtocol, register);
            if (optionalReadableRegisterObisCode.isPresent()) {
                ObisReader<CollectedRegister, OfflineRegister, ?, T> collectedRegister = optionalReadableRegisterObisCode.get();
                collectedRegisters.add(collectedRegister.read(dlmsProtocol, register));
            } else {
                collectedRegisters.add(collectedRegisterBuilder.createCollectedRegister(register, ResultType.NotSupported, "No reader found"));
            }
        }
        return collectedRegisters;
    }

    public CollectedRegisterReaderFinder<T> getCollectedRegisterReaderFinder() {
        return collectedRegisterReaderFinder;
    }

}
