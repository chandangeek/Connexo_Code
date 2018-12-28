/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.device.topology.State;

import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

public class DevicesStatusAndQuantity {

    public String status;
    public long quantity;

    public DevicesStatusAndQuantity(String status, long quantity) {
        this.status = status;
        this.quantity = quantity;
    }

    public String getStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case SUCCESSFUL:
                return thesaurus.getString(MessageSeeds.STATUS_SUCCESSFUL.getKey(), MessageSeeds.STATUS_SUCCESSFUL.getDefaultFormat());
            case FAILED:
                return thesaurus.getString(MessageSeeds.STATUS_FAILED.getKey(), MessageSeeds.STATUS_FAILED.getDefaultFormat());
            case REJECTED:
                return thesaurus.getString(MessageSeeds.STATUS_CONFIGURATION_ERROR.getKey(), MessageSeeds.STATUS_CONFIGURATION_ERROR.getDefaultFormat());
            case ONGOING:
                return thesaurus.getString(MessageSeeds.STATUS_ONGOING.getKey(), MessageSeeds.STATUS_ONGOING.getDefaultFormat());
            case PENDING:
                return thesaurus.getString(MessageSeeds.STATUS_PENDING.getKey(), MessageSeeds.STATUS_PENDING.getDefaultFormat());
            case CANCELLED:
                return thesaurus.getString(MessageSeeds.STATUS_CANCELED.getKey(), MessageSeeds.STATUS_CANCELED.getDefaultFormat());
        }
        return defaultState.getDisplayName(thesaurus);
    }

}
