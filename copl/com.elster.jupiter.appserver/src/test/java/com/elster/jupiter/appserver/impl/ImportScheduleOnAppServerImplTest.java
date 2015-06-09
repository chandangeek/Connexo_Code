package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleOnAppServerImplTest {

    @Mock
    private AppServer appServer;
    @Mock
    protected ImportSchedule importSchedule;
    @Mock
    private DataMapper<ImportScheduleOnAppServer> importScheduleOnAppServerFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private FileImportService fileImportService;

    @Before
    public void setUp() {
        when(dataModel.mapper(ImportScheduleOnAppServer.class)).thenReturn(importScheduleOnAppServerFactory);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetImportSchedule() {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(dataModel, fileImportService, importSchedule, appServer);

        assertThat(importScheduleOnAppServer.getImportSchedule().get()).isEqualTo(importSchedule);
    }

    @Test
    public void testGetAppServer() {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(dataModel, fileImportService, importSchedule, appServer);

        assertThat(importScheduleOnAppServer.getAppServer()).isEqualTo(appServer);
    }

    @Test
    public void testSave() {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(dataModel, fileImportService, importSchedule, appServer);

        importScheduleOnAppServer.save();

        verify(importScheduleOnAppServerFactory).persist(importScheduleOnAppServer);
    }

}
