package com.energyict.mdc.dashboard.rest.status;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the summary of statusses of all {@link com.energyict.mdc.engine.model.ComServer}s
 * that are configured in the system, in the REST layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (16:35)
 */
public class ComServerStatusSummaryInfo {

    public List<ComServerStatusInfo> comServerStatusInfos = new ArrayList<>();

    public void add(ComServerStatusInfo comServerStatusInfo) {
        this.comServerStatusInfos.add(comServerStatusInfo);
    }

}