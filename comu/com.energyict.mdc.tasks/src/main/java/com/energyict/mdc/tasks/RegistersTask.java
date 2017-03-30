/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.masterdata.RegisterGroup;

import java.util.Collection;
import java.util.List;

/**
 * Models the {@link com.energyict.mdc.tasks.ProtocolTask} which can read one or multiple registers from a Device.
 * <p>
 * The task can contain an optional list of {@link RegisterGroup rtuRegisterGroups},
 * which means only the registers in those groups (if defined on the Device) will be fetched
 * from the Device. If no list is provided, the <b>all</b> defined registers on the Device
 * will be fetched.
 * </p>
 *
 * @author gna
 * @since 19/04/12 - 13:57
 */
public interface RegistersTask extends ProtocolTask {

    /**
     * Return a list of {@link RegisterGroup}s which need to be fetched during this task.
     * If no groups are defined, then an empty list will be returned.
     *
     * @return the list of RtuRegisterGroups
     */
    public List<RegisterGroup> getRegisterGroups();
    void setRegisterGroups(Collection<RegisterGroup> registerGroups);

    interface RegistersTaskBuilder {
        public RegistersTaskBuilder registerGroups(Collection<RegisterGroup> registerGroups);
        public RegistersTask add();
    }
}