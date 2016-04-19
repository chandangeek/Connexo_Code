package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.Version;

public interface FullInstaller {

    /**
     * @return up to which version is installed
     */
    Version installs();

    void install();

}
