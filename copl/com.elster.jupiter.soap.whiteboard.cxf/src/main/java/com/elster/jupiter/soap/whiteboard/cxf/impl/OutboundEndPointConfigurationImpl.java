/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;

/**
 * Created by bvn on 5/4/16.
 */
@ValidCredentials(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
public final class OutboundEndPointConfigurationImpl extends EndPointConfigurationImpl implements OutboundEndPointConfiguration {

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String username;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String password;

    @Inject
    public OutboundEndPointConfigurationImpl(DataModel dataModel, Clock clock, TransactionService transactionService) {
        super(clock, dataModel, transactionService);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isInbound() {
        return false;
    }
}
