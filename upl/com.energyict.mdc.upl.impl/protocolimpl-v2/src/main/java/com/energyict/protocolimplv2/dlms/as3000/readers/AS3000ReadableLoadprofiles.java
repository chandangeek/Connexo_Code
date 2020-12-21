package com.energyict.protocolimplv2.dlms.as3000.readers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.as3000.readers.loadprofile.AS3000BufferParser;
import com.energyict.protocolimplv2.dlms.as3000.readers.loadprofile.AS3000ChannelReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.GenericLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.buffer.SelectiveBufferReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLoadProfileReader;

import java.util.ArrayList;
import java.util.List;

public class AS3000ReadableLoadprofiles {

    private final CollectedDataFactory collectedLoadProfileBuilder;
    private final IssueFactory issueFactory;
    private final int limitMaxNrOfDays;

    public AS3000ReadableLoadprofiles(CollectedDataFactory collectedLoadProfileBuilder, IssueFactory issueFactory, int limitMaxNrOfDays) {
        this.collectedLoadProfileBuilder = collectedLoadProfileBuilder;
        this.issueFactory = issueFactory;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
    }

    public CollectedLoadProfileReader getCollectedLogBookReader(AbstractDlmsProtocol dlmsProtocol) {
        List<LoadProfileReader> loadProfileReaders = new ArrayList<>();
        loadProfileReaders.add(loadProfile());
        loadProfileReaders.add(instrumentationProfile());
        return new CollectedLoadProfileReader(loadProfileReaders, dlmsProtocol, collectedLoadProfileBuilder, issueFactory);
    }

    private GenericLoadProfileReader loadProfile() {
        return new GenericLoadProfileReader(new ObisCodeMatcher(ObisCode.fromString("1.1.99.1.0.255")), issueFactory, collectedLoadProfileBuilder,
                new AS3000ChannelReader(collectedLoadProfileBuilder), new SelectiveBufferReader(limitMaxNrOfDays), new AS3000BufferParser());
    }

    private GenericLoadProfileReader instrumentationProfile() {
        return new GenericLoadProfileReader(new ObisCodeMatcher(ObisCode.fromString("1.1.99.2.0.255")), issueFactory, collectedLoadProfileBuilder,
                new AS3000ChannelReader(collectedLoadProfileBuilder), new SelectiveBufferReader(limitMaxNrOfDays), new AS3000BufferParser());
    }

}
