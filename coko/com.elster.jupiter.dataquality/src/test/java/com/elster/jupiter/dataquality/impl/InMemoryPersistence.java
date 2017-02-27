/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import static org.mockito.Mockito.mock;

public class InMemoryPersistence {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    private ValidationService validationService;
    private EstimationService estimationService;

    public void activate() {
        initMocks();
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new AppServiceModule(),
                new FileImportModule(),
                new WebServicesModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new BasicPropertiesModule(),
                new TimeModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new TaskModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new KpiModule(),
                new NlsModule(),
                new DataVaultModule(),
                new CustomPropertySetsModule(),
                new UserModule(),
                new DataQualityKpiModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(DataQualityKpiService.class);
            ctx.commit();
        }
    }

    private void initMocks() {
        this.validationService = mock(ValidationService.class);
        this.estimationService = mock(EstimationService.class);
    }

    public void deactivate() {
        inMemoryBootstrapModule.deactivate();
    }

    public <T> T get(Class<T> clazz) {
        return this.injector.getInstance(clazz);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(ValidationService.class).toInstance(validationService);
            bind(EstimationService.class).toInstance(estimationService);
        }
    }
}
