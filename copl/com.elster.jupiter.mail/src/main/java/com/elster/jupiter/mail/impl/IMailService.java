/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailService;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

public interface IMailService extends MailService {
    Address getFrom();

    MailSession getSession();

    interface MailSession {
        MimeMessage createMessage();

        void send(MimeMessage message);
    }
}
