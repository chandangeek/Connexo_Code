package test.com.energyict.protocolimplv2.coronis.muc;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveCard;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisParameterException;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStackException;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 4/03/13
 * Time: 11:20
 * Author: khe
 */
public class RegisterReader {

    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_RADIO_ACKNOWLEDGE = ObisCode.fromString("0.0.96.50.4.255");
    private static final ObisCode OBISCODE_RADIO_USER_TIMEOUT = ObisCode.fromString("0.0.96.50.12.255");
    private static final ObisCode OBISCODE_EXCHANGE_STATUS = ObisCode.fromString("0.0.96.50.14.255");
    private final WavenisStack wavenisStack;

    public RegisterReader(WavenisStack wavenisStack) {
        this.wavenisStack = wavenisStack;
    }

    public CollectedRegister readRegister(OfflineRegister register) throws IOException {
        ObisCode obisCode = register.getObisCode();
        CollectedRegister collectedRegister = createCollectedRegister(register);
        try {
            if (OBISCODE_FIRMWARE.equals(obisCode)) {
                collectedRegister.setCollectedData(WavenisStackUtils.readFirmwareVersion(wavenisStack));
            } else if (OBISCODE_RADIO_USER_TIMEOUT.equals(obisCode)) {
                int timeout = getWaveCard().getRadioUserTimeoutInSeconds();
                collectedRegister.setCollectedData(new Quantity(timeout, Unit.get(BaseUnit.SECOND)), "");
            } else if (OBISCODE_RADIO_ACKNOWLEDGE.equals(obisCode)) {
                Boolean radioAcknowledge = getWaveCard().isRadioAcknowledge();
                collectedRegister.setCollectedData(radioAcknowledge.toString());
            } else if (OBISCODE_EXCHANGE_STATUS.equals(obisCode)) {
                int exchangeStatus = getWaveCard().getExchangeStatus();
                collectedRegister.setCollectedData(new Quantity(exchangeStatus, Unit.get(BaseUnit.UNITLESS)), "");
            } else {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(obisCode, "Obiscode not supported by protocol", obisCode));
                return collectedRegister;
            }
        } catch (WavenisParameterException e) {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(obisCode, "Parameter not supported by Wavecard: " + e.getMessage(), obisCode));
            return collectedRegister;
        }
        collectedRegister.setReadTime(new Date());
        return collectedRegister;
    }

    private WaveCard getWaveCard() throws WavenisStackException {
        return wavenisStack.getWaveCard();
    }

    private CollectedRegister createCollectedRegister(OfflineRegister register) {
        return MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode()));
    }
}