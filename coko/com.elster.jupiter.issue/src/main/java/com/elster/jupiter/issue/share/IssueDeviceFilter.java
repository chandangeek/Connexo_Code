package com.elster.jupiter.issue.share;

import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface IssueDeviceFilter {

    List<EndDevice> getShowTopologyCondition(List<EndDevice> endDeviceList);
}
