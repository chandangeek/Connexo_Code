package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides functionality to update the cache of a Device in the database
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:22
 */
public class CollectedDeviceCacheCommand extends DeviceCommandImpl {

    private final UpdatedDeviceCache deviceCache;

    public CollectedDeviceCacheCommand(UpdatedDeviceCache deviceCache, IssueService issueService) {
        super(issueService);
        this.deviceCache = deviceCache;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        // we will only perform the update when the cache actually changed
        DeviceProtocolCache collectedDeviceCache = this.deviceCache.getCollectedDeviceCache();
        if (collectedDeviceCache != null && collectedDeviceCache.contentChanged()) {
            DeviceCacheShadow shadow = new DeviceCacheShadow();
            DeviceIdentifier deviceIdentifier = this.deviceCache.getDeviceIdentifier();
            shadow.setRtuId((int) deviceIdentifier.findDevice().getId());
            shadow.setSimpleCacheObject(collectedDeviceCache);
            comServerDAO.createOrUpdateDeviceCache((int) deviceIdentifier.findDevice().getId(), shadow);
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        builder.addProperty("deviceIdentifier").append(this.deviceCache.getDeviceIdentifier());
    }

}