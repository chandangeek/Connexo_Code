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
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 26/11/14
 * Time: 15:19
 */
public class MaximumDemandRegisterProfileMapper {

    final ObisCode MAXIMUM_DEMAND_1_PROFILE = ObisCode.fromString("0.0.98.133.61.255");
    final ObisCode MAXIMUM_DEMAND_2_PROFILE = ObisCode.fromString("0.0.98.133.62.255");
    final ObisCode MAXIMUM_DEMAND_3_PROFILE = ObisCode.fromString("0.0.98.133.63.255");
    final ObisCode MAXIMUM_DEMAND_4_PROFILE = ObisCode.fromString("0.0.98.133.64.255");

    ProfileGeneric[] profileGenerics;

    private ActarisSl7000 meterProtocol;


    public MaximumDemandRegisterProfileMapper(ActarisSl7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public RegisterValue getRegister(Register register) throws IOException {
        return getAllMaximumDemandRegister(register);
    }

    private RegisterValue getAllMaximumDemandRegister(Register register) throws IOException {
        ProfileGeneric profileGeneric = getProfileGenerics()[getProfileGenericIndexForRegister(register)];
        DataContainer buffer = profileGeneric.getBuffer();

        long value = buffer.getRoot().getStructure(0).getValue(0);
        ScalerUnit scalerUnit = new ScalerUnit(buffer.getRoot().getStructure(0).getStructure(1).getInteger(0), buffer.getRoot().getStructure(0).getStructure(1).getInteger(1));
        Date date = buffer.getRoot().getStructure(0).getOctetString(2).toDate(meterProtocol.getTimeZone());
        return new RegisterValue(register, new Quantity(value, scalerUnit.getEisUnit()), date);
    }

    /**
     * Checks if the register is captured in any profile.
     *
     * @return the profileGenerics index
     *         -1 if the register is not captured in any of the profiles.
     */
    private int getProfileGenericIndexForRegister(Register register) throws IOException {
        int i = 0;
        for (ProfileGeneric each : getProfileGenerics()) {
            if (profileContainsRegister(each, register.getObisCode())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Checks if the register is captured in any profile.
     *
     * @return the profileGeneric ObisCode
     *         null if the register is not captured in any of the profiles.
     */
    public ObisCode getProfileGenericForRegister(Register register) throws IOException {
        return getProfileGenericForRegister(register.getObisCode());
    }

    /**
     * Checks if the register is captured in any profile.
     *
     * @return the profileGeneric ObisCode
     *         null if the register is not captured in any of the profiles.
     */
    public ObisCode getProfileGenericForRegister(ObisCode register) throws IOException {
        for (ProfileGeneric each : getProfileGenerics()) {
            if (profileContainsRegister(each, register)) {
                return each.getObisCode();
            }
        }
        return null;
    }

    /**
     * Check if the register is mentioned in the CapturedObjects of the profile
     *
     * @return true if the register is mentioned in the CapturedObjects of the profile
     * @throws java.io.IOException
     */
    public boolean profileContainsRegister(ProfileGeneric profile, ObisCode obisCode) throws IOException {
        List<CapturedObject> captureObjects = profile.getCaptureObjects();
        for (CapturedObject each : captureObjects) {
            if (each.getObisCode().equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    public ProfileGeneric[] getProfileGenerics() throws IOException {
        if (profileGenerics == null) {
            profileGenerics = new ProfileGeneric[4];
            profileGenerics[0] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(MAXIMUM_DEMAND_1_PROFILE);
            profileGenerics[1] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(MAXIMUM_DEMAND_2_PROFILE);
            profileGenerics[2] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(MAXIMUM_DEMAND_3_PROFILE);
            profileGenerics[3] = meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(MAXIMUM_DEMAND_4_PROFILE);
        }
        return profileGenerics;
    }
}
