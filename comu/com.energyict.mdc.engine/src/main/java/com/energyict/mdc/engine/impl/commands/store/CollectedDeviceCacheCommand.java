package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedDeviceCacheEvent;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to update the cache of a Device in the database.
 * <p>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:22
 */
public class CollectedDeviceCacheCommand extends DeviceCommandImpl<CollectedDeviceCacheEvent> {

    public static final String DESCRIPTION_TITLE = "Collected device cache";

    private final UpdatedDeviceCache deviceCache;

    public CollectedDeviceCacheCommand(UpdatedDeviceCache deviceCache, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceCache = deviceCache;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        // we will only perform the update when the cache actually changed
        DeviceProtocolCache collectedDeviceCache = this.deviceCache.getCollectedDeviceCache();
        if (collectedDeviceCache != null && collectedDeviceCache.contentChanged()) {
            DeviceIdentifier deviceIdentifier = this.deviceCache.getDeviceIdentifier();
            try {
                Optional<DeviceCache> deviceCache = this.getEngineService().findDeviceCacheByDeviceIdentifier(deviceIdentifier);
                if (deviceCache.isPresent()) {
                    DeviceCache actualDeviceCache = deviceCache.get();
                    actualDeviceCache.setCacheObject(collectedDeviceCache);
                    actualDeviceCache.update();
                } else {
                    this.getEngineService().newDeviceCache(deviceIdentifier, collectedDeviceCache);
                }
            } catch (IllegalArgumentException e) {
                //Device could not be found
                this.addIssue(CompletionCode.ConfigurationWarning,
                        this.getIssueService().newWarning(this,
                                MessageSeeds.COLLECTED_DEVICE_CACHE_FOR_UNKNOWN_DEVICE,
                                deviceIdentifier));
            }
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        builder.addProperty("deviceIdentifier").append(this.deviceCache.getDeviceIdentifier());
    }

    protected Optional<CollectedDeviceCacheEvent> newEvent(List<Issue> issues) {
        CollectedDeviceCacheEvent event = new CollectedDeviceCacheEvent(new ComServerEventServiceProvider(), deviceCache);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }
}