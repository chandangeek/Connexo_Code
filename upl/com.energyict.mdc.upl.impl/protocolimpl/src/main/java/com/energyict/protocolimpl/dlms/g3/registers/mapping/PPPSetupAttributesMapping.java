package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;

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
                return new RegisterValue(obisCode, "Phy reference: " + abstractDataType.getOctetString().stringValue());
            case 3:
                return new RegisterValue(obisCode, "LCP options: " + getOptionsString(abstractDataType));
            case 4:
                return new RegisterValue(obisCode, "IPCP options:" + getOptionsString(abstractDataType));
            case 5:
                return new RegisterValue(obisCode, "PPP authentication:" + getPPPAuthenticationString(abstractDataType));
            case 6:
                return new RegisterValue(obisCode, "PPP idle time: " + abstractDataType.getUnsigned32());
            default:
                throw new NoSuchRegisterException("PPP Setup attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    public String getOptionsString(AbstractDataType pppSetupAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (pppSetupAttribute.isArray()) {
            int numberOfArrayEntries = pppSetupAttribute.getArray().nrOfDataTypes();

            Array lcpOptions = pppSetupAttribute.getArray();
            for (int index = 0; index < numberOfArrayEntries; index++) {
                builder.append((index + 1) + ". ");
                builder.append(lcpOptions.getDataType(index).getOctetString().stringValue());
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct Options attribute format.");
        }
    }

    public String getPPPAuthenticationString(AbstractDataType pppSetupAttributes) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (pppSetupAttributes.isStructure() && pppSetupAttributes.getStructure().nrOfDataTypes() == 2) {
                /*Structure pppSpecs = pppSetupAttributes.getStructure().getDataType(0).getStructure();*/
            final PPPSetup pppSetup = getCosemObjectFactory().getPPPSetup(PPPSetup.getDefaultObisCode());
            PPPSetup.PPPAuthenticationType pppSet = pppSetup.readPPPAuthenticationType();

            if (pppSet.getAlgorithmId() != null) {
                builder.append("Algorithm Id: ").append(pppSet.getAlgorithmId());
            }

            if (pppSet.getUsername() != null) {
                builder.append(" User name: ").append(pppSet.getUsername().stringValue());
            }

            if (pppSet.getPassword() != null) {
                builder.append(" User password: ").append(pppSet.getPassword().stringValue());
            }

            if (pppSet.getOneTimePassword() != null) {
                builder.append(" One time password: ").append(pppSet.getOneTimePassword().getState());
            }

            if (pppSet.getGenericTokenCard() != null) {
                builder.append(" Generic token card: ").append(pppSet.getGenericTokenCard().getState());
            }

            if (pppSet.getMd5Challange() != null) {
                builder.append(" MD5 challenge: ").append(pppSet.getMd5Challange().getState());
            }

            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct PPP setup format.");
        }
    }
}


