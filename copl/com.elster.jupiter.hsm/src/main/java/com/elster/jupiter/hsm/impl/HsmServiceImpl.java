/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.hsm", service = {HsmService.class}, immediate = true, property = "name=" + HsmService.COMPONENTNAME)
@SuppressWarnings("unused")
public class HsmServiceImpl implements HsmService {
    //private volatile UpgradeService upgradeService;

    @Activate
    public void activate(BundleContext context) {
       /* DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HsmService.class).toInstance(HsmServiceImpl.this);
            }
        });*/
        //upgradeService.register(InstallIdentifier.identifier("Pulse", "HSM"), dataModel, Installer.class, Collections.emptyMap());
    }
}
