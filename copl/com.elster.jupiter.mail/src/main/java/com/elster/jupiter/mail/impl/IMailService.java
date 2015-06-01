package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailService;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

public interface IMailService extends MailService {
    Address getFrom();

    MimeMessage createMessage();

    void send(MimeMessage message);
}
