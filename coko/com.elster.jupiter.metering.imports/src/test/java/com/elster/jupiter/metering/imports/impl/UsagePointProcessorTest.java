package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointProcessorTest {

    @Mock
    Clock clock;

    @Mock
    private Logger logger;

    @Mock
    private UsagePoint usagePoint;

    @Mock
    private Thesaurus thesaurus;

    @Mock
    MeteringService meteringService;

    @Mock
    private ServiceCategory serviceCategoryOne;

    @Mock
    private ServiceCategory serviceCategoryTwo;

    @Mock
    private ServiceLocation servicelocation;

    @Mock
    NlsMessageFormat nlsMessageFormat;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(meteringService.findUsagePoint(anyString())).thenReturn(Optional.ofNullable(usagePoint));
        when(meteringService.getServiceCategory(Matchers.any(ServiceKind.class))).thenReturn(Optional.ofNullable(serviceCategoryTwo));
        when(meteringService.findServiceLocation(anyLong())).thenReturn(Optional.ofNullable(servicelocation));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategoryOne);
        when(serviceCategoryTwo.getId()).thenReturn(34L);
        when(thesaurus.getFormat((Matchers.any(MessageSeeds.class)))).thenReturn(nlsMessageFormat);
    }

    @Test
    public void testProcessCorrectInfo() throws IOException {
        UsagePointFileInfo usagePointFileInfo = new UsagePointFileInfo();
        UsagePointProcessor usagePointProcessor = new UsagePointProcessor(clock, thesaurus, meteringService);
        usagePointFileInfo.setmRID("test");
        usagePointFileInfo.setServiceKind("GAS");
        usagePointFileInfo.setServiceLocationID(23L);
        when(serviceCategoryOne.getId()).thenReturn(34L);
        assertTrue(usagePointProcessor.validateData(usagePointFileInfo, logger, 5).isPresent());
    }

    @Test
    public void testProcessIncorrectInfo() throws IOException {
        UsagePointFileInfo usagePointFileInfo = new UsagePointFileInfo();
        UsagePointProcessor usagePointProcessor = new UsagePointProcessor(clock, thesaurus, meteringService);
        usagePointFileInfo.setmRID("test");
        usagePointFileInfo.setServiceKind("GAS");
        when(serviceCategoryOne.getId()).thenReturn(54L);
        assertFalse(usagePointProcessor.validateData(usagePointFileInfo, logger, 5).isPresent());
    }
}