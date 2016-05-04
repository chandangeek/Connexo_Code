package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Version;

import java.util.Map;

public interface UpgradeService {

    void register(InstallIdentifier installIdentifier, DataModel dataModel, Class<? extends FullInstaller> installerClass, Map<Version, Class<? extends Upgrader>> upgraders);

    boolean isInstalled(InstallIdentifier installIdentifier, Version version);

    DataModel newNonOrmDataModel();
}
