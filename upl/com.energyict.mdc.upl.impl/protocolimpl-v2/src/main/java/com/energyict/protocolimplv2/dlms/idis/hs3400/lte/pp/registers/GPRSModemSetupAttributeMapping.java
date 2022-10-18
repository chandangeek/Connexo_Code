package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;

import java.io.IOException;

public class GPRSModemSetupAttributeMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 4;

    public GPRSModemSetupAttributeMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(ObisCode obisCode) {
        return GPRSModemSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final GPRSModemSetup gprsModemSetup = getCosemObjectFactory().getGPRSModemSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, gprsModemSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, GPRSModemSetup gprsModemSetup) throws IOException {

        switch (obisCode.getE()) {
            case 1:
                return OctetString.fromObisCode(obisCode);
            case 2:
                return gprsModemSetup.getAPN();
            case 3:
                return gprsModemSetup.getPinCod();
            case 4:
                return gprsModemSetup.getQualityOfService();
            default:
                throw new NoSuchRegisterException("GPRSModemSetup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 1:
                return new RegisterValue(obisCode, "Logical name: " + abstractDataType.getOctetString().stringValue());
            case 2:
                return new RegisterValue(obisCode, "APN: " + abstractDataType.getOctetString().stringValue());
            case 3:
                return new RegisterValue(obisCode, "PIN code: " + abstractDataType.getUnsigned16());
            case 4:
                return new RegisterValue(obisCode, "Quality of service: " + getQualityOfServiceString(abstractDataType));
            default:
                throw new NoSuchRegisterException("GPRSModemSetup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    public String getObisCode(AbstractDataType pppSetupAttribute) throws NumberFormatException {
        byte[] obisCodeBytes = pppSetupAttribute.getOctetString().toByteArray();
        ObisCode obisCode = ObisCode.fromByteArray(obisCodeBytes);
        return obisCode.toString();
    }

    public String getQualityOfServiceString(AbstractDataType gprsModemSetupAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (gprsModemSetupAttribute.isStructure() && gprsModemSetupAttribute.getStructure().nrOfDataTypes() == 2) {
            // parsing for quality of service
            Structure defaultNetworkSpecs = gprsModemSetupAttribute.getStructure().getDataType(0).getStructure();

            builder.append(" Default network characteristics - Precedence: ")
                    .append(defaultNetworkSpecs.getStructure().getDataType(0).toBigDecimal())
                    .append(", delay: ")
                    .append(defaultNetworkSpecs.getStructure().getDataType(1).toBigDecimal())
                    .append(", reliability: ")
                    .append(defaultNetworkSpecs.getStructure().getDataType(2).toBigDecimal())
                    .append(", peak throughput: ")
                    .append(defaultNetworkSpecs.getStructure().getDataType(3).toBigDecimal())
                    .append(", mean throughput: ")
                    .append(defaultNetworkSpecs.getStructure().getDataType(4).toBigDecimal());

            Structure requestedNetworkSpecs = gprsModemSetupAttribute.getStructure().getDataType(1).getStructure();

            builder.append(" Requested network characteristics - Precedence: ")
                    .append(requestedNetworkSpecs.getStructure().getDataType(0).toBigDecimal())
                    .append(", delay: ")
                    .append(requestedNetworkSpecs.getStructure().getDataType(1).toBigDecimal())
                    .append(", reliability: ")
                    .append(requestedNetworkSpecs.getStructure().getDataType(2).toBigDecimal())
                    .append(", peak throughput: ")
                    .append(requestedNetworkSpecs.getStructure().getDataType(3).toBigDecimal())
                    .append(", mean throughput: ")
                    .append(requestedNetworkSpecs.getStructure().getDataType(4).toBigDecimal());

            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct GPRS modem setup format.");
        }
    }

}
