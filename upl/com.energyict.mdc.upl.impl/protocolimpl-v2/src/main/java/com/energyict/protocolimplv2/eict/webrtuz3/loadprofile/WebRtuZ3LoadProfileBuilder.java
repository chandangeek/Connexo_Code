package com.energyict.protocolimplv2.eict.webrtuz3.loadprofile;

import com.energyict.dlms.cosem.Clock;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimplv2.dlms.DLMSDefaultProfileIntervalStatusBits;
import com.energyict.protocolimplv2.eict.webrtuz3.WebRTUZ3;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/04/2015 - 14:33
 */
public final class WebRtuZ3LoadProfileBuilder extends LoadProfileBuilder<WebRTUZ3> {

    /**
     * Hardcoded ObisCode for the status of an Emeter profile
     */
    protected static final ObisCode EmeterStatusObisCode = ObisCode.fromString("0.0.96.10.1.255");
    /**
     * Hardcoded ObisCode for the status of an Mbus profile
     */
    protected static final ObisCode MbusMeterStatusObisCode = ObisCode.fromString("0.x.96.10.3.255");

    /**
     * Default constructor
     *
     * @param meterProtocol the {#meterProtocol}
     */
    public WebRtuZ3LoadProfileBuilder(WebRTUZ3 meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }


    protected ProfileIntervalStatusBits getIntervalStatusBits() {
        return new DLMSDefaultProfileIntervalStatusBits();
    }

    /**
     * Checks if the given ObisCode/Serialnumber combination is a valid profileChannel. Checks are done based on the the StatusObisCodes and ClockObisCode
     *
     * @param obisCode     the obiscode to check
     * @param serialNumber the serialNumber of the meter, related to the given obisCode
     * @return true if the obisCode is not a {@link Clock} object nor a Status object
     */
    protected boolean isDataObisCode(ObisCode obisCode, String serialNumber) {
        return !(Clock.isClockObisCode(obisCode) || isStatusObisCode(obisCode, serialNumber));
    }

    protected boolean isStatusObisCode(ObisCode obisCode, String serialNumber) {
        boolean isStatusObisCode = false;
        ObisCode testObisCode;

        if (obisCode.getB() != this.getMeterProtocol().getPhysicalAddressFromSerialNumber(serialNumber)) {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(EmeterStatusObisCode, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(MbusMeterStatusObisCode, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        return isStatusObisCode;
    }

    @Override
    protected ObisCode fixObisCodeForSlaveLoadProfile( ObisCode obisCode )
    {
        return obisCode;
    }
}
