/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.OutboundMailMessage;

import java.nio.file.Path;
import java.util.List;

public class MailMessageBuilderImpl implements MailMessageBuilder {

    private final OutboundMailMessageImpl constructing;

    public MailMessageBuilderImpl(MailServiceImpl mailService, List<MailAddress> recipients) {
        constructing = new OutboundMailMessageImpl(mailService);
        recipients.forEach(constructing::addRecipient);
    }

    @Override
    public OutboundMailMessage build() {
        return new OutboundMailMessageImpl(constructing);
    }

    @Override
    public MailMessageBuilder addRecipient(MailAddress recipient) {
        constructing.addRecipient(recipient);
        return this;
    }

    @Override
    public MailMessageBuilder withSubject(String subject) {
        constructing.setSubject(subject);
        return this;
    }

    @Override
    public MailMessageBuilder withBody(String body) {
        constructing.setBody(body);
        return this;
    }

    @Override
    public MailMessageBuilder withAttachment(Path path, String fileName) {
        constructing.addAttachment(path, fileName);
        return this;
    }

    @Override
    public MailMessageBuilder withReplyTo(MailAddress replyTo) {
        constructing.setReplyTo(replyTo);
        return this;
    }
}
