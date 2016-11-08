package com.energyict.protocolimpl.dlms.g3.registers.mapping;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GPRSModemSetup;

import java.io.IOException;

/**
 * Created by astor on 22.09.2016.
 */
public class GprsModemSetupAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 11;

    private static final int AUTOMATIC = 0;
    private static final int MANUAL = 1;

    private static final int GPRS = 0;
    private static final int UTRAN = 1;
    private static final int EUTRAN = 2;

    public GprsModemSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return GPRSModemSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final GPRSModemSetup gprsModemSetup = getCosemObjectFactory().getGPRSModemSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, gprsModemSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, GPRSModemSetup gprsModemSetup) throws IOException {
        switch (obisCode.getB()) {
            // Logical name
            case 1:
                return OctetString.fromObisCode(GPRSModemSetup.getDefaultObisCode());
            case 2:
                return gprsModemSetup.readAPN();
            case 3:
                return gprsModemSetup.readPinCode();
            case 4:
                return gprsModemSetup.readQualityOfService();
            case 5:
                return gprsModemSetup.readNetworkSelectionMode();
            case 6:
                return gprsModemSetup.readPreferredOperatorList();
            case 7:
                return gprsModemSetup.readIntlRoamingAllowed();
            case 8:
                return gprsModemSetup.readMinimumRssi();
            case 9:
                return gprsModemSetup.readMaximumBer();
            case 10:
                return gprsModemSetup.readNetworkTechnology();
            case 11:
                return gprsModemSetup.readIsGprsPreferred();
            default:
                throw new NoSuchRegisterException("GPRS Setup attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, GPRSModemSetup.getDefaultObisCode().toString());
            case 2:
                return new RegisterValue(obisCode, "APN: " + abstractDataType.getOctetString().stringValue());
            case 3:
                return new RegisterValue(obisCode, "Pin code: " + abstractDataType.getUnsigned16());
            case 4:
                return new RegisterValue(obisCode, "Quality of service:" + getQualityOfServiceString(abstractDataType));
            case 5:
                return new RegisterValue(obisCode, "Network Selection Mode:" + (abstractDataType.getTypeEnum().getValue() == MANUAL ? " MANUAL" : " AUTOMATIC"));
            case 6:
                return new RegisterValue(obisCode, "Preferred operator list: " + getPreferredOperatorListString(abstractDataType));
            case 7:
                return new RegisterValue(obisCode, "Intl roaming allowed: " + abstractDataType.getBooleanObject().getState());
            case 8:
                return new RegisterValue(obisCode, "Minimum RSSI: " + abstractDataType.getUnsigned32());
            case 9:
                return new RegisterValue(obisCode, "Maximum BER: " + abstractDataType.getFloat32());
            case 10:
                return new RegisterValue(obisCode, "Network technology: " + getNetworkTechnologyString(abstractDataType));
            case 11:
                return new RegisterValue(obisCode, "Is GPRS preferred: " + abstractDataType.getBooleanObject().getState());
            default:
                throw new NoSuchRegisterException("GPRS Modem Setup attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    public String getPreferredOperatorListString(AbstractDataType gprsModemSetupAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (gprsModemSetupAttribute.isArray()) {
            int numberOfArrayEntries = gprsModemSetupAttribute.getArray().nrOfDataTypes();

            Array networkTechnologies = gprsModemSetupAttribute.getArray();
            for (int index = 0; index < numberOfArrayEntries; index++) {
                builder.append((index+1) + ". ");
                builder.append(networkTechnologies.getDataType(index).getOctetString().stringValue());
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct GPRS modem setup Network Technology attribute format.");
        }
    }

    public String getNetworkTechnologyString(AbstractDataType gprsModemSetupAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (gprsModemSetupAttribute.isArray()) {
            int numberOfArrayEntries = gprsModemSetupAttribute.getArray().nrOfDataTypes();

            Array networkTechnologies = gprsModemSetupAttribute.getArray();
            for (int index = 0; index < numberOfArrayEntries; index++) {
                builder.append(networkTechnologies.getDataType(index).getTypeEnum().toString());
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct GPRS modem setup Network Technology attribute format.");
        }
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

