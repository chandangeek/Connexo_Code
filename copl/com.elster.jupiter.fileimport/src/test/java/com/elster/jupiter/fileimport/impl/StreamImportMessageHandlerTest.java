package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamImportMessageHandlerTest {

    private static final byte[] PAYLOAD = "PAYLOAD".getBytes();
    private static final long FILE_IMPORT_ID = 17L;
    private StreamImportMessageHandler streamImportMessageHandler;

    private FileImportMessage fileImportMessage;

    @Mock
    private FileImporter fileImporter;
    @Mock
    private Message message;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private JsonService jsonService;
    @Mock
    private FileImport fileImport;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrmClient ormClient;

    @Before
    public void setUp() {
        when(fileImport.getId()).thenReturn(FILE_IMPORT_ID);
        fileImportMessage = new FileImportMessage(fileImport);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(message.getPayload()).thenReturn(PAYLOAD);
        when(jsonService.deserialize(aryEq(PAYLOAD), eq(FileImportMessage.class))).thenReturn(fileImportMessage);
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getFileImportFactory().get(FILE_IMPORT_ID)).thenReturn(Optional.of(fileImport));


        streamImportMessageHandler = new StreamImportMessageHandler(fileImporter);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testProcessPassesFileImportToFileImporter() {

        streamImportMessageHandler.process(message);

        verify(fileImporter).process(fileImport);
    }

}
