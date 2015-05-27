package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.orm.DataModel;

/**
 * Created by igh on 22/05/2015.
 */
public class EmailDestinationImpl extends AbstractDataExportDestination implements EmailDestination {

    private String recipients;
    private String subject;
    private String attachmentName;

    EmailDestinationImpl(DataModel dataModel) {
        super(dataModel);
    }

    EmailDestinationImpl init(String recipients, String subject, String attachmentName) {
        this.recipients = recipients;
        this.subject = subject;
        this.attachmentName = attachmentName;
        return this;
    }
}
