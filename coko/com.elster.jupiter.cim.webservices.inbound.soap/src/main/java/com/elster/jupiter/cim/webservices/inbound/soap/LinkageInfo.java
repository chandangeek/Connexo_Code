/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap;

import java.util.List;

public class LinkageInfo {

    private ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent configurationEventNode;
    private List<ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint> usagePointNodes;
    private List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> meterNodes;

    public ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent getConfigurationEventNode() {
        return configurationEventNode;
    }

    public void setConfigurationEventNode(
            ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent configurationEventNode) {
        this.configurationEventNode = configurationEventNode;
    }

    public List<ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint> getUsagePointNodes() {
        return usagePointNodes;
    }

    public void setUsagePointNodes(List<ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint> usagePointNodes) {
        this.usagePointNodes = usagePointNodes;
    }

    public List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> getMeterNodes() {
        return meterNodes;
    }

    public void setMeterNodes(List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> meterNodes) {
        this.meterNodes = meterNodes;
    }

}
