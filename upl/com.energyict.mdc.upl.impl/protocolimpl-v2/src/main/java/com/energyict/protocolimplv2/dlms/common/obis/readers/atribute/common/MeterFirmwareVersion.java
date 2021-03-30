package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.common;

import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.util.Optional;

public class MeterFirmwareVersion<T extends AbstractDlmsProtocol> implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, T> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ObisCode obisCode;

    public MeterFirmwareVersion(CollectedRegisterBuilder collectedRegisterBuilder, ObisCode obisCode) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.obisCode = obisCode;
    }

    @Override
    public CollectedRegister read(T protocol, OfflineRegister readingSpecs) {
        CollectedFirmwareVersion firmwareVersions = protocol.getFirmwareVersions();
        Optional<String> activeMeterFirmwareVersion = firmwareVersions.getActiveMeterFirmwareVersion();
        if (activeMeterFirmwareVersion.isPresent()) {
            return collectedRegisterBuilder.createCollectedRegister(readingSpecs, new RegisterValue(readingSpecs, activeMeterFirmwareVersion.get()));
        }
        return collectedRegisterBuilder.createCollectedRegister(readingSpecs, ResultType.NotSupported, "Could not read firmware version");
    }

    @Override
    public boolean isApplicable(ObisCode o) {
        return obisCode.equals(o);
    }
}
