package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailService;
import com.google.inject.AbstractModule;

public class MailModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MailService.class).to(MailServiceImpl.class);

    }
}
