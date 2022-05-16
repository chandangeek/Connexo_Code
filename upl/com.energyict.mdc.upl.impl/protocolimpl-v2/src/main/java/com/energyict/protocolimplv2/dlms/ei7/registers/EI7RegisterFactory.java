package com.energyict.protocolimplv2.dlms.ei7.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.cbo.BaseUnit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.registers.A2RegisterFactory;
import com.energyict.protocolimplv2.dlms.ei7.EI7;

import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.DATA_VOLUME_UNIT_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.EI7_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.EI7_DEFAULT_DATA_VOLUME_UNIT_PROPERTY;

public class EI7RegisterFactory extends A2RegisterFactory {
    private final static ObisCode CONVERTED_VOLUME_OBIS_CODE = ObisCode.fromString("7.0.13.2.0.255");
    private final static ObisCode CONVERTED_VOLUME_UNDER_ALARM_OBIS_CODE = ObisCode.fromString("7.0.12.2.0.255");

    private final EI7 protocol;

    public EI7RegisterFactory(EI7 ei7, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(ei7, collectedDataFactory, issueFactory);
        this.protocol = ei7;
    }

    @Override
    public int getDataVolumeUnit(ObisCode obisCode) {
        return isDataVolumeObisCode(obisCode) ?
                this.protocol.getOfflineDevice().getAllProperties().getTypedProperty(DATA_VOLUME_UNIT_PROPERTY, EI7_DEFAULT_DATA_VOLUME_UNIT_PROPERTY) :
                BaseUnit.UNITLESS;
    }

    @Override
    public int getDataVolumeScalar(ObisCode obisCode) {
        return isDataVolumeObisCode(obisCode) ?
                this.protocol.getOfflineDevice().getAllProperties().getTypedProperty(DATA_VOLUME_SCALAR_PROPERTY, EI7_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY) : 0;
    }

    protected boolean isDataVolumeObisCode(ObisCode obisCode) {
        return obisCode != null &&
                obisCode.getA() == CONVERTED_VOLUME_OBIS_CODE.getA() &&
                obisCode.getB() == CONVERTED_VOLUME_OBIS_CODE.getB() &&
                (obisCode.getC() == CONVERTED_VOLUME_OBIS_CODE.getC() || obisCode.getC() ==  CONVERTED_VOLUME_UNDER_ALARM_OBIS_CODE.getC()) &&
                obisCode.getD() == CONVERTED_VOLUME_OBIS_CODE.getD();
    }
}
