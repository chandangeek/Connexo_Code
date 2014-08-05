package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link ManuallyScheduledComTaskExecution} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
public class ManuallyScheduledComTaskExecutionImpl extends SingleComTaskExecutionImpl implements ManuallyScheduledComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NEXTEXECUTIONSPEC_IS_REQUIRED + "}")
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    @Inject
    public ManuallyScheduledComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, schedulingService);
    }

    public ManuallyScheduledComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TemporalExpression temporalExpression) {
        this.initializeFrom(device, comTaskEnablement);
        this.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
        this.setNextExecutionSpecsFrom(temporalExpression);
        return this;
    }

    @Override
    protected void postNew() {
        this.recalculateNextAndPlannedExecutionTimestamp();
        super.postNew();
    }

    @Override
    public void prepareForSaving() {
        this.nextExecutionSpecs.get().save();
        super.prepareForSaving();
    }

    @Override
    public void doDelete() {
        super.doDelete();
        this.nextExecutionSpecs.get().delete();
    }

    @Override
    public boolean isScheduled() {
        return true;
    }

    @Override
    public boolean isScheduledManually() {
        return true;
    }

    @Override
    public boolean isAdHoc() {
        return false;
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return this.nextExecutionSpecs.getOptional();
    }

    public void setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
        if (!this.nextExecutionSpecs.isPresent()) {
            this.nextExecutionSpecs.set(this.getSchedulingService().newNextExecutionSpecs(temporalExpression));
        }
        else {
            this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
        }
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
    public ManuallyScheduledComTaskExecutionUpdater getUpdater() {
        return new ManuallyScheduledComTaskExecutionUpdaterImpl(this);
    }

    public static class ManuallyScheduledComTaskExecutionBuilderImpl extends AbstractComTaskExecutionBuilder<ManuallyScheduledComTaskExecutionBuilder, ManuallyScheduledComTaskExecution, ManuallyScheduledComTaskExecutionImpl> implements ManuallyScheduledComTaskExecutionBuilder {

        protected ManuallyScheduledComTaskExecutionBuilderImpl(ManuallyScheduledComTaskExecutionImpl manuallyScheduledComTaskExecution) {
            super(manuallyScheduledComTaskExecution, ManuallyScheduledComTaskExecutionBuilder.class);
        }

        @Override
        public ManuallyScheduledComTaskExecutionBuilder scheduleAccordingTo(TemporalExpression temporalExpression) {
            this.getComTaskExecution().setNextExecutionSpecsFrom(temporalExpression);
            return self();
        }

    }

    class ManuallyScheduledComTaskExecutionUpdaterImpl
        extends AbstractComTaskExecutionUpdater<ManuallyScheduledComTaskExecutionUpdater, ManuallyScheduledComTaskExecution, ManuallyScheduledComTaskExecutionImpl>
        implements ManuallyScheduledComTaskExecutionUpdater {

        protected ManuallyScheduledComTaskExecutionUpdaterImpl(ManuallyScheduledComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, ManuallyScheduledComTaskExecutionUpdater.class);
        }

        @Override
        public ManuallyScheduledComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.getComTaskExecution().setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return self();
        }

        @Override
        public ManuallyScheduledComTaskExecutionUpdater scheduleAccordingTo(TemporalExpression temporalExpression) {
            this.getComTaskExecution().setNextExecutionSpecsFrom(temporalExpression);
            return self();
        }

    }

}