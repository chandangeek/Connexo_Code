/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo;

import com.elster.jupiter.demo.impl.DemoServiceImpl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class DemoModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(NlsService.class);
        requireBinding(EngineConfigurationService.class);
        requireBinding(TransactionService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(MasterDataService.class);
        requireBinding(MeteringService.class);
        requireBinding(TaskService.class);
        requireBinding(FirmwareService.class);

        bind(DemoServiceImpl.class).in(Scopes.SINGLETON);
        bind(FileSystem.class).toInstance(FileSystems.getDefault());
    }
}
