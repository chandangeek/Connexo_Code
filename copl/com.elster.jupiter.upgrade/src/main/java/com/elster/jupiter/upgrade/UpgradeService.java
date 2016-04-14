package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;

import java.util.List;

public interface UpgradeService {

    void register(String component, DataModel dataModel, Class<? extends Installer> installerClass, List<Class<? extends Upgrader>> upgraders);

}
