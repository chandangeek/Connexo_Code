/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.tasks.TaskServiceKeys;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.tasks.TopologyAction;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation for a {@link TopologyTask}.
 *
 * @author gna
 * @since 25/04/12 - 9:05
 */
class TopologyTaskImpl extends ProtocolTaskImpl implements TopologyTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(SLAVE_DEVICES_FLAG);

    enum Fields {
        TOPOLOGY_ACTION("topologyAction");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @NotNull(groups = { Save.Create.class, Save.Update.class}, message = "{"+ TaskServiceKeys.CAN_NOT_BE_EMPTY +"}")
    private TopologyAction topologyAction;

    public TopologyTaskImpl() {
        super();
        setFlags(FLAGS);
    }

    @Inject
    public TopologyTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    void deleteDependents() {
        // currently no dependents to delete
    }

    @Override
    public TopologyAction getTopologyAction() {
        return this.topologyAction;
    }

    @Override
    public void setTopologyAction(TopologyAction topologyAction) {
        this.topologyAction = topologyAction;
    }
}