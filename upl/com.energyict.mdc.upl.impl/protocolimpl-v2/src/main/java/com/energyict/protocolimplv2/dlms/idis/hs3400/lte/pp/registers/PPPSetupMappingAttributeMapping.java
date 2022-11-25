package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;

import java.io.IOException;

public class PPPSetupMappingAttributeMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 5;

    public PPPSetupMappingAttributeMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(ObisCode obisCode) {
        return PPPSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final PPPSetup pppSetup = getCosemObjectFactory().getPPPSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, pppSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, PPPSetup pppSetup) throws IOException {

        switch (obisCode.getE()) {
            case 1:
                return OctetString.fromObisCode(obisCode);
            case 2:
                return pppSetup.readPhyReference();
            case 3:
                return pppSetup.readLCPOptions();
            case 4:
                return pppSetup.readIPCPOptions();
            case 5:
                return pppSetup.readPPPAuthentication();
            default:
                throw new NoSuchRegisterException("PPPSetup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 1:
                return new RegisterValue(obisCode, "Logical name: " + obisCode);
            case 2:
                return new RegisterValue(obisCode, "Phy Reference: " +  obisCode);
            case 3:
                return new RegisterValue(obisCode, "LCP options:" + parseLCPOptionsString(abstractDataType));
            case 4:
                return new RegisterValue(obisCode, "IPCP Options: " + parseIPCPOptionsString(abstractDataType));
            case 5:
                return new RegisterValue(obisCode, "PPP authentication:" + parsePPPAuthentication(abstractDataType));
            default:
                throw new NoSuchRegisterException("PPPSetup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    public String parseIPCPOptionsString(AbstractDataType abstractDataType) throws IOException {

        StringBuffer builder = new StringBuffer();

        if (abstractDataType.isArray()) {

            int numberOfArrayEntries = abstractDataType.getArray().nrOfDataTypes();
            Array lcpOptions = abstractDataType.getArray();

            for (int i = 0; i < numberOfArrayEntries; i++) {
                if (lcpOptions.getDataType(i).isStructure()) {
                    Structure lcpOption = lcpOptions.getDataType(i).getStructure();
                    builder.append(" IPCP Type: " + lcpOption.getDataType(0).getUnsigned8().getValue());
                    builder.append(" Length: " + lcpOption.getDataType(1).getUnsigned8().getValue());
                    builder.append(" Data: " + lcpOption.getDataType(2).getUnsigned16().getValue());
                }
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct IPCP Options attribute format.");
        }
    }

    public String parsePPPAuthentication(AbstractDataType abstractDataType) throws IOException {

        StringBuffer builder = new StringBuffer();

        if (abstractDataType.isStructure() && abstractDataType.getStructure().nrOfDataTypes() == 2) {
            Structure pppSpecs = abstractDataType.getStructure();

            if (pppSpecs.getDataType(0).isOctetString()) {
                builder.append(" User name: ").append(pppSpecs.getDataType(0).getOctetString().stringValue());
            }

            if (pppSpecs.getDataType(1).isOctetString()) {
                builder.append(" User password: ").append(pppSpecs.getDataType(1).getOctetString().stringValue());
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct PPP setup format.");
        }
    }

    public String parseLCPOptionsString(AbstractDataType abstractDataType) throws IOException {

        StringBuffer builder = new StringBuffer();

        if (abstractDataType.isArray()) {

            int numberOfArrayEntries = abstractDataType.getArray().nrOfDataTypes();
            Array lcpOptions = abstractDataType.getArray();

            for (int i = 0; i < numberOfArrayEntries; i++) {
                if (lcpOptions.getDataType(i).isStructure()) {
                    Structure lcpOption = lcpOptions.getDataType(i).getStructure();
                    builder.append(" LCP Type: " + lcpOption.getDataType(0).getUnsigned8().getValue());
                    builder.append(" Length: " + lcpOption.getDataType(1).getUnsigned8().getValue());
                    builder.append(" Data: " + lcpOption.getDataType(2).getUnsigned16().getValue());
                }
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct LCP Options attribute format.");
        }
    }
}
