package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.SingleActionScheduleAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by H165680 on 6/16/2017.
 */
public class SchedulerMapping extends G3Mapping {
    public static final ObisCode SCHEDULER_BASE_OBISCODE = ObisCode.fromString("0.0.15.0.0.255");
    private final TimeZone timeZone;
    private SingleActionScheduleAttributesMapping singleActionScheduleAttributesMapping;

    protected SchedulerMapping(TimeZone timeZone, ObisCode obis) {
        super(obis);
        this.timeZone = timeZone;
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the B-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 1, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (singleActionScheduleAttributesMapping == null) {
            singleActionScheduleAttributesMapping = new SingleActionScheduleAttributesMapping(timeZone, cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getB();        //The B-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (singleActionScheduleAttributesMapping.canRead(getObisCode())) {
            return singleActionScheduleAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (singleActionScheduleAttributesMapping.canRead(obisCode)) {
            return singleActionScheduleAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if(getObisCode().equalsIgnoreBAndEChannel(SCHEDULER_BASE_OBISCODE) ){
            return DLMSClassId.SINGLE_ACTION_SCHEDULE.getClassId();
        } else {
            return -1;
        }
    }
}
