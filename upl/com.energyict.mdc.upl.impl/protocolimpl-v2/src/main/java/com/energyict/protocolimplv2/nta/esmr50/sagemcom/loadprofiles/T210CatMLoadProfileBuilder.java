package com.energyict.protocolimplv2.nta.esmr50.sagemcom.loadprofiles;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.LTEMonitoringProfileVersion1;
import com.energyict.protocolimplv2.nta.esmr50.sagemcom.T210CatM;

import java.util.List;

public final class T210CatMLoadProfileBuilder extends ESMR50LoadProfileBuilder<T210CatM> {
    /**
     * Default constructor
     *
     * @param meterProtocol
     * @param collectedDataFactory
     * @param issueFactory
     */
    public T210CatMLoadProfileBuilder(T210CatM meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected List<ChannelInfo> getLTEMonitoringChannelInfos(CollectedLoadProfileConfiguration lpc) {
        return LTEMonitoringProfileVersion1.getLTEMonitoringChannelInfos(lpc);
    }
}
