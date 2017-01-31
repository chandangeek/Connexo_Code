/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.mail.OutboundMailMessage;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class EmailDestinationImpl extends AbstractDataExportDestination implements EmailDestination {

    private class Sender {

        private final TagReplacerFactory tagReplacerFactory;
        private final Logger logger;
        private final Thesaurus thesaurus;

        private Sender(TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
            this.tagReplacerFactory = tagReplacerFactory;
            this.logger = logger;
            this.thesaurus = thesaurus;
        }

        private void send(Map<StructureMarker, Path> files) {
            if (files.isEmpty()) {
                return;
            }
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(javax.mail.Session.class.getClassLoader());

                sendMail(files.entrySet().stream()
                        .collect(Collectors.toMap(entry -> toFileName(entry.getKey()), Map.Entry::getValue)));

            } finally {
                Thread.currentThread().setContextClassLoader(tcl);
            }
        }

        private void sendMail(Map<String, Path> files) {
            String fileNames = "";
            Object[] fileNamesArray = files.keySet().toArray();
            for (int i = 0; i < fileNamesArray.length; i++) {
                fileNames = fileNames + fileNamesArray[i].toString();
                if (i < (fileNamesArray.length - 1)) {
                    fileNames = fileNames + ",";
                }
            }
            try {
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
            } catch (Exception e) {
                String errorMessage = e.getLocalizedMessage();
                if (Checks.is(errorMessage).emptyOrOnlyWhiteSpace()){
                    errorMessage = e.toString();
                }
                throw new DestinationFailedException(
                        thesaurus, MessageSeeds.MAIL_DESTINATION_FAILED, e, EmailDestinationImpl.this.recipients, errorMessage);
            }
            try (TransactionContext context = getTransactionService().getContext()) {
                MessageSeeds.DATA_MAILED_TO.log(logger, thesaurus, EmailDestinationImpl.this.recipients, fileNames);
                context.commit();
            }

        }

        private String toFileName(StructureMarker structureMarker) {
            return tagReplacerFactory.forMarker(structureMarker).replaceTags(attachmentName) + '.' + attachmentExtension;
        }


    }

    private final MailService mailService;

    private String recipients;
    private String subject;
    @ValidFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    private String attachmentName;
    @ValidFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    private String attachmentExtension;

    @Inject
    EmailDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, DataExportService dataExportService, AppService appService, FileSystem fileSystem, MailService mailService, TransactionService transactionService) {
        super(dataModel, clock, thesaurus, dataExportService, fileSystem, transactionService);
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
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
        new Sender(tagReplacerFactory, logger, thesaurus).send(files);
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
        return Arrays.asList(recipients.split(";"));
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
