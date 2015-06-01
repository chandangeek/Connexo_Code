package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.FileSystem;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailDestinationImplTest {

    public static final String DATA1 = "blablablablabla1";
    public static final String DATA2 = "blablablablabla2";

    private FileSystem fileSystem;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private AppService appService;
    @Mock
    private FormattedExportData data1, data2;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(dataModel.getInstance(EmailDestinationImpl.class)).thenAnswer(invocation -> new EmailDestinationImpl(dataModel, thesaurus, dataExportService, appService, fileSystem, mailService));
        when(data1.getAppendablePayload()).thenReturn(DATA1);
        when(data2.getAppendablePayload()).thenReturn(DATA2);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSend() throws Exception {
        EmailDestinationImpl.from(dataModel, "EmailDestinationImplTest@mailinator.com", "test", "file", "txt");

        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, thesaurus, dataExportService, appService, fileSystem, mailService);

        emailDestination.send(Arrays.asList(data1, data2));
    }
}