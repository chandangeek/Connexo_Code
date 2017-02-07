/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade;

import aQute.bnd.annotation.ConsumerType;

import java.util.Set;

@ConsumerType
public interface UpgradeCheckList {

    String application();

    Set<InstallIdentifier> componentsToInstall();
}
