package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EndDeviceEventTypePartInfos {

    public int total = 0;
    public List<EndDeviceEventTypePartInfo> endDeviceEventTypePartInfos = new ArrayList<>();

    public EndDeviceEventTypePartInfos from(EndDeviceType[] endDeviceTypesArray, Thesaurus thesaurus) {
        for (EndDeviceType each : endDeviceTypesArray) {
            EndDeviceEventTypePartInfo endDeviceEventTypePartInfo = new EndDeviceEventTypePartInfo();
            endDeviceEventTypePartInfo.name = each.name();
            endDeviceEventTypePartInfo.mnemonic = each.getMnemonic();
            endDeviceEventTypePartInfo.value = each.getValue();
            endDeviceEventTypePartInfo.displayName = thesaurus.getString(each.name(), each.getMnemonic());
            endDeviceEventTypePartInfos.add(endDeviceEventTypePartInfo);
            total++;
        }
        return this;
    }

    public EndDeviceEventTypePartInfos from(EndDeviceDomain[] endDeviceDomainsArray, Thesaurus thesaurus) {
        for (EndDeviceDomain each : endDeviceDomainsArray) {
            EndDeviceEventTypePartInfo endDeviceEventTypePartInfo = new EndDeviceEventTypePartInfo();
            endDeviceEventTypePartInfo.name = each.name();
            endDeviceEventTypePartInfo.mnemonic = each.getMnemonic();
            endDeviceEventTypePartInfo.value = each.getValue();
            endDeviceEventTypePartInfo.displayName = thesaurus.getString(each.name(), each.getMnemonic());
            endDeviceEventTypePartInfos.add(endDeviceEventTypePartInfo);
            total++;
        }
        return this;
    }

    public EndDeviceEventTypePartInfos from(EndDeviceSubDomain[] endDeviceSubDomainsArray, Thesaurus thesaurus) {
        for (EndDeviceSubDomain each : endDeviceSubDomainsArray) {
            EndDeviceEventTypePartInfo endDeviceEventTypePartInfo = new EndDeviceEventTypePartInfo();
            endDeviceEventTypePartInfo.name = each.name();
            endDeviceEventTypePartInfo.mnemonic = each.getMnemonic();
            endDeviceEventTypePartInfo.value = each.getValue();
            endDeviceEventTypePartInfo.displayName = thesaurus.getString(each.name(), each.getMnemonic());
            endDeviceEventTypePartInfos.add(endDeviceEventTypePartInfo);
            total++;
        }
        return this;
    }

    public EndDeviceEventTypePartInfos from(EndDeviceEventorAction[] endDeviceEventOrActionArray, Thesaurus thesaurus) {
        for (EndDeviceEventorAction each : endDeviceEventOrActionArray) {
            EndDeviceEventTypePartInfo endDeviceEventTypePartInfo = new EndDeviceEventTypePartInfo();
            endDeviceEventTypePartInfo.name = each.name();
            endDeviceEventTypePartInfo.mnemonic = each.getMnemonic();
            endDeviceEventTypePartInfo.value = each.getValue();
            endDeviceEventTypePartInfo.displayName = thesaurus.getString(each.name(), each.getMnemonic());
            endDeviceEventTypePartInfos.add(endDeviceEventTypePartInfo);
            total++;
        }
        return this;
    }
}
