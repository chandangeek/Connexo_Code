package com.elster.jupiter.mail;

public interface MailService {

    String COMPONENT_NAME = "MLS";

    MailMessageBuilder messageBuilder(MailAddress first, MailAddress... other);

    MailAddress mailAddress(String mailAddress);
}
