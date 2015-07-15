package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.ftpclient.FtpClientService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class FtpModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FtpClientService.class).to(FtpClientServiceImpl.class).in(Scopes.SINGLETON);

    }
}
