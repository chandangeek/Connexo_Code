/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailException;
import com.elster.jupiter.mail.OutboundMailMessage;
import com.elster.jupiter.util.Pair;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OutboundMailMessageImpl implements OutboundMailMessage {
    private final IMailService mailService;
    private String subject;
    private String body;
    private List<MailAddress> recipients;
    private List<Pair<String, Path>> attachments;
    private MailAddress replyTo;

    OutboundMailMessageImpl(OutboundMailMessageImpl origin) {
        this.mailService = Objects.requireNonNull(origin.mailService);
        subject = origin.subject;
        body = origin.body;
        recipients = new ArrayList<>(origin.recipients);
        attachments = new ArrayList<>(origin.attachments);
        replyTo = origin.replyTo;
    }

    OutboundMailMessageImpl(IMailService mailService) {
        this.mailService = Objects.requireNonNull(mailService);
        recipients = new ArrayList<>();
        attachments = new ArrayList<>();
    }

    @Override
    public void send() {
        IMailService.MailSession session = mailService.getSession();
        session.send(toMessage(session));
    }

    MimeMessage toMessage(IMailService.MailSession session) {
        try {
            return tryToMessage(session);
        } catch (MessagingException e) {
            // TODO
            throw new MailException(null, null, e);
        }
    }

    private MimeMessage tryToMessage(IMailService.MailSession session) throws MessagingException {
        MimeMessage message = session.createMessage();
        message.setFrom(mailService.getFrom());
        if (replyTo != null) {
            message.setReplyTo(new Address[]{replyTo.asAddress()});
        }
        message.setSubject(subject);
        recipients.stream()
                .map(MailAddress::asAddress)
                .forEach(internetAddress -> addTo(message, internetAddress));
        Multipart multipart = new MimeMultipart();
        if (body != null) {
            MimeBodyPart bodyText = new MimeBodyPart();
            bodyText.setText(body);
            multipart.addBodyPart(bodyText);
        }
        attachments.stream()
                .map(this::toMimeBodyPart)
                .forEach(mimeBodyPart -> addToMultiPart(multipart, mimeBodyPart));
        message.setContent(multipart);
        return message;
    }

    private void addToMultiPart(Multipart multipart, BodyPart bodyPart) {
        try {
            multipart.addBodyPart(bodyPart);
        } catch (MessagingException e) {
            // TODO
            throw new MailException(null, null, e);
        }
    }

    private MimeBodyPart toMimeBodyPart(Pair<String, Path> attachment) {
        try {
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new PathDataSource(attachment.getLast());
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachment.getFirst());
            return messageBodyPart;
        } catch (MessagingException e) {
            // TODO
            throw new MailException(null, null, e);
        }
    }

    private void addTo(MimeMessage message, Address recipient) {
        try {
            message.addRecipient(Message.RecipientType.TO, recipient);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    void addRecipient(MailAddress recipient) {
        recipients.add(recipient);
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    void setBody(String body) {
        this.body = body;
    }

    void addAttachment(Path path, String fileName) {
        attachments.add(Pair.of(fileName, path));
    }

    public void setReplyTo(MailAddress replyTo) {
        this.replyTo = replyTo;
    }
}
