/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

/**
 * @author sva
 * @since 19/12/12 - 15:22
 */

public interface RegularLoadProfileCommand extends LoadProfileCommand {

    /**
     * @return the {@link ReadLoadProfileDataCommand}
     */
    public ReadLoadProfileDataCommand getReadLoadProfileDataCommand();

}
