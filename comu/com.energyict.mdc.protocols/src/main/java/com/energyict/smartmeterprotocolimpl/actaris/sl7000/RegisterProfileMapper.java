package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 17/08/12
 * Time: 13:56
 */
public class RegisterProfileMapper {

    final ObisCode ALLDEMANDS_PROFILE = ObisCode.fromString("0.0.98.133.5.255");
    final ObisCode ALLCUMULATIVEMAXDEMANDS_PROFILE = ObisCode.fromString("0.0.98.133.90.255");
    final ObisCode ALLENERGYRATES_PROFILE = ObisCode.fromString("255.255.98.133.1.255");
    final ObisCode ALLTOTALENERGIES_PROFILE = ObisCode.fromString("255.255.98.133.2.255");

    ProfileGeneric[] profileGenerics;

    private ActarisSl7000 meterProtocol;


    public RegisterProfileMapper(ActarisSl7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public RegisterValue getRegister(Register register) throws IOException {
        switch (getProfileGenericForRegister(register)) {
            case 0:
                return getRegister(register, 0);
            case 1:
                return getAllCumulativeMaximumDemandRegister(register);
            case 2:
                return getRegister(register, 2);
            case 3:
                return getRegister(register, 3);
            default:
                return null;
        }
    }

    private RegisterValue getRegister(Register register, int profileGenericIndex) throws IOException {
        DataContainer buffer = getProfileGenerics()[profileGenericIndex].getBuffer();
        int channelIndex = getChannelIndexForRegister(getProfileGenerics()[profileGenericIndex], register.getObisCode());

        long value = buffer.getRoot().getStructure(0).getStructure(channelIndex).getValue(1);
        ScalerUnit scalerUnit = new ScalerUnit(buffer.getRoot().getStructure(0).getStructure(channelIndex).getStructure(2).getInteger(0), buffer.getRoot().getStructure(0).getStructure(channelIndex).getStructure(2).getInteger(1));
        return new RegisterValue(register, new Quantity(value, scalerUnit.getEisUnit()));
    }

    private RegisterValue getAllCumulativeMaximumDemandRegister(Register register) throws IOException {
        DataContainer buffer = getProfileGenerics()[2].getBuffer();
        int channelIndex = getChannelIndexForRegister(getProfileGenerics()[2], register.getObisCode());

        long value = buffer.getRoot().getStructure(0).getValue(channelIndex);
        ScalerUnit scalerUnit = new ScalerUnit(buffer.getRoot().getStructure(0).getStructure(channelIndex + 1).getInteger(0), buffer.getRoot().getStructure(0).getStructure(channelIndex + 1).getInteger(1));
        return new RegisterValue(register, new Quantity(value, scalerUnit.getEisUnit()));
    }

    /**
     * Checks if the register is captured in any profile.
     *
     * @return the profileGenerics index
     *         -1 if the register is not captured in any of the profiles.
     */
    public int getProfileGenericForRegister(Register register) throws IOException {
        int i = 0;
        for (ProfileGeneric each : getProfileGenerics()) {
            if (getChannelIndexForRegister(each, register.getObisCode()) != -1) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Gets the CapturedObjects channel index for the register
     *
     * @param obisCode
     * @return the CapturedObjects channel index
     *         -1, if the obisCode is not present in the list of captured objects
     * @throws IOException
     */
    public int getChannelIndexForRegister(ProfileGeneric profile, ObisCode obisCode) throws IOException {
        List<CapturedObject> captureObjects = profile.getCaptureObjects();
        int i = 0;
        for (CapturedObject each : captureObjects) {
            if (each.getObisCode().equals(obisCode)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public ProfileGeneric[] getProfileGenerics() throws IOException {
        if (profileGenerics == null) {
            profileGenerics = new ProfileGeneric[4];
            profileGenerics[0] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(ALLDEMANDS_PROFILE);
            profileGenerics[1] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(ALLCUMULATIVEMAXDEMANDS_PROFILE);
            profileGenerics[2] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(ALLENERGYRATES_PROFILE);
            profileGenerics[3] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(ALLTOTALENERGIES_PROFILE);
        }
        return profileGenerics;
    }
}