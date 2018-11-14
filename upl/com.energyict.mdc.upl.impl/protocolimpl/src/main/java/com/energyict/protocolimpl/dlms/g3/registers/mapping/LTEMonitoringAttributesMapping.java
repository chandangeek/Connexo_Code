package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.LTEMonitoringIC;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by H165680 on 17/04/2017.
 */
public class LTEMonitoringAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 2;
    private static ObjectMapper mapper = new ObjectMapper();

    public LTEMonitoringAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return LTEMonitoringIC.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final LTEMonitoringIC lteMonitoringIC = getCosemObjectFactory().getLTEMonitoringIC(obisCode);
        return parse(obisCode, readAttribute(obisCode, lteMonitoringIC));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, LTEMonitoringIC lteMonitoringIC) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                return lteMonitoringIC.readLTEQoS();
            default:
                throw new NoSuchRegisterException("LTE Monitoring attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 2:
                Structure qos = abstractDataType.getStructure();
                String result = parseLTEQoSStructure(qos);
                return new RegisterValue(obisCode, result);
            default:
                throw new NoSuchRegisterException("LTE Monitoring attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    private String parseLTEQoSStructure(Structure structure) throws IOException {
        final LTEQoS lteQoS = new LTEQoS(structure);
        return mapper.writeValueAsString(lteQoS);
    }

    class LTEQoS {
        public int t3402;
        public int t3412;
        public int rsrq;
        public int rsrp;
        public int qRxlevMin;

        public LTEQoS(Structure structure) {
            t3402 = structure.getDataType(0).getUnsigned16().getValue();
            t3412 = structure.getDataType(1).getUnsigned16().getValue();
            rsrq = structure.getDataType(2).getUnsigned8().getValue();
            rsrp = structure.getDataType(3).getUnsigned8().getValue();
            qRxlevMin = structure.getDataType(4).getInteger8().getValue();
        }
    }

}
