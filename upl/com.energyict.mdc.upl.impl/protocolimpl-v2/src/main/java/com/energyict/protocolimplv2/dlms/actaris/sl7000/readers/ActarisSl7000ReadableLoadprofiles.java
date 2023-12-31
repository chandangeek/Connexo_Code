package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.loadprofile.ActarisSl7000LoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.GenericLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLoadProfileReader;

import java.util.ArrayList;
import java.util.List;

public class ActarisSl7000ReadableLoadprofiles {

    private final CollectedDataFactory collectedLoadProfileBuilder;
    private final IssueFactory issueFactory;
    private final int limitMaxNrOfDays;

    public ActarisSl7000ReadableLoadprofiles(CollectedDataFactory collectedLoadProfileBuilder, IssueFactory issueFactory, int limitMaxNrOfDays) {
        this.collectedLoadProfileBuilder = collectedLoadProfileBuilder;
        this.issueFactory = issueFactory;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
    }

    public CollectedLoadProfileReader<ActarisSl7000> getCollectedLogBookReader(ActarisSl7000 dlmsProtocol) {
        List<LoadProfileReader<ActarisSl7000>> loadProfileReaders = new ArrayList<>();
        loadProfileReaders.add(loadProfile(dlmsProtocol));
        return new CollectedLoadProfileReader<>(loadProfileReaders, dlmsProtocol, collectedLoadProfileBuilder, issueFactory);
    }

    private GenericLoadProfileReader<ActarisSl7000> loadProfile(AbstractDlmsProtocol dlmsProtocol) {
        return new ActarisSl7000LoadProfileReader(issueFactory, collectedLoadProfileBuilder, limitMaxNrOfDays, dlmsProtocol);
    }
}
