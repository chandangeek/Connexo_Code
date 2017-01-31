/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.dashboard.rest.status.impl.ComServerTypeAdapter;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import javax.inject.Inject;

/**
 * Created by bvn on 10/2/14.
 */
public class ComServerStatusInfoFactory {

    private static final ComServerTypeAdapter COM_SERVER_TYPE_ADAPTER = new ComServerTypeAdapter();

    private final Thesaurus thesaurus;

    @Inject
    public ComServerStatusInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComServerStatusInfo from(ComServerStatus status, String defaultUri) {
        ComServerStatusInfo info = new ComServerStatusInfo();
        info.comServerName = status.getComServerName();
        info.comServerId=status.getComServerId();
        info.comServerType =  getComServerType(status.getComServerType());
        info.running = status.isRunning();
        info.blocked = status.isBlocked();
        info.uri = defaultUri;
        if (info.blocked) {
            info.blockTime = new TimeDurationInfo((int) status.getBlockTime().getSeconds());
            info.blockedSince = status.getBlockTimestamp();
        }
        return info;
    }

    private String getComServerType(ComServerType comServerType) {
        return COM_SERVER_TYPE_ADAPTER.marshal(comServerType);
    }

    private String getTranslatedComServerType(String comServerTypeString) {
        return thesaurus.getString(comServerTypeString, comServerTypeString);
    }

    private String getTranslatedComServerType(ComServerType comServerType) {
        String comServerTypeString = COM_SERVER_TYPE_ADAPTER.marshal(comServerType);
        return thesaurus.getString(comServerTypeString, comServerTypeString);
    }

    public ComServerStatusInfo from(long comServerId, String comServerName, String defaultUri, ComServerType comServerType) {
        ComServerStatusInfo statusInfo = new ComServerStatusInfo();
        statusInfo.comServerId = comServerId;
        statusInfo.comServerName = comServerName;
        statusInfo.uri = defaultUri;
        statusInfo.comServerType = getComServerType(comServerType);
        statusInfo.blocked = false;
        statusInfo.running = false;
        return statusInfo;
    }

    public ComServerStatusInfo translate(ComServerStatusInfo statusInfo) {
        statusInfo.comServerType = getTranslatedComServerType(statusInfo.comServerType);
        return statusInfo;
    }
}
