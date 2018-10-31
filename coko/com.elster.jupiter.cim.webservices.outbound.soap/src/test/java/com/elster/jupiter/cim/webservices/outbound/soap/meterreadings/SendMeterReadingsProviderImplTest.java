/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.MessageSeeds;
import com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.MeterReadinsServiceException;
import com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.SendMeterReadingsProviderImpl;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendMeterReadingsProviderImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Mock
    private NlsService nlsService;

    @Mock
    private ReadingStorer readingStorer;

    @Mock
    MeterReadings meterReadings;

    @Mock
    private ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType outboundMessage;

    @Mock
    private MeterReadingsPort meterReadingsPort;

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Before
    public void setUp() {
        when(nlsService.getThesaurus(SendMeterReadingsProvider.NAME, Layer.SERVICE)).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() throws FaultMessage {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);
        provider.addMeterReadingsPortService(meterReadingsPort);
        provider.call(meterReadings, true);

        Mockito.verify(meterReadingsPort).createdMeterReadings(Mockito.any(MeterReadingsEventMessageType.class));
    }

    @Test
    public void testSendWithoutPort() {
        SendMeterReadingsProvider provider = new SendMeterReadingsProviderImpl(nlsService);

        expectedException.expect(MeterReadinsServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.send(readingStorer, true);
    }

    @Test
    public void testGetService() {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);
        Assert.assertEquals(provider.getService(), MeterReadingsPort.class);
    }

    @Test
    public void testGet() {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);
        Assert.assertEquals(provider.get().getClass(), ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings.class);
    }
}