/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail;

public interface MailService {

    String COMPONENT_NAME = "MLS";

    MailMessageBuilder messageBuilder(MailAddress first, MailAddress... other);

    MailAddress mailAddress(String mailAddress);
}
