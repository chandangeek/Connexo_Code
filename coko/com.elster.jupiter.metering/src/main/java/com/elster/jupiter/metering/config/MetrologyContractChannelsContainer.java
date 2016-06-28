package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ChannelsContainer;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MetrologyContractChannelsContainer extends ChannelsContainer {

    MetrologyContract getMetrologyContract();
}
