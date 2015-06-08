package test.com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.PulseWeight;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.DailyConsumption;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.ExtendedIndexReading;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ObisCodeMapper implements DeviceRegisterSupport {

    private WaveFlow waveFlow;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveFlow the protocol
     */
    public ObisCodeMapper(final WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister register : registers) {
            result.add(readRegister(register));
        }
        return result;
    }

    private CollectedRegister readRegister(OfflineRegister register) {
        ObisCode obisCode = register.getObisCode();
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode()));

        if (isCurrentIndexReading(obisCode)) {
            int channel = obisCode.getB() - 1;
            if (channel > (waveFlow.getNumberOfChannels() - 1)) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(this, "Channel " + channel + " does not exist."));
            } else {
                PulseWeight pulseWeight = waveFlow.getPulseWeight(channel);
                BigDecimal currentIndexValue = new BigDecimal(pulseWeight.getWeight() * waveFlow.getRadioCommandFactory().readCurrentReading().getReadings()[channel]);
                collectedRegister.setCollectedData(new Quantity(currentIndexValue, pulseWeight.getUnit()));
            }
        } else if (isLastBillingPeriodIndexReadingForMonth(obisCode)) {
            int channel = obisCode.getB() - 1;
            PulseWeight pulseWeight = waveFlow.getPulseWeight(channel);
            ExtendedIndexReading extendedIndexReadingConfiguration = waveFlow.getRadioCommandFactory().readExtendedIndexConfiguration();
            if (channel > (waveFlow.getNumberOfChannels() - 1)) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(this, "No billing data available for this channel"));
            } else {
                int value = extendedIndexReadingConfiguration.getIndexOfLastMonth(channel);
                if (value == -1) {
                    collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(this, "No monthly billing data available yet"));
                } else {
                    BigDecimal lastMonthsIndexValue = new BigDecimal(pulseWeight.getWeight() * value);
                    Date toDate = extendedIndexReadingConfiguration.getDateOfLastMonthsEnd();
                    Calendar fromCal = Calendar.getInstance();
                    fromCal.setTime(toDate);
                    fromCal.setLenient(true);
                    fromCal.add(Calendar.MONTH, -1);

                    collectedRegister.setCollectedData(new Quantity(lastMonthsIndexValue, pulseWeight.getUnit()));
                    collectedRegister.setCollectedTimeStamps(new Date(), fromCal.getTime(), toDate);
                }
            }
        } else if (isLastBillingPeriodIndexReadingForDay(obisCode)) {
            int channel = obisCode.getB() - 1;
            PulseWeight pulseWeight = waveFlow.getPulseWeight(channel);
            DailyConsumption consumption = waveFlow.getRadioCommandFactory().readDailyConsumption();
            int value = consumption.getIndexZone().getDailyIndexOnPort(channel);
            if (value == -1) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(this, "No daily billing data available yet"));
            }
            BigDecimal lastMonthsIndexValue = new BigDecimal(pulseWeight.getWeight() * value);
            Date toDate = consumption.getIndexZone().getLastDailyLoggedIndex();
            Calendar fromCal = Calendar.getInstance();
            fromCal.setTime(toDate);
            fromCal.setLenient(true);
            fromCal.add(Calendar.HOUR_OF_DAY, -24);
            collectedRegister.setCollectedData(new Quantity(lastMonthsIndexValue, pulseWeight.getUnit()));
            collectedRegister.setCollectedTimeStamps(new Date(), fromCal.getTime(), toDate);
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.5.4.255"))) {
            if (waveFlow.getParameterFactory().readProfileType().supportsWaterValveControl()) {
                int status = waveFlow.getParameterFactory().readValveApplicationStatus();
                collectedRegister.setCollectedData(new Quantity(status, Unit.get("")));
            } else {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(this, "Module doesn't have valve support"));
            }
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.5.5.255"))) {
            if (waveFlow.getParameterFactory().readProfileType().supportsWaterValveControl()) {
                int status = waveFlow.getRadioCommandFactory().readValveStatus();
                collectedRegister.setCollectedData(new Quantity(status, Unit.get("")), ((status & 0x02) == 0x02) ? "Valve is closed" : "Valve is open");
            } else {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(this, "Module doesn't have valve support"));
            }
        } else {
            collectedRegister = waveFlow.getCommonObisCodeMapper().getRegisterValue(register);
        }
        collectedRegister.setReadTime(new Date());
        return collectedRegister;
    }

    /**
     * Checks if the obis code is of the form 1.b.82.8.0.f       (indicates an input pulse channel)
     * Where b = 1, 2, 3 or 4 and f = 0 or 255.
     *
     * @param obisCode the obis code
     * @return true or false
     */
    private boolean isInputPulseRegister(ObisCode obisCode) {
        return ((obisCode.getA() == 1) &&
                ((obisCode.getB() < 5) && (obisCode.getB()) > 0) &&
                (obisCode.getC() == 82) &&
                (obisCode.getD() == 8) &&
                (obisCode.getE() == 0));
    }

    // Index request for inputs A..D  (FieldA = 1 (electricity) because there's no pulse counter yet for water meters in the blue book...)
    private boolean isCurrentIndexReading(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 255);
    }

    // Billing data request for inputs A ... D
    private boolean isLastBillingPeriodIndexReadingForMonth(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 0);
    }

    private boolean isLastBillingPeriodIndexReadingForDay(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 1);
    }
}
