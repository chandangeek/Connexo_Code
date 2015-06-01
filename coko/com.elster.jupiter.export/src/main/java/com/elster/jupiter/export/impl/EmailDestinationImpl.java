package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FileUtils;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class EmailDestinationImpl extends AbstractDataExportDestination implements EmailDestination {

    private final MailService mailService;

    private String recipients;
    private String subject;
    private String attachmentName;
    private String attachmentExtension;

    @Inject
    EmailDestinationImpl(DataModel dataModel, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem, MailService mailService) {
        super(dataModel, thesaurus, dataExportService, appService, fileSystem);
        this.mailService = mailService;
    }

    EmailDestinationImpl init(String recipients, String subject, String attachmentName, String attachmentExtension) {
        this.recipients = recipients;
        this.subject = subject;
        this.attachmentName = attachmentName;
        this.attachmentExtension = attachmentExtension;
        return this;
    }

    static EmailDestinationImpl from(DataModel dataModel, String recipients, String subject, String attachmentName, String attachmentExtension) {
        return dataModel.getInstance(EmailDestinationImpl.class).init(recipients, subject, attachmentName, attachmentExtension);
    }

    @Override
    public void send(List<FormattedExportData> data) {
        FileUtils fileUtils = new FileUtils(this.getFileSystem(), this.getThesaurus(), this.getDataExportService(), this.getAppService());
        Path file = fileUtils.createTemporaryFile(data, attachmentName, attachmentExtension);
        sendMail(file);
    }

    private void sendMail(Path file) {
        List<String> recipients = getRecipients();
        if (recipients.isEmpty()) {
            return;
        }
        MailAddress primary = mailService.mailAddress(recipients.get(0));
        MailMessageBuilder builder = mailService.messageBuilder(primary)
                .withSubject(subject)
                .withAttachment(file);
        recipients.stream()
                .skip(1)
                .map(mailService::mailAddress)
                .forEach(builder::addRecipient);
        builder.build().send();
    }


    private List<String> getRecipients() {
        return Arrays.asList(recipients.split(","));
    }
}
