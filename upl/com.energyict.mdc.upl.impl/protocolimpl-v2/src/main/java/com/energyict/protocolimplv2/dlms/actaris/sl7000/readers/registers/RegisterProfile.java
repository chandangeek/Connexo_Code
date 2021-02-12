package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RegisterProfile implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ObisCode profileGenericObisCode;
    private final ActarisSl7000 protocol;
    private Optional<ProfileGeneric> profile = Optional.empty();

    public RegisterProfile(CollectedRegisterBuilder collectedRegisterBuilder, ObisCode obisCode, ActarisSl7000 protocol) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.profileGenericObisCode = obisCode;
        this.protocol = protocol;
    }

    @Override
    public CollectedRegister read(ActarisSl7000 protocol, OfflineRegister readingSpecs) {
        try {
            if (profile.isPresent()) {
                DataContainer buffer = profile.get().getBuffer();
                int channelIndex = getChannel(readingSpecs.getObisCode());
                long value = buffer.getRoot().getStructure(0).getValue(channelIndex);
                Date now = Calendar.getInstance(protocol.getTimeZone()).getTime();
                ScalerUnit scalerUnit = new ScalerUnit(buffer.getRoot().getStructure(0).getStructure(channelIndex + 1).getInteger(0), buffer.getRoot().getStructure(0).getStructure(channelIndex + 1).getInteger(1));
                RegisterValue registerValue = new RegisterValue(readingSpecs, new Quantity(value, scalerUnit.getEisUnit()));
                registerValue.setTimes(now, null, now, now);
                return collectedRegisterBuilder.createCollectedRegister(readingSpecs, registerValue);
            } else {
                return collectedRegisterBuilder.createCollectedRegister(readingSpecs, ResultType.NotSupported, "Something went wrong while this reader returned isApplicable yet profile is not readable");
            }
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(readingSpecs, ResultType.DataIncomplete, e.getMessage());
        }
    }

    @Override
    public boolean isApplicable(ObisCode obisCode) {
        if (protocol.getDlmsSessionProperties().useRegisterProfile() && !this.profile.isPresent()) {
            try {
                this.profile = Optional.of(protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(profileGenericObisCode));
            } catch (NotInObjectListException e) {
            }
        }
        return getChannel(obisCode) != -1;
    }

    private int getChannel(ObisCode obisCode) {
        try {
            if (profile.isPresent()) {
                List<CapturedObject> captureObjects = profile.get().getCaptureObjects();
                int i = 0;
                for (CapturedObject each : captureObjects) {
                    if (each.getObisCode().equals(obisCode)) {
                        return i;
                    }
                    i++;
                }
            }
        } catch (IOException e) {
        }
        return -1;
    }

}
