package com.energyict.protocolimplv2.dlms.acud.properties;


import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class AcudDlmsProperties extends DlmsProperties {

    @Override
    public byte[] getSystemIdentifier() {
        return null;
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        ConformanceBlock conformanceBlock = super.getConformanceBlock();
        conformanceBlock.setPriorityManagementSupported(false);
        conformanceBlock.setAttribute0SupportedWithGet(false);
        conformanceBlock.setEventNotification(false);
        return conformanceBlock;
    }

}