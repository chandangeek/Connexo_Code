package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class EmailDestinationImpl extends AbstractDataExportDestination implements EmailDestination {

    private class Sender {

        private final TagReplacerFactory tagReplacerFactory;

        private Sender(TagReplacerFactory tagReplacerFactory) {
            this.tagReplacerFactory = tagReplacerFactory;
        }

        private void send(Map<StructureMarker, Path> files) {
            sendMail(files.entrySet().stream()
                    .collect(Collectors.toMap(entry -> toFileName(entry.getKey()), Map.Entry::getValue)));
        }

        private void sendMail(Map<String, Path> files) {
            List<String> recipients = getRecipientsList();
            if (recipients.isEmpty()) {
                return;
            }
            MailAddress primary = mailService.mailAddress(recipients.get(0));
            MailMessageBuilder mailBuilder = mailService.messageBuilder(primary)
                    .withSubject(subject);

            files.forEach((fileName, path) -> mailBuilder.withAttachment(path, fileName));

            recipients.stream()
                    .skip(1)
                    .map(mailService::mailAddress)
                    .forEach(mailBuilder::addRecipient);
            mailBuilder.build().send();
        }

        private String toFileName(StructureMarker structureMarker) {
            return tagReplacerFactory.forMarker(structureMarker).replaceTags(attachmentName) + '.' + attachmentExtension;
        }


    }

    private final MailService mailService;

    private String recipients;
    private String subject;
    private String attachmentName;
    private String attachmentExtension;

    @Inject
    EmailDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem, MailService mailService) {
        super(dataModel, clock, thesaurus, dataExportService, appService, fileSystem);
        this.mailService = mailService;
    }

    EmailDestinationImpl init(IExportTask task, String recipients, String subject, String attachmentName, String attachmentExtension) {
        initTask(task);
        this.recipients = recipients;
        this.subject = subject;
        this.attachmentName = attachmentName;
        this.attachmentExtension = attachmentExtension;
        return this;
    }

    static EmailDestinationImpl from(IExportTask task, DataModel dataModel, String recipients, String subject, String attachmentName, String attachmentExtension) {
        return dataModel.getInstance(EmailDestinationImpl.class).init(task, recipients, subject, attachmentName, attachmentExtension);
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory) {
        new Sender(tagReplacerFactory).send(files);
    }

    @Override
    public String getRecipients() {
        return recipients;
    }

    @Override
    public String getFileName() {
        return attachmentName;
    }

    @Override
    public String getFileExtension() {
        return attachmentExtension;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    private List<String> getRecipientsList() {
        return Arrays.asList(recipients.split(","));
    }

    @Override
    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    @Override
    public void setAttachmentExtension(String attachmentExtension) {
        this.attachmentExtension = attachmentExtension;
    }
}
