package com.energyict.protocolimplv2.dlms.itron.em620.registers;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.util.List;

public class EM620RegisterFactory implements DeviceRegisterSupport {
    private final AbstractDlmsProtocol mProtocol;

    final ObisCode ALLDEMANDS_PROFILE = ObisCode.fromString("0.0.98.133.5.255");
    final ObisCode ALLMAXIMUMDEMANDS_PROFILE = ObisCode.fromString("0.0.98.133.6.255");
    final ObisCode ALLCUMULATIVEMAXDEMANDS_PROFILE = ObisCode.fromString("0.0.98.133.90.255");
    final ObisCode ALLTOTALENERGIES_PROFILE = ObisCode.fromString("255.255.98.133.2.255");
    final ObisCode ALLENERGYRATES_PROFILE = ObisCode.fromString("255.255.98.133.1.255");

    public EM620RegisterFactory(AbstractDlmsProtocol protocol) {
        this.mProtocol = protocol;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }
}
