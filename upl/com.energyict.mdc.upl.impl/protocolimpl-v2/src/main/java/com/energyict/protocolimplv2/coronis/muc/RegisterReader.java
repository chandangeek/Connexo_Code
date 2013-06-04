package com.energyict.protocolimplv2.coronis.muc;

import com.energyict.cbo.*;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.*;
import com.energyict.mdc.WavenisStackUtils;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 4/03/13
 * Time: 11:20
 * Author: khe
 */
public class RegisterReader {

    private final DeviceIdentifier deviceIdentifier;
    private final WavenisStack wavenisStack;

    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_RADIO_ACKNOWLEDGE = ObisCode.fromString("0.0.96.50.4.255");
    private static final ObisCode OBISCODE_RADIO_USER_TIMEOUT = ObisCode.fromString("0.0.96.50.12.255");
    private static final ObisCode OBISCODE_EXCHANGE_STATUS = ObisCode.fromString("0.0.96.50.14.255");

    public RegisterReader(WavenisStack wavenisStack, DeviceIdentifier deviceIdentifier) {
        this.wavenisStack = wavenisStack;
        this.deviceIdentifier = deviceIdentifier;
    }

    public CollectedRegister readRegister(ObisCode obisCode) throws IOException {
        CollectedRegister collectedRegister = createCollectedRegister(obisCode);
        try {
            if (OBISCODE_FIRMWARE.equals(obisCode)) {
                collectedRegister.setCollectedData(WavenisStackUtils.readFirmwareVersion(wavenisStack));
                return collectedRegister;
            } else if (OBISCODE_RADIO_USER_TIMEOUT.equals(obisCode)) {
                int timeout = getWaveCard().getRadioUserTimeoutInSeconds();
                collectedRegister.setCollectedData(new Quantity(timeout, Unit.get(BaseUnit.SECOND)), "");
                return collectedRegister;
            } else if (OBISCODE_RADIO_ACKNOWLEDGE.equals(obisCode)) {
                Boolean radioAcknowledge = getWaveCard().isRadioAcknowledge();
                collectedRegister.setCollectedData(radioAcknowledge.toString());
                return collectedRegister;
            } else if (OBISCODE_EXCHANGE_STATUS.equals(obisCode)) {
                int exchangeStatus = getWaveCard().getExchangeStatus();
                collectedRegister.setCollectedData(new Quantity(exchangeStatus, Unit.get(BaseUnit.UNITLESS)), "");
                return collectedRegister;
            }
        } catch (WavenisParameterException e) {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(obisCode, "Parameter not supported by Wavecard: " + e.getMessage(), obisCode));
            return collectedRegister;
        }
        collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(obisCode, "Obiscode not supported by protocol", obisCode));
        return collectedRegister;
    }

    private WaveCard getWaveCard() throws WavenisStackException {
        return wavenisStack.getWaveCard();
    }

    private CollectedRegister createCollectedRegister(ObisCode obisCode) {
        return MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(obisCode, deviceIdentifier));
    }
}