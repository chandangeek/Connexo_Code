package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleOnAppServerImplTest {

    @Mock
    private AppServer appServer;
    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private DataMapper<ImportScheduleOnAppServer> importScheduleOnAppServerFactory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;

    @Before
    public void setUp() {
        when(serviceLocator.getOrmClient().getImportScheduleOnAppServerFactory()).thenReturn(importScheduleOnAppServerFactory);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }
    @Test
    public void testGetImportSchedule() {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = new ImportScheduleOnAppServerImpl(importSchedule, appServer);

        assertThat(importScheduleOnAppServer.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test
    public void testGetAppServer() {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = new ImportScheduleOnAppServerImpl(importSchedule, appServer);

        assertThat(importScheduleOnAppServer.getAppServer()).isEqualTo(appServer);
    }

    @Test
    public void testSave() {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = new ImportScheduleOnAppServerImpl(importSchedule, appServer);

        importScheduleOnAppServer.save();

        verify(importScheduleOnAppServerFactory).persist(importScheduleOnAppServer);
    }

}
