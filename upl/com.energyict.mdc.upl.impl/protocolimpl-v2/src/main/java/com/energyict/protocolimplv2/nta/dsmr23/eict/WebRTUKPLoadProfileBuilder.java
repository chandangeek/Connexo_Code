package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

/**
 * The WebRTU is misusing the registers in the LoadProfile. They don' use the ones that
 * are defined by DSMR, but created others to get more precision in the data.
 * By doing this, we actually need to create a different LoadProfileType in EIServer.
 * But they want to prevent this so we make a hack in our protocol.
 * If we have CapturedObject definitions with a B-field set to <i>128</i>,
 * then we map it to 0
 * <p/>
 * Copyrights EnergyICT
 * Date: 30/11/12
 * Time: 12:08
 */
//TODO remove WebRTUKPLoadProfileBuilder, method createCapturedObjectRegisterList was the only one overriden but the code was identical to the method from LoadProfileBuilder
public class WebRTUKPLoadProfileBuilder extends LoadProfileBuilder {

    public static final ObisCode MBUS_OBISCODE = ObisCode.fromString("0.x.24.2.1.255");

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public WebRTUKPLoadProfileBuilder(AbstractDlmsProtocol meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    private ObisCode getCorrectedChannelInfoObisCode(CapturedObject co) {
        ObisCode obisCode = co.getLogicalName().getObisCode();
        if (obisCode.getB() == 128) {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0x00);
        }
        return obisCode;
    }
}
