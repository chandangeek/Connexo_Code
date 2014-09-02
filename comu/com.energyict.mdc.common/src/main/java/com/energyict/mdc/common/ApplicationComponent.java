package com.energyict.mdc.common;

public interface ApplicationComponent {

    String getLabelBundleName();

    String getErrorBundleName();

    String getCustomBundleName();

    String getErrorCodeBundleName();

    String getName();

    void setApplicationContext(ApplicationContext applicationContext);

}