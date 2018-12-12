/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.callback;


import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface InstallService {
    void install();

    /**
     * When bundles register their InstallService, the container actually calls the install method.
     * But problem is here that these should be called in a particular order because installers/modules/bundles depend on each other.
     *
     * When you need to create a new InstallService, you change something to an existing InstallService or you change/create some
     * TableSpecs that have a foreign key to a table of a different module, you also need to modify the getPrerequisiteModules()
     * method. (So remark, you only need to indicate the modules on which the installer depends)
     *
     * To determine on which modules you depend
     * - you can look at all the @Reference services your installer gets injected
     * - have a look at all your TableSpecs and check all foreign key constraints to tables of different modules.
     *
     * @see {http://confluence.eict.vpdc/pages/viewpage.action?pageId=27699169}
     */
    public List<String> getPrerequisiteModules();
}
