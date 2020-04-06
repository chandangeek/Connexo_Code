package com.elster.jupiter.bpm.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BpmServerImplTest {

    @Mock
    BpmServerImpl bpmServer;

    @Test
    public void testUrlConcatenationNoTrainingSlash() throws MalformedURLException {
        when(bpmServer.buildTargetUrl(anyString())).thenCallRealMethod();

        when(bpmServer.getUrl()).thenReturn("http://localhost:8081/flow");
        URL targetUrl = bpmServer.buildTargetUrl("/rest/organizationalunits/");

        assertEquals("http://localhost:8081/flow/rest/organizationalunits/", targetUrl.toString() );
    }


    @Test
    public void testUrlConcatenationWithTrainingSlash() throws MalformedURLException {
        when(bpmServer.buildTargetUrl(anyString())).thenCallRealMethod();

        when(bpmServer.getUrl()).thenReturn("http://localhost:8081/flow/");
        URL targetUrl = bpmServer.buildTargetUrl("/rest/organizationalunits/");

        assertEquals("http://localhost:8081/flow/rest/organizationalunits/", targetUrl.toString() );
    }

    @Test
    public void testUrlConcatenationNoTrainingSlashAndNoRoot() throws MalformedURLException {
        when(bpmServer.buildTargetUrl(anyString())).thenCallRealMethod();

        when(bpmServer.getUrl()).thenReturn("http://localhost:8081/flow");
        URL targetUrl = bpmServer.buildTargetUrl("rest/organizationalunits/");

        assertEquals("http://localhost:8081/flow/rest/organizationalunits/", targetUrl.toString() );
    }


    @Test
    public void testUrlConcatenationWithTrainingSlashAndNoRoot() throws MalformedURLException {
        when(bpmServer.buildTargetUrl(anyString())).thenCallRealMethod();

        when(bpmServer.getUrl()).thenReturn("http://localhost:8081/flow/");
        URL targetUrl = bpmServer.buildTargetUrl("rest/organizationalunits/");

        assertEquals("http://localhost:8081/flow/rest/organizationalunits/", targetUrl.toString() );
    }

}