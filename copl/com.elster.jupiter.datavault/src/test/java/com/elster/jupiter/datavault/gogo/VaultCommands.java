/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.gogo;

import com.elster.jupiter.datavault.DataVaultService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.playground.vault",
        service = VaultCommands.class,
        property = {"osgi.command.scope=vault", "osgi.command.function=encrypt", "osgi.command.function=decrypt"},
        immediate = true)
public class VaultCommands {

    private volatile DataVaultService dataVaultService;

    @Activate
    public void activate(BundleContext context) {
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    public String encrypt(String string) {
        return dataVaultService.encrypt(string.getBytes());
    }

    public String decrypt(String string) {
        return new String(dataVaultService.decrypt(string));
    }


}
