/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.energyict.dlms.cosem.PPPSetup;

import java.io.IOException;


public class PPPSetupAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 6;

    public PPPSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return PPPSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final PPPSetup pppSetup = getCosemObjectFactory().getPPPSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, pppSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, PPPSetup pppSetup) throws IOException {
        switch (obisCode.getB()) {
            // Logical name
            case 1:
                return OctetString.fromObisCode(PPPSetup.getDefaultObisCode());
            case 2:
                return pppSetup.readPhyReference();
            case 3:
                return pppSetup.readLCPOptions();
            case 4:
                return pppSetup.readIPCPOptions();
            case 5:
                return pppSetup.readPPPAuthentication();
            case 6:
                return pppSetup.readPPPIdleTime();
            default:
                throw new NoSuchRegisterException("PPPSetup attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {
            // Logical name
            case 1:
                return new RegisterValue(obisCode, PPPSetup.getDefaultObisCode().toString());
            case 2:
                return new RegisterValue(obisCode, "Phy reference: " + getObisCode(abstractDataType));
            case 3:
                return new RegisterValue(obisCode, "LCP options:" + getLCPOptionsString(abstractDataType));
            case 4:
                return new RegisterValue(obisCode, "IPCP options:" + getIPCPOptionsString(abstractDataType));
            case 5:
                return new RegisterValue(obisCode, "PPP authentication:" + getPPPAuthenticationString(abstractDataType));
            case 6:
                return new RegisterValue(obisCode, "PPP idle time: " + abstractDataType.getUnsigned32());
            default:
                throw new NoSuchRegisterException("PPP Setup attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    public String getObisCode(AbstractDataType pppSetupAttribute) throws NumberFormatException {
        byte[] obisCodeBytes = pppSetupAttribute.getOctetString().toByteArray();
        ObisCode obisCode = ObisCode.fromByteArray(obisCodeBytes);
        return obisCode.toString();
    }

    public String getLCPOptionsString(AbstractDataType pppSetupAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (pppSetupAttribute.isArray()) {
            int numberOfArrayEntries = pppSetupAttribute.getArray().nrOfDataTypes();

            Array lcpOptions = pppSetupAttribute.getArray();
            for (int index = 0; index < numberOfArrayEntries; index++) {
                if (lcpOptions.getDataType(index).isStructure()) {
                    Structure lcpOption = lcpOptions.getDataType(index).getStructure();
                    builder.append(" LCP Type: " + lcpOption.getDataType(0).getUnsigned8().getValue());
                    builder.append(" length: " + lcpOption.getDataType(1).getUnsigned8().getValue());
                    builder.append(" data: " + lcpOption.getDataType(2).getUnsigned16().getValue());
                }
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct LCP Options attribute format.");
        }
    }

    public String getIPCPOptionsString(AbstractDataType pppSetupAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (pppSetupAttribute.isArray()) {
            int numberOfArrayEntries = pppSetupAttribute.getArray().nrOfDataTypes();

            Array lcpOptions = pppSetupAttribute.getArray();
            for (int index = 0; index < numberOfArrayEntries; index++) {
                if (lcpOptions.getDataType(index).isStructure()) {
                    Structure lcpOption = lcpOptions.getDataType(index).getStructure();
                    builder.append(" IPCP Type: " + lcpOption.getDataType(0).getUnsigned8().getValue());
                    builder.append(" length: " + lcpOption.getDataType(1).getUnsigned8().getValue());
                    builder.append(" data: " + lcpOption.getDataType(2).getUnsigned16().getValue());
                }
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct IPCP Options attribute format.");
        }
    }

    public String getPPPAuthenticationString(AbstractDataType pppSetupAttributes) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (pppSetupAttributes.isStructure() && pppSetupAttributes.getStructure().nrOfDataTypes() == 2) {
            Structure pppSpecs = pppSetupAttributes.getStructure();

            if (pppSpecs.getDataType(0).isOctetString()) {
                builder.append(" User name: ").append(pppSpecs.getDataType(0).getOctetString().stringValue());
            }

            if (pppSpecs.getDataType(1).isOctetString()) {
                builder.append(" User password: ").append(pppSpecs.getDataType(1).getOctetString().stringValue());
            } else if (pppSpecs.getDataType(1).isNumerical()) {
                int algorithmId = pppSpecs.getDataType(1).intValue();
                builder.append(" Algorithm Id: ");

                switch (algorithmId) {
                    case PPPSetup.PPPAuthenticationType.CHAP_MD5:
                        builder.append("value: " + algorithmId+" - CHAP_MD5");
                        break;
                    case PPPSetup.PPPAuthenticationType.CHAP_MS_CHAP2:
                        builder.append("value: " + algorithmId+" - CHAP_MS_CHAP2");
                        break;
                    case PPPSetup.PPPAuthenticationType.CHAP_MS_CHAP:
                        builder.append("value: " + algorithmId+" - CHAP_MS_CHAP");
                        break;
                    case PPPSetup.PPPAuthenticationType.CHAP_SHA_1:
                        builder.append("value: " + algorithmId+" - CHAP_SHA_1");
                        break;
                    default:
                }
                builder.append(" Algorithm Id: ").append(pppSpecs.getDataType(1).intValue());
            }

            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct PPP setup format.");
        }
    }
}


