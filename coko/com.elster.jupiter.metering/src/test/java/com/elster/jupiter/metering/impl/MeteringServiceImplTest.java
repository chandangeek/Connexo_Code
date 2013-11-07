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
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
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
    @Mock
    private Table table;
    @Mock
    private DataMapper<ReadingType> readingTypeFactory;
    @Mock
    private ReadingType readingType;
    @Mock
    private Column column1, column2;
    @Mock
    private CacheService cacheService;
    @Mock
    private ComponentCache componentCache;
    @Mock
    private TypeCache<ReadingType> readingTypeCache;
    @Mock
    private DataMapper<ServiceLocation> serviceLocationFactory;
    @Mock
    private ServiceLocation serviceLocation;
    @Mock
    private TypeCache<ServiceCategory> serviceCategoryTypeCache;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private IdsService idsService;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString())).thenReturn(table);
        when(table.addIntervalColumns(anyString())).thenReturn(Arrays.asList(column1, column2));
        when(dataModel.getDataMapper(ReadingType.class, ReadingTypeImpl.class, TableSpecs.MTR_READINGTYPE.name())).thenReturn(readingTypeFactory);
        when(dataModel.getDataMapper(ServiceLocation.class, ServiceLocationImpl.class, TableSpecs.MTR_SERVICELOCATION.name())).thenReturn(serviceLocationFactory);
        when(cacheService.createComponentCache(dataModel)).thenReturn(componentCache);
        when(componentCache.getTypeCache(ReadingType.class, ReadingTypeImpl.class, TableSpecs.MTR_READINGTYPE.name())).thenReturn(readingTypeCache);
        when(componentCache.getTypeCache(ServiceCategory.class, ServiceCategoryImpl.class, TableSpecs.MTR_SERVICECATEGORY.name())).thenReturn(serviceCategoryTypeCache);
        meteringService = new MeteringServiceImpl();
        Bus.setServiceLocator(meteringService);

        meteringService.setOrmService(ormService);
        meteringService.setCacheService(cacheService);
        meteringService.setIdsService(idsService);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(meteringService);
    }

    @Test
    public void testGetReadingType() {
        String mrID = "mrID";
        when(readingTypeCache.get(mrID)).thenReturn(Optional.of(readingType));

        assertThat(meteringService.getReadingType(mrID).isPresent()).isTrue();
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

        assertThat(meteringService.findServiceLocation(mrID).isPresent()).isTrue();
        assertThat(meteringService.findServiceLocation(mrID).get()).isEqualTo(serviceLocation);
    }

    @Test
    public void testFindServiceLocationById() {
        long id = 156L;
        when(serviceLocationFactory.get(id)).thenReturn(Optional.of(serviceLocation));

        assertThat(meteringService.findServiceLocation(id).isPresent()).isTrue();
        assertThat(meteringService.findServiceLocation(id).get()).isEqualTo(serviceLocation);
    }

    @Test
    public void testGetServiceCategory() {
        when(serviceCategoryTypeCache.get(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));

        assertThat(meteringService.getServiceCategory(ServiceKind.GAS).isPresent()).isTrue();
        assertThat(meteringService.getServiceCategory(ServiceKind.GAS).get()).isEqualTo(serviceCategory);
    }

    @Test
    public void testCreateOverrulingStorer() {
        ReadingStorer storer = meteringService.createOverrulingStorer();

        assertThat(storer).isInstanceOf(ReadingStorerImpl.class); // implementation specific, but saves us verifying the contract of the returned instance
        verify(idsService).createStorer(true);
    }

    @Test
    public void testCreateRegularStorer() {
        ReadingStorer storer = meteringService.createRegularStorer();

        assertThat(storer).isInstanceOf(ReadingStorerImpl.class); // implementation specific, but saves us verifying the contract of the returned instance
        verify(idsService).createStorer(false);
    }

    @Test
    public void testFindServiceLocationJournal() {
        long id = 156L;
        JournalEntry<ServiceLocation> journalEntry = new JournalEntry<>(new UtcInstant(1455245L), serviceLocation);
        when(serviceLocationFactory.getJournal(id)).thenReturn(Arrays.asList(journalEntry));

        assertThat(meteringService.findServiceLocationJournal(id))
                .hasSize(1)
                .contains(journalEntry);
    }


}
