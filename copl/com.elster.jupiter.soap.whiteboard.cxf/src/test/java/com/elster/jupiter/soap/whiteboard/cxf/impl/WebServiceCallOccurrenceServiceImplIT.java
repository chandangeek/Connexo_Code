package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Where;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WebServiceCallOccurrenceServiceImplIT extends WebServiceTest{
    @Test
    public void findWebCallServiceOccurrence() {
        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();

            WebServiceCallOccurrence tmpOccurrence  = endPointConfiguration.createEndPointOccurrence(clock.instant(), "RequestName", "Multisense");

            //WebServiceCallOccurrence tmpOccurrence = webServicesService.startOccurrence(endPointConfiguration, "RequestName", "Multisense");
            Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(tmpOccurrence.getId());

            assertThat(epOcc.get().getId()).isEqualTo(tmpOccurrence.getId());
            assertThat(epOcc.get().getRequest().get()).isEqualTo("RequestName");
            assertThat(epOcc.get().getApplicationName().get()).isEqualTo("Multisense");
            assertThat(epOcc.get().getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING);

            tmpOccurrence.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
            tmpOccurrence.save();
            epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(tmpOccurrence.getId());
            assertThat(epOcc.get().getId()).isEqualTo(tmpOccurrence.getId());
            assertThat(epOcc.get().getRequest().get()).isEqualTo("RequestName");
            assertThat(epOcc.get().getApplicationName().get()).isEqualTo("Multisense");
            assertThat(epOcc.get().getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL);
        }
    }


    @Test
    public void findWebCallServiceOccurrences() {

        JsonQueryParameters queryParameters = new JsonQueryParameters(0,10);
        JsonQueryFilter filter = new JsonQueryFilter(null);

        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();

            WebServiceCallOccurrence tmpOccurrence1  = endPointConfiguration.createEndPointOccurrence(clock.instant(), "RequestName1", "Multisense");

            WebServiceCallOccurrence tmpOccurrence2  = endPointConfiguration.createEndPointOccurrence(clock.instant(), "RequestName2", "Multisense");
            
            Set names = new HashSet();
            names.add("Multisense");
            List<WebServiceCallOccurrence> epOccList = webServiceCallOccurrenceService.getEndPointOccurrences(queryParameters, filter, names, null);

            assertThat(epOccList.size()).isEqualTo(2);
            assertThat(epOccList.get(0).getId()).isEqualTo(tmpOccurrence1.getId());
            assertThat(epOccList.get(0).getRequest().get()).isEqualTo("RequestName1");
            assertThat(epOccList.get(0).getApplicationName().get()).isEqualTo("Multisense");
            assertThat(epOccList.get(0).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING);

            assertThat(epOccList.get(1).getId()).isEqualTo(tmpOccurrence2.getId());
            assertThat(epOccList.get(1).getRequest().get()).isEqualTo("RequestName2");
            assertThat(epOccList.get(1).getApplicationName().get()).isEqualTo("Multisense");
            assertThat(epOccList.get(1).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING);

            tmpOccurrence1.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
            tmpOccurrence1.save();
            tmpOccurrence2.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
            tmpOccurrence2.save();

            epOccList = webServiceCallOccurrenceService.getEndPointOccurrences(queryParameters, filter, names, null);

            assertThat(epOccList.size()).isEqualTo(2);
            assertThat(epOccList.get(0).getId()).isEqualTo(tmpOccurrence1.getId());
            assertThat(epOccList.get(0).getRequest().get()).isEqualTo("RequestName1");
            assertThat(epOccList.get(0).getApplicationName().get()).isEqualTo("Multisense");
            assertThat(epOccList.get(0).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL);

            assertThat(epOccList.get(1).getId()).isEqualTo(tmpOccurrence2.getId());
            assertThat(epOccList.get(1).getRequest().get()).isEqualTo("RequestName2");
            assertThat(epOccList.get(1).getApplicationName().get()).isEqualTo("Multisense");
            assertThat(epOccList.get(1).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL);
       }
    }


    @Test
    public void findWebCallServiceOccurrenceLogs() {

        JsonQueryParameters queryParameters = new JsonQueryParameters(0,10);
        JsonQueryFilter filter = new JsonQueryFilter(null);

        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();

            WebServiceCallOccurrence tmpOccurrence1  = endPointConfiguration.createEndPointOccurrence(clock.instant(), "RequestName1", "Multisense");

            WebServiceCallOccurrence tmpOccurrence2  = endPointConfiguration.createEndPointOccurrence(clock.instant(), "RequestName2", "Multisense");


            tmpOccurrence1.log(LogLevel.SEVERE, "MESSAGE1");
            tmpOccurrence1.log(LogLevel.SEVERE, "MESSAGE2");

            tmpOccurrence2.log(LogLevel.INFO, "MESSAGE1");//LogLevel is Severe. so shouldn't be logged.
            tmpOccurrence2.log(LogLevel.SEVERE, "MESSAGE2");


            List<EndPointLog> logs = endPointConfiguration.getLogs().find();
            assertThat(logs.size()).isEqualTo(0);

            logs = webServiceCallOccurrenceService.getLogForOccurrence(tmpOccurrence1.getId(), queryParameters);
            assertThat(logs.size()).isEqualTo(2);
            assertThat(logs.get(0).getMessage()).isEqualTo("MESSAGE1");
            assertThat(logs.get(1).getMessage()).isEqualTo("MESSAGE2");

            logs = webServiceCallOccurrenceService.getLogForOccurrence(tmpOccurrence2.getId(), queryParameters);
            assertThat(logs.size()).isEqualTo(1);
            assertThat(logs.get(0).getMessage()).isEqualTo("MESSAGE2");
        }
    }


    @Test
    public void testCreateInboundEndpoint() {
        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();
            assertThat(endPointConfigurationService.findEndPointConfigurations().find()).hasSize(1);
            assertThat(endPointConfigurationService.streamEndPointConfigurations().findAny()).isPresent();
            assertThat(endPointConfigurationService.streamEndPointConfigurations().filter(Where.where("logLevel").isEqualTo(LogLevel.INFO)).findAny()).isEmpty();
            assertThat(endPointConfigurationService.streamEndPointConfigurations().filter(Where.where("logLevel").isEqualTo(LogLevel.SEVERE)).findAny()).isPresent();
        }
    }
}
