package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeteringServiceImplTest {

    private MeteringServiceImpl meteringService;

    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataMapper<ReadingType> readingTypeFactory;
    @Mock
    private ReadingType readingType;
    @Mock
    private Column column1, column2;
    @Mock
    private DataMapper<ServiceLocation> serviceLocationFactory;
    @Mock
    private ServiceLocation serviceLocation;
    @Mock
    private DataMapper<ServiceCategory> serviceCategoryTypeCache;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private IdsService idsService;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(table.addIntervalColumns(anyString())).thenReturn(Arrays.asList(column1, column2));
        when(dataModel.mapper(ReadingType.class)).thenReturn(readingTypeFactory);
        when(dataModel.mapper(ServiceLocation.class)).thenReturn(serviceLocationFactory);
        when(dataModel.mapper(ServiceCategory.class)).thenReturn(serviceCategoryTypeCache);
        meteringService = new MeteringServiceImpl();

        meteringService.setOrmService(ormService);
        meteringService.setIdsService(idsService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetReadingType() {
        String mrID = "mrID";
        when(readingTypeFactory.getOptional(mrID)).thenReturn(Optional.of(readingType));

        assertThat(meteringService.getReadingType(mrID).get()).isEqualTo(readingType);
    }

    @Test
    public void testNewServiceLocation() {
        ServiceLocation serviceLocation = meteringService.newServiceLocation();

        assertThat(serviceLocation).isInstanceOf(ServiceLocationImpl.class); // implementation specific, but saves us verifying the contract of the returned instance
    }

    @Test
    public void testFindServiceLocationByMrid() {
        String mrID = "mrID";
        when(serviceLocationFactory.getUnique("mRID", mrID)).thenReturn(Optional.of(serviceLocation));

        assertThat(meteringService.findServiceLocation(mrID).get()).isEqualTo(serviceLocation);
    }

    @Test
    public void testFindServiceLocationById() {
        long id = 156L;
        when(serviceLocationFactory.getOptional(id)).thenReturn(Optional.of(serviceLocation));

        assertThat(meteringService.findServiceLocation(id).get()).isEqualTo(serviceLocation);
    }

    @Test
    public void testGetServiceCategory() {
        when(serviceCategoryTypeCache.getOptional(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));

        assertThat(meteringService.getServiceCategory(ServiceKind.GAS).get()).isEqualTo(serviceCategory);
    }

    @Test
    public void testCreateOverrulingStorer() {
        ReadingStorer storer = meteringService.createOverrulingStorer();

        assertThat(storer).isInstanceOf(ReadingStorerImpl.class); // implementation specific, but saves us verifying the contract of the returned instance
        verify(idsService).createOverrulingStorer();
    }

    @Test
    public void testCreateRegularStorer() {
        ReadingStorer storer = meteringService.createNonOverrulingStorer();

        assertThat(storer).isInstanceOf(ReadingStorerImpl.class); // implementation specific, but saves us verifying the contract of the returned instance
        verify(idsService).createNonOverrulingStorer();
    }

    @Test
    public void testFindServiceLocationJournal() {
        long id = 156L;
        JournalEntry<ServiceLocation> journalEntry = new JournalEntry<>(Instant.ofEpochMilli(1455245L), serviceLocation);
        when(serviceLocationFactory.getJournal(id)).thenReturn(Arrays.asList(journalEntry));

        assertThat(meteringService.findServiceLocationJournal(id))
                .hasSize(1)
                .contains(journalEntry);
    }


}
