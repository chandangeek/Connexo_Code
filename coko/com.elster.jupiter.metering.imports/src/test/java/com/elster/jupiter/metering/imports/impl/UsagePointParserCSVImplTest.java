package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointParserCSVImplTest {

    @Mock
    private Thesaurus thesaurus;

    @Mock
    private Logger logger;

    @Mock
    private FileImportOccurrence fileImportOccurrenceCorrect;

    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrect;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        try {
            when(fileImportOccurrenceCorrect.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrect.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceCorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader().getResource("usagepoint_correct.csv").getPath()));
            when(fileImportOccurrenceIncorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader().getResource("usagepoint_incorrect.csv").getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessCorrectFile() throws IOException {
        UsagePointParser parserCSV = new UsagePointParserCSVImpl();
        List<UsagePointFileInfo> usagePointFileInfos = parserCSV.parse(fileImportOccurrenceCorrect, thesaurus);
        assertNotNull(usagePointFileInfos);
        assertEquals("less_demo5", usagePointFileInfos.get(0).getmRID());
        assertEquals("ELECTRICITY", usagePointFileInfos.get(0).getServiceKind());
        assertEquals(1L, usagePointFileInfos.get(0).getServiceLocationID());
        assertEquals("name", usagePointFileInfos.get(0).getName());
        assertEquals("aliasName", usagePointFileInfos.get(0).getAliasName());
        assertEquals("description", usagePointFileInfos.get(0).getDescription());
        assertEquals("outageregion", usagePointFileInfos.get(0).getOutageregion());
        assertEquals("readcycle", usagePointFileInfos.get(0).getReadcycle());
        assertEquals("readroute", usagePointFileInfos.get(0).getReadroute());
        assertEquals("servicePriority", usagePointFileInfos.get(0).getServicePriority());
        assertEquals("true", usagePointFileInfos.get(0).getGrounded());
        assertEquals("ABCN", usagePointFileInfos.get(0).getPhaseCode());
        assertEquals("26.34", usagePointFileInfos.get(0).getNominalVoltageValue());
        assertEquals(3, usagePointFileInfos.get(0).getNominalVoltageMultiplier());
        assertEquals("V", usagePointFileInfos.get(0).getNominalVoltageUnit());
        assertEquals("4.6", usagePointFileInfos.get(0).getEstimatedLoadValue());
        assertEquals(3, usagePointFileInfos.get(0).getEstimatedLoadMultiplier());
        assertEquals("W", usagePointFileInfos.get(0).getEstimatedLoadUnit());
        assertEquals("2.36", usagePointFileInfos.get(0).getRatedCurrentValue());
        assertEquals(6, usagePointFileInfos.get(0).getRatedCurrentMultiplier());
        assertEquals("A", usagePointFileInfos.get(0).getRatedCurrentUnit());
        assertEquals("1.24", usagePointFileInfos.get(0).getRatedPowerValue());
        assertEquals(9, usagePointFileInfos.get(0).getRatedPowerMultiplier());
        assertEquals("W", usagePointFileInfos.get(0).getRatedPowerUnit());
        assertEquals("", usagePointFileInfos.get(0).getAllowUpdate());
    }

    @Test
    public void testProcessIncorrectFile() throws IOException {
        UsagePointParser parserCSV = new UsagePointParserCSVImpl();
        List<UsagePointFileInfo> usagePointFileInfos = parserCSV.parse(fileImportOccurrenceIncorrect, thesaurus);
        assertNotNull(usagePointFileInfos);
        assertEquals("", usagePointFileInfos.get(0).getmRID());
        assertEquals("", usagePointFileInfos.get(0).getServiceKind());
        assertEquals(0L, usagePointFileInfos.get(0).getServiceLocationID());
        assertEquals("", usagePointFileInfos.get(0).getName());
        assertEquals("", usagePointFileInfos.get(0).getAliasName());
        assertEquals("", usagePointFileInfos.get(0).getDescription());
        assertEquals("", usagePointFileInfos.get(0).getOutageregion());
        assertEquals("", usagePointFileInfos.get(0).getReadcycle());
        assertEquals("", usagePointFileInfos.get(0).getReadroute());
        assertEquals("", usagePointFileInfos.get(0).getServicePriority());
        assertEquals("", usagePointFileInfos.get(0).getGrounded());
        assertEquals("", usagePointFileInfos.get(0).getPhaseCode());
        assertEquals("", usagePointFileInfos.get(0).getNominalVoltageValue());
        assertEquals(0, usagePointFileInfos.get(0).getNominalVoltageMultiplier());
        assertEquals("", usagePointFileInfos.get(0).getNominalVoltageUnit());
        assertEquals("", usagePointFileInfos.get(0).getEstimatedLoadValue());
        assertEquals(0, usagePointFileInfos.get(0).getEstimatedLoadMultiplier());
        assertEquals("", usagePointFileInfos.get(0).getEstimatedLoadUnit());
        assertEquals("", usagePointFileInfos.get(0).getRatedCurrentValue());
        assertEquals(0, usagePointFileInfos.get(0).getRatedCurrentMultiplier());
        assertEquals("", usagePointFileInfos.get(0).getRatedCurrentUnit());
        assertEquals("", usagePointFileInfos.get(0).getRatedPowerValue());
        assertEquals(0, usagePointFileInfos.get(0).getRatedPowerMultiplier());
        assertEquals("", usagePointFileInfos.get(0).getRatedPowerUnit());
        assertEquals("", usagePointFileInfos.get(0).getAllowUpdate());
    }
}