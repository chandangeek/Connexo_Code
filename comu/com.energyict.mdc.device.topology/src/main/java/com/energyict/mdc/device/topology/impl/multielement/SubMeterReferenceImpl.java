/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.multielement;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.impl.AllDataLoggerChannelsAvailable;
import com.energyict.mdc.device.topology.impl.AllSlaveChannelsIncluded;
import com.energyict.mdc.device.topology.impl.DataLoggerLinkException;
import com.energyict.mdc.device.topology.impl.DataLoggerReferenceImpl;
import com.energyict.mdc.device.topology.impl.MessageSeeds;
import com.energyict.mdc.device.topology.impl.PhysicalGatewayNotSameAsOrigin;

import javax.inject.Inject;

@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_MULTI_ELEMENT_METER_FOR_ITSELF + "}")
@OriginDeviceTypeIsMultiElementSubmeter(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NOT_A_MULTI_ELEMENT_SUBMETER_DEVICE + "}")
@GatewayDeviceTypeIsMultiElementEnabled(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.GATEWAY_NOT_MULTI_ELEMENT_ENABLED + "}")
@AllSlaveChannelsIncluded(groups = {Save.Create.class}, message = "{DataLoggerLinkException.noPhysicalSlaveChannelForReadingTypeX}")
@AllDataLoggerChannelsAvailable(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.MULTI_ELEMENT_DEVICE_CHANNEL_ALREADY_REFERENCED + "}")
public class SubMeterReferenceImpl extends DataLoggerReferenceImpl {

    @Inject
    public SubMeterReferenceImpl(Thesaurus thesaurus) {
        super(thesaurus);
    }

    public SubMeterReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

}
