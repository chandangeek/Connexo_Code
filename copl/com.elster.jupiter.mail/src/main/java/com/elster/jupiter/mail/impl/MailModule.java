/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailService;
import com.google.inject.AbstractModule;

public class MailModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MailService.class).to(MailServiceImpl.class);

    }
}
