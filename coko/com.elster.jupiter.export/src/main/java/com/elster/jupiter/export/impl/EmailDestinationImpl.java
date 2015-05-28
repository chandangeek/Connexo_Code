package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FileUtils;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by igh on 22/05/2015.
 */
public class EmailDestinationImpl extends AbstractDataExportDestination implements EmailDestination {

    private String recipients;
    private String subject;
    private String attachmentName;
    private String attachmentExtension;

    //todo where to get these properties from?
    private String smtpHost = "";
    private String from = "";

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
        Path file = fileUtils.createTemporaryFile(data, attachmentName, attachmentExtension);
        sendMail(file);
    }

    private void sendMail(Path file) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", smtpHost);
        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            for (String recipient : getRecipients()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            message.setSubject(subject);

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(file.toFile());
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(file.toFile().getName());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new FatalDataExportException(new RuntimeException(e));
        }
    }

    private List<String> getRecipients() {
        return Arrays.asList(recipients.split(","));
    }
}
