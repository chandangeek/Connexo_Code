package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WebServiceCallOccurrenceServiceImplIT extends AbstractWebServiceIT {
    @Test
    @Transactional
    public void findWebCallServiceOccurrence() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();

        WebServiceCallOccurrence tmpOccurrence  = endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), "RequestName", "MultiSense");

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(tmpOccurrence.getId());

        assertThat(epOcc.get().getId()).isEqualTo(tmpOccurrence.getId());
        assertThat(epOcc.get().getRequest().get()).isEqualTo("RequestName");
        assertThat(epOcc.get().getApplicationName().get()).isEqualTo("MultiSense");
        assertThat(epOcc.get().getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING);

        tmpOccurrence.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
        tmpOccurrence.save();
        epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(tmpOccurrence.getId());
        assertThat(epOcc.get().getId()).isEqualTo(tmpOccurrence.getId());
        assertThat(epOcc.get().getRequest().get()).isEqualTo("RequestName");
        assertThat(epOcc.get().getApplicationName().get()).isEqualTo("MultiSense");
        assertThat(epOcc.get().getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL);
    }

    @Test
    @Transactional
    public void findWebCallServiceOccurrences() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();

        WebServiceCallOccurrence tmpOccurrence1  = endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), "RequestName1", "MultiSense");

        WebServiceCallOccurrence tmpOccurrence2  = endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), "RequestName2", "MultiSense");

        List<WebServiceCallOccurrence> epOccList = webServiceCallOccurrenceService.getWebServiceCallOccurrenceFinderBuilder()
                .withApplicationNames(Collections.singleton("MultiSense"))
                .build()
                .paged(0, 10)
                .find();

        assertThat(epOccList.size()).isEqualTo(2);
        assertThat(epOccList.get(0).getId()).isEqualTo(tmpOccurrence1.getId());
        assertThat(epOccList.get(0).getRequest().get()).isEqualTo("RequestName1");
        assertThat(epOccList.get(0).getApplicationName().get()).isEqualTo("MultiSense");
        assertThat(epOccList.get(0).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING);

        assertThat(epOccList.get(1).getId()).isEqualTo(tmpOccurrence2.getId());
        assertThat(epOccList.get(1).getRequest().get()).isEqualTo("RequestName2");
        assertThat(epOccList.get(1).getApplicationName().get()).isEqualTo("MultiSense");
        assertThat(epOccList.get(1).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING);

        tmpOccurrence1.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
        tmpOccurrence1.save();
        tmpOccurrence2.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
        tmpOccurrence2.save();

        epOccList = webServiceCallOccurrenceService.getWebServiceCallOccurrenceFinderBuilder()
                .withApplicationNames(Collections.singleton("MultiSense"))
                .build()
                .paged(0, 10)
                .find();

        assertThat(epOccList.size()).isEqualTo(2);
        assertThat(epOccList.get(0).getId()).isEqualTo(tmpOccurrence1.getId());
        assertThat(epOccList.get(0).getRequest().get()).isEqualTo("RequestName1");
        assertThat(epOccList.get(0).getApplicationName().get()).isEqualTo("MultiSense");
        assertThat(epOccList.get(0).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL);

        assertThat(epOccList.get(1).getId()).isEqualTo(tmpOccurrence2.getId());
        assertThat(epOccList.get(1).getRequest().get()).isEqualTo("RequestName2");
        assertThat(epOccList.get(1).getApplicationName().get()).isEqualTo("MultiSense");
        assertThat(epOccList.get(1).getStatus()).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL);
    }

    @Test
    @Transactional
    public void findWebCallServiceOccurrenceLogs() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();

        WebServiceCallOccurrence tmpOccurrence1  = endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), "RequestName1", "MultiSense");

        WebServiceCallOccurrence tmpOccurrence2  = endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), "RequestName2", "MultiSense");


        tmpOccurrence1.log(LogLevel.SEVERE, "MESSAGE1");
        tmpOccurrence1.log(LogLevel.SEVERE, "MESSAGE2");

        tmpOccurrence2.log(LogLevel.INFO, "MESSAGE1");//LogLevel is Severe. so shouldn't be logged.
        tmpOccurrence2.log(LogLevel.SEVERE, "MESSAGE2");


        List<EndPointLog> logs = endPointConfiguration.getLogs().find();
        assertThat(logs.size()).isEqualTo(0);

        logs = webServiceCallOccurrenceService.getOccurrenceLogFinderBuilder()
                .withOccurrenceId(tmpOccurrence1)
                .build()
                .paged(0, 10)
                .find();
        assertThat(logs.size()).isEqualTo(2);
        assertThat(logs.get(0).getMessage()).isEqualTo("MESSAGE1");
        assertThat(logs.get(1).getMessage()).isEqualTo("MESSAGE2");

        logs = webServiceCallOccurrenceService.getOccurrenceLogFinderBuilder()
                .withOccurrenceId(tmpOccurrence2)
                .build()
                .paged(0, 10)
                .find();
        assertThat(logs.size()).isEqualTo(1);
        assertThat(logs.get(0).getMessage()).isEqualTo("MESSAGE2");
    }
}
