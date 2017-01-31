/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Version;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

@ProviderType
public interface UpgradeService {

    void register(InstallIdentifier installIdentifier, DataModel dataModel, Class<? extends FullInstaller> installerClass, Map<Version, Class<? extends Upgrader>> upgraders);

    boolean isInstalled(InstallIdentifier installIdentifier, Version version);

    void addStartupFinishedListener(StartupFinishedListener startupFinishedListener);

    DataModel newNonOrmDataModel();

}