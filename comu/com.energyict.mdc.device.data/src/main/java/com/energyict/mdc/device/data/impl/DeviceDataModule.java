package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiServiceImpl;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.CommunicationTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import java.time.Clock;

/**
 * Module intended for use by integration tests.
 * <p>
 * Copyrights EnergyICT
 * Date: 26/02/14
 * Time: 11:30
 */
public class DeviceDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(TransactionService.class);
        requireBinding(EventService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(MeteringService.class);
        requireBinding(ValidationService.class);
        requireBinding(Clock.class);
        requireBinding(RelationService.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(SchedulingService.class);
        requireBinding(DeviceMessageSpecificationService.class);
        requireBinding(NlsService.class);

        bind(SecurityPropertyService.class).to(SecurityPropertyServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceDataModelService.class).to(DeviceDataModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceService.class).to(DeviceServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionTaskService.class).to(ConnectionTaskServiceImpl.class).in(Scopes.SINGLETON);
        bind(CommunicationTaskService.class).to(CommunicationTaskServiceImpl.class).in(Scopes.SINGLETON);
        bind(LoadProfileService.class).to(LoadProfileServiceImpl.class).in(Scopes.SINGLETON);
        bind(LogBookService.class).to(LogBookServiceImpl.class).in(Scopes.SINGLETON);
        bind(IdentificationService.class).to(IdentificationServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataCollectionKpiService.class).to(DataCollectionKpiServiceImpl.class).in(Scopes.SINGLETON);
        bind(BatchService.class).to(BatchServiceImpl.class).in(Scopes.SINGLETON);
    }

}