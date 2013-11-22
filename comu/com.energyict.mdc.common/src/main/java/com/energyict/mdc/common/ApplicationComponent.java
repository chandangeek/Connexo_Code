package com.energyict.mdc.common;

import java.util.List;

public interface ApplicationComponent extends FactoryFinder {

    String getLabelBundleName();

    String getErrorBundleName();

    String getCustomBundleName();

    String getErrorCodeBundleName();

    String getName();

    void setApplicationContext(ApplicationContext applicationContext);

    List<SystemParameterSpec> getSystemParameterSpecs();

    List<Upgrader> getUpgraders();

}