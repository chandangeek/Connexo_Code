package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FileUtils;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by igh on 22/05/2015.
 */
public class EmailDestinationImpl extends AbstractDataExportDestination implements EmailDestination {

    private String recipients;
    private String subject;
    private String attachmentName;
    private String attachmentExtension;

    @Inject
    EmailDestinationImpl(DataModel dataModel, Thesaurus thesaurus, DataExportService dataExportService, AppService appService) {
        super(dataModel, thesaurus, dataExportService, appService);
    }

    EmailDestinationImpl init(String recipients, String subject, String attachmentName, String attachmentExtension) {
        this.recipients = recipients;
        this.subject = subject;
        this.attachmentName = attachmentName;
        this.attachmentExtension = attachmentExtension;
        return this;
    }

    public void send(List<FormattedExportData> data) {
        FileUtils fileUtils = new FileUtils(this.getThesaurus(), this.getDataExportService(), this.getAppService());
        fileUtils.createTemporaryFile(data, attachmentName, attachmentExtension);
        //todo mail file
    }
}
