package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

import java.util.List;

public class WebRTUKPLoadProfileBuilder extends LoadProfileBuilder<WebRTUKP> {

    public WebRTUKPLoadProfileBuilder(WebRTUKP meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected int constructChannelMask(ObisCode obisCode, List<CapturedRegisterObject> registers) {
        int channelMask;

        if (isCombinedLoadProfile(obisCode, registers)) {
            channelMask = getCombinedLoadProfileChannelMask(registers);
        } else {
            channelMask = getLoadProfileChannelMask(registers);
        }
        return channelMask;
    }

    /* Combined LoadProfile for WebRtuKP with 1 slave on Channel 1,2,3 or 4
        Structure
            OctetString -   DateTime
            Unsigned16  -   Status master
            Unsigned32  -   Value master
            Unsigned32  -   Value master
            Unsigned32  -   Value master
            Unsigned32  -   Value master
            Unsigned16  -   Status slave
            Unsigned32  -   Value slave

            No idea how it looks like with more than 1 slave, most likely only 1 status for All M-bus Slaves
            These cases do not exist within Enexis
     */

    @Override
    protected int getCombinedLoadProfileChannelMask(List<CapturedRegisterObject> registers) {
        int channelMask = 0;
        int counter = 0;

        for (CapturedRegisterObject register : registers) {
            if (isValidMasterRegister(register) || isMbusRegister(register)){
                channelMask |= (int) Math.pow(2, counter);
            }
            counter++;
        }

        return channelMask;
    }
}
