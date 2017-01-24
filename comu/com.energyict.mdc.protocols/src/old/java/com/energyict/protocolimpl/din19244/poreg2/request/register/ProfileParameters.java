package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read out the profile data configuration.
 * The configuration sets which registers should be added to the profile data.
 *
 * Copyrights EnergyICT
 * Date: 4-mei-2011
 * Time: 13:23:17
 */
public class ProfileParameters extends AbstractRegister {

    public ProfileParameters(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    public ProfileParameters(Poreg poreg) {
        super(poreg, 0, 0, 4, 28);
    }

    private List<ProfileInfo> profileParameters;

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.ProfileParameters.getId();
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        profileParameters = new ArrayList<ProfileInfo>();
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());

        for (int i = 0; i < 4; i++) {
            ProfileInfo info = new ProfileInfo(values.subList(i * 28, (i * 28) + 28));
            profileParameters.add(info);
        }
    }

    public List<ProfileInfo> getProfileParameters() {
        return profileParameters;
    }
}