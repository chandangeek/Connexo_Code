/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail;

import java.nio.file.Path;

public interface MailMessageBuilder {

    OutboundMailMessage build();

    MailMessageBuilder addRecipient(MailAddress recipient);

    MailMessageBuilder withSubject(String subject);

    MailMessageBuilder withBody(String body);

    MailMessageBuilder withAttachment(Path path, String fileName);

    MailMessageBuilder withReplyTo(MailAddress replyTo);
}
