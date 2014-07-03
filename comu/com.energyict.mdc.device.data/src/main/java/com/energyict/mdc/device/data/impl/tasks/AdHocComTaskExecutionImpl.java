package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueAdHocComTaskExecutionPerDevice;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecution;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecutionUpdater;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.Date;

@UniqueAdHocComTaskExecutionPerDevice
public class AdHocComTaskExecutionImpl extends SingleComTaskExecutionImpl implements AdHocComTaskExecution {

    @Inject
    public AdHocComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, schedulingService);
    }

    public AdHocComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        this.initializeFrom(device, comTaskEnablement);
        this.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
        return this;
    }

    @Override
    public boolean isScheduled() {
        return false;
    }

    @Override
    public boolean isScheduledManually() {
        return false;
    }

    @Override
    public boolean isAdHoc() {
        return true;
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return Optional.absent();
    }

    @Override
    protected Date calculateNextExecutionTimestamp(Date now) {
        return null;
    }

    @Override
    public int getMaxNumberOfTries() {
        return getComTask().getMaxNumberOfTries();
    }

    @Override
    // Override from superclass to add @Override annotation
    public ComTask getComTask() {
        return super.getComTask();
    }

    @Override
    // Override from superclass only to add @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return super.getProtocolDialectConfigurationProperties();
    }

    @Override
    public AdHocComTaskExecutionUpdater getUpdater() {
        return new AdHocComTaskExecutionUpdaterImpl(this);
    }

    public static class AdHocComTaskExecutionBuilderImpl extends AbstractComTaskExecutionBuilder<AdHocComTaskExecutionBuilder, AdHocComTaskExecution, AdHocComTaskExecutionImpl> implements AdHocComTaskExecutionBuilder{

        protected AdHocComTaskExecutionBuilderImpl(AdHocComTaskExecutionImpl adHocComTaskExecution) {
            super(adHocComTaskExecution, AdHocComTaskExecutionBuilder.class);
        }

        @Override
        public AdHocComTaskExecutionBuilder protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.getComTaskExecution().setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return self();
        }

    }

    class AdHocComTaskExecutionUpdaterImpl
        extends AbstractComTaskExecutionUpdater<AdHocComTaskExecutionUpdater, AdHocComTaskExecution, AdHocComTaskExecutionImpl>
        implements AdHocComTaskExecutionUpdater {

        protected AdHocComTaskExecutionUpdaterImpl(AdHocComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, AdHocComTaskExecutionUpdater.class);
        }

        @Override
        public AdHocComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.getComTaskExecution().setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return this;
        }
    }

}