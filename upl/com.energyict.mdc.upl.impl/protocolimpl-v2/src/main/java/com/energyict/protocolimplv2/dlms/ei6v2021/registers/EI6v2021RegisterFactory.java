package com.energyict.protocolimplv2.dlms.ei6v2021.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.cbo.BaseUnit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021;
import com.energyict.protocolimplv2.dlms.ei7.registers.EI7RegisterFactory;

import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.DATA_VOLUME_UNIT_PROPERTY;

public class EI6v2021RegisterFactory extends EI7RegisterFactory {
    private final EI6v2021 protocol;

    public EI6v2021RegisterFactory(EI6v2021 ei6, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(ei6, collectedDataFactory, issueFactory);
        this.protocol = ei6;
    }

    @Override
    public int getDataVolumeUnit(ObisCode obisCode) {
        return isDataVolumeObisCode(obisCode) ?
                this.protocol.getOfflineDevice().getAllProperties().getTypedProperty(DATA_VOLUME_UNIT_PROPERTY, EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY) :
                BaseUnit.UNITLESS;
    }

    @Override
    public int getDataVolumeScalar(ObisCode obisCode) {
        return isDataVolumeObisCode(obisCode) ?
                this.protocol.getOfflineDevice().getAllProperties().getTypedProperty(DATA_VOLUME_SCALAR_PROPERTY, EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY) : 0;
    }
}
