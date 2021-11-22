package com.energyict.protocolimplv2.dlms.idis.aec.registers;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTimeOctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.common.composedobjects.ComposedObject;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AECRegisterFactory extends AM130RegisterFactory {
    private static final int MAXIMUM_DEMAND_REGISTER_D = 6;
    public AECRegisterFactory(AM130 am130, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(am130, collectedDataFactory, issueFactory);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<OfflineRegister> copyOfOfflineRegisters = new ArrayList<>();
        copyOfOfflineRegisters.addAll( offlineRegisters );

        return super.readRegisters(copyOfOfflineRegisters);
    }

    @Override
    protected CollectedRegister getCollectedRegisterForComposedObject(ComposedObject composedObject, OfflineRegister offlineRegister, ComposedCosemObject composedCosemObject) throws IOException {
        ComposedRegister composedRegister = ((ComposedRegister) composedObject);
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        Issue scalerUnitIssue = null;
        if (composedRegister.getRegisterUnitAttribute() != null &&
                composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute()).getStructure().getDataType(1) != null) {
            ScalerUnit scalerUnit = null;
            try {
                scalerUnit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute()));
                unit = scalerUnit.getEisUnit();
            } catch (Exception e) {
                String errorStr = "Unable to resolve the unit code value: " + scalerUnit.getUnitCode();
                scalerUnitIssue = getIssueFactory().createProblem(offlineRegister.getObisCode(), "registerXissue: " + errorStr, offlineRegister.getObisCode(),
                        errorStr);
            }
        }
        Date captureTime = null;
        Issue timeZoneIssue = null;
        boolean emptyDate = true;
        boolean maximumDemandRegister = offlineRegister.getObisCode().getD() == MAXIMUM_DEMAND_REGISTER_D;
        String timezoneError = null;

        if (composedRegister.getRegisterCaptureTime() != null) {
            AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());

            if (maximumDemandRegister) {
                IntBuffer intBuf = ByteBuffer.wrap(captureTimeOctetString.getOctetString().getOctetStr())
                                        .order(ByteOrder.BIG_ENDIAN)
                                        .asIntBuffer();
                int[] dateArray = new int[intBuf.remaining()];
                intBuf.get(dateArray);

                emptyDate = Arrays.stream(dateArray).allMatch(val -> val == 0);
            }

            TimeZone configuredTimeZone = getMeterProtocol().getDlmsSession().getTimeZone();
            DateTimeOctetString dlmsDateTimeOctetString = captureTimeOctetString.getOctetString().getDateTime(configuredTimeZone);

            captureTime = dlmsDateTimeOctetString.getValue().getTime();

            // Add the raw offset.
            int offsetAtCaptureTime = configuredTimeZone.getRawOffset();

            // If DST is active at capture time, add DST savings too.
            if (configuredTimeZone.inDaylightTime(captureTime)) {
                offsetAtCaptureTime += configuredTimeZone.getDSTSavings();
            }

            int configuredTimeZoneOffset = offsetAtCaptureTime / (-1 * 60 * 1000);
            if (dlmsDateTimeOctetString.getDeviation() != configuredTimeZoneOffset) {
                timezoneError = "Time zone offset reported by the meter [" + dlmsDateTimeOctetString.getDeviation() + "] " +
                        (dlmsDateTimeOctetString.isDST() ? " (in DST) " : "") +
                        "differs from the time zone configured in HES [" + configuredTimeZone.getID() +
                        "] = [" + configuredTimeZoneOffset + "] for register: " + offlineRegister.getObisCode().toString();
                if (!maximumDemandRegister) {
                    timeZoneIssue = getIssueFactory().createWarning(offlineRegister.getObisCode(), "registerXissue: " + timezoneError, offlineRegister.getObisCode(), timezoneError);
                }
            }
        }

        AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

        if(maximumDemandRegister) {
            if (emptyDate) {
                if (attributeValue.isNumerical() && !attributeValue.isNullData() && attributeValue.intValue() != 0) {
                    String errorStr = "The received capture date is invalid";
                    timeZoneIssue = getIssueFactory().createWarning(offlineRegister.getObisCode(), "registerXissue: " + errorStr, offlineRegister.getObisCode(), errorStr);
                }
            } else {
                if (timezoneError != null) {
                    timeZoneIssue = getIssueFactory().createWarning(offlineRegister.getObisCode(), "registerXissue: " + timezoneError, offlineRegister.getObisCode(), timezoneError);
                }
            }
        }

        RegisterValue registerValue = getRegisterValueForComposedRegister(offlineRegister, captureTime, attributeValue, unit);

        CollectedRegister collectedRegister = createCollectedRegister(registerValue, offlineRegister);

        if (timeZoneIssue != null) {
            collectedRegister.setFailureInformation(ResultType.ConfigurationError, timeZoneIssue);
        }
        if (scalerUnitIssue != null) {
            collectedRegister.setFailureInformation(ResultType.DataIncomplete, scalerUnitIssue);
        }

        return collectedRegister;
    }
}
