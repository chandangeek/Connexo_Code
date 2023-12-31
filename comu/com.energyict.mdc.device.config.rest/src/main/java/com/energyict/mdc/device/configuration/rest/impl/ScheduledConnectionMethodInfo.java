/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.ws.rs.core.UriInfo;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo<PartialScheduledConnectionTask> {

    public ScheduledConnectionMethodInfo() {
    }

    public ScheduledConnectionMethodInfo(PartialScheduledConnectionTask partialConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        super(partialConnectionTask, uriInfo, mdcPropertyUtils, thesaurus);
        if (partialConnectionTask.getConnectionStrategy() != null) {
            connectionStrategyInfo = new ConnectionStrategyInfo();
            connectionStrategyInfo.connectionStrategy =  partialConnectionTask.getConnectionStrategy().name();
            connectionStrategyInfo.localizedValue = ConnectionStrategyTranslationKeys.translationFor(partialConnectionTask.getConnectionStrategy(), thesaurus);
        }
        this.numberOfSimultaneousConnections = partialConnectionTask.getNumberOfSimultaneousConnections();
        this.numberOfRetriesConnectionMethod = partialConnectionTask.getNumberOfRetriesConnectionMethod();
        this.rescheduleRetryDelay = TimeDurationInfo.of(partialConnectionTask.getRescheduleDelay());
        if (partialConnectionTask.getCommunicationWindow()!=null) {
            this.comWindowStart=partialConnectionTask.getCommunicationWindow().getStart().getMillis()/1000;
            this.comWindowEnd=partialConnectionTask.getCommunicationWindow().getEnd().getMillis()/1000;
        } else {
            this.comWindowStart=0;
            this.comWindowEnd=0;
        }
        this.temporalExpression = partialConnectionTask.getNextExecutionSpecs()!=null?
                TemporalExpressionInfo.from(partialConnectionTask.getNextExecutionSpecs().getTemporalExpression()):null;
    }

    @Override
    protected void writeTo(PartialScheduledConnectionTask partialConnectionTask, DeviceType deviceType, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(partialConnectionTask, deviceType, engineConfigurationService, protocolPluggableService);
        partialConnectionTask.setDefault(this.isDefault);
        partialConnectionTask.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
        partialConnectionTask.setNumberOfRetriesConnectionMethod(this.numberOfRetriesConnectionMethod);
        if (this.comWindowEnd!=null && this.comWindowStart!=null) {
            partialConnectionTask.setComWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        } else {
            partialConnectionTask.setComWindow(null);
        }
        if (this.connectionStrategyInfo != null){
            partialConnectionTask.setConnectionStrategy(getConnectionStrategy());
        }
        if (!Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()) {
            engineConfigurationService.findOutboundComPortPoolByName(this.comPortPool).ifPresent(partialConnectionTask::setComportPool);
        } else {
            partialConnectionTask.setComportPool(null);
        }
        partialConnectionTask.setRescheduleRetryDelay(this.rescheduleRetryDelay!=null?this.rescheduleRetryDelay.asTimeDuration():null);
        if (temporalExpression !=null) {
            partialConnectionTask.setTemporalExpression(temporalExpression.asTemporalExpression());
        } else {
            partialConnectionTask.setNextExecutionSpecs(null);
        }
    }

    @Override
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionTypePluggableClass.id, protocolPluggableService);
        TimeDuration rescheduleDelay = this.rescheduleRetryDelay == null ? null : this.rescheduleRetryDelay.asTimeDuration();
        PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder =
                deviceConfiguration
                        .newPartialScheduledConnectionTask(this.name, connectionTypePluggableClass, rescheduleDelay, getConnectionStrategy(), protocolDialectConfigurationProperties)
                        .comPortPool(engineConfigurationService.findOutboundComPortPoolByName(this.comPortPool).orElse(null))
                        .connectionFunction(getConnectionFunction(deviceConfiguration.getDeviceType()))
                        .asDefault(this.isDefault)
                        .setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
        if (this.temporalExpression !=null) {
            if (this.temporalExpression.offset==null) {
                scheduledConnectionTaskBuilder
                        .nextExecutionSpec()
                        .temporalExpression(this.temporalExpression.every.asTimeDuration())
                        .set();
            } else {
                scheduledConnectionTaskBuilder
                        .nextExecutionSpec()
                        .temporalExpression(this.temporalExpression.every.asTimeDuration(), this.temporalExpression.offset.asTimeDuration())
                        .set();
            }
        }
        if(this.comWindowStart!=null && this.comWindowEnd!=null){
            scheduledConnectionTaskBuilder.comWindow(new ComWindow(this.comWindowStart, this.comWindowEnd));
        }

        addPropertiesToPartialConnectionTask(scheduledConnectionTaskBuilder, connectionTypePluggableClass);
        return scheduledConnectionTaskBuilder.build();
    }

    @JsonIgnore
    private ConnectionStrategy getConnectionStrategy(){
        try{
            return ConnectionStrategy.valueOf(this.connectionStrategyInfo.connectionStrategy);
        }catch (IllegalArgumentException e){
            return null;
        }
    }
}
