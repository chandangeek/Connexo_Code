package com.energyict.protocolimplv2.dlms.common.obis.readers.logbook;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.protocol.LogBookReader;

public class CollectedLogBookBuilder {

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public CollectedLogBookBuilder(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    public CollectedLogBook createLogBook(LogBookIdentifier logBookIdentifier) {
        return collectedDataFactory.createCollectedLogBook(logBookIdentifier);
    }

    public CollectedLogBook createLogBook(LogBookReader lbr, ResultType resultType,  String message) {
        CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(lbr.getLogBookIdentifier());
        collectedLogBook.setFailureInformation(resultType, issueFactory.createWarning(lbr, message, lbr.getLogBookObisCode().toString()));
        return collectedLogBook;
    }
}
