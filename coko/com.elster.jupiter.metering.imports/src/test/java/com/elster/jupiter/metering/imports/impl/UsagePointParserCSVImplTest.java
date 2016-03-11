package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

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
            when(fileImportOccurrenceCorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader()
                    .getResource("usagepoint_correct.csv")
                    .getPath()));
            when(fileImportOccurrenceIncorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader()
                    .getResource("usagepoint_incorrect.csv")
                    .getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessCorrectFile() throws IOException {

    }

    @Test
    public void testProcessIncorrectFile() throws IOException {
    }
}