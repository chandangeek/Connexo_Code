/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.util.Optional;

/**
 * Created by bvn on 5/4/16.
 */
public final class InboundEndPointConfigurationImpl extends EndPointConfigurationImpl implements InboundEndPointConfiguration {

    private Reference<Group> group = Reference.empty();

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String clientId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String clientSecret;

    @Inject
    public InboundEndPointConfigurationImpl(DataModel dataModel, Clock clock, TransactionService transactionService, WebServicesService webServicesService) {
        super(clock, dataModel, transactionService, webServicesService);
    }

    @Override
    public Optional<Group> getGroup() {
        return group.getOptional();
    }

    @Override
    public void setGroup(Group group) {
        this.group.set(group);
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public void setUrl(String url) {
        if (url != null && !url.startsWith("/")) {
            super.setUrl("/" + url);
        } else {
            super.setUrl(url);
        }
    }
}
