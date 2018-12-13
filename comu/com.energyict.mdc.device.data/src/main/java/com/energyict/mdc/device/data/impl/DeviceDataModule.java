/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CertificateRenewalService;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyRenewalService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.ami.EndDeviceCommandFactory;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.impl.ami.EndDeviceCommandFactoryImpl;
import com.energyict.mdc.device.data.impl.crlrequest.CrlRequestTaskPropertiesServiceImpl;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiServiceImpl;
import com.energyict.mdc.device.data.impl.pki.tasks.certrenewal.CertificateRenewalHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal.KeyRenewalHandlerFactory;
import com.energyict.mdc.device.data.impl.tasks.CommunicationTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.report.CommunicationTaskReportServiceImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class DeviceDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(TransactionService.class);
        requireBinding(EventService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(MeteringService.class);
        requireBinding(ValidationService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(Clock.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(SchedulingService.class);
        requireBinding(DeviceMessageSpecificationService.class);
        requireBinding(NlsService.class);
        requireBinding(ServiceCallService.class);
        requireBinding(LockService.class);

        bind(DeviceDataModelService.class).to(DeviceDataModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceService.class).to(DeviceServiceImpl.class).in(Scopes.SINGLETON);
        bind(RegisterService.class).to(RegisterServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionTaskService.class).to(ConnectionTaskServiceImpl.class).in(Scopes.SINGLETON);
        bind(CommunicationTaskService.class).to(CommunicationTaskServiceImpl.class).in(Scopes.SINGLETON);
        bind(CommunicationTaskReportService.class).to(CommunicationTaskReportServiceImpl.class).in(Scopes.SINGLETON);
        bind(LoadProfileService.class).to(LoadProfileServiceImpl.class).in(Scopes.SINGLETON);
        bind(LogBookService.class).to(LogBookServiceImpl.class).in(Scopes.SINGLETON);
        bind(IdentificationService.class).to(IdentificationServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataCollectionKpiService.class).to(DataCollectionKpiServiceImpl.class).in(Scopes.SINGLETON);
        bind(BatchService.class).to(BatchServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceMessageService.class).to(DeviceMessageServiceImpl.class).in(Scopes.SINGLETON);
        bind(EndDeviceCommandFactory.class).to(EndDeviceCommandFactoryImpl.class).in(Scopes.SINGLETON);
        bind(CertificateRenewalService.class).to(CertificateRenewalHandlerFactory.class).in(Scopes.SINGLETON);
        bind(KeyRenewalService.class).to(KeyRenewalHandlerFactory.class).in(Scopes.SINGLETON);
        bind(CrlRequestTaskPropertiesService.class).to(CrlRequestTaskPropertiesServiceImpl.class).in(Scopes.SINGLETON);
        bind(CrlRequestService.class).to(CrlRequestHandlerFactory.class).in(Scopes.SINGLETON);
    }
}
