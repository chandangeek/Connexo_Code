package com.elster.jupiter.mail;

import java.nio.file.Path;

public interface MailMessageBuilder {

    OutboundMailMessage build();

    MailMessageBuilder addRecipient(MailAddress recipient);

    MailMessageBuilder withSubject(String subject);

    MailMessageBuilder withBody(String body);

    MailMessageBuilder withAttachment(Path path);

    MailMessageBuilder withReplyTo(MailAddress replyTo);
}
