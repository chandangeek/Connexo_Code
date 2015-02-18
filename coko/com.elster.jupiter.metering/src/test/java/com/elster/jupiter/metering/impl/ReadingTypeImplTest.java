package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeImplTest extends EqualsContractTest {

    private static final String MRID = "0.0.7.4.0.8.2.8.16.9.11.0.0.0.0.0.0.0";
    private static final String MRID2 = "0.0.7.1.0.8.2.9.16.9.110.0.0.0.0.0.0.0";
    private static final String MRID3 = "0.0.7.1.0.8.2.8.16.9.11.0.0.0.0.0.0.0";
    private static final String ALIAS = "alias";


    private final String plainAlias = "PlainAlias";
    private final String aliasWithMacroPeriod = "[Daily] PlainAlias";
    private final String aliasWithMeasuringPeriod = "[15-minute] PlainAlias";
    private final String aliasWithUnit = "PlainAlias (Wh)";
    private final String aliasWithPhase = "PlainAlias Phase-A";
    private final String aliasWithTOU = "PlainAlias ToU 3";
    private final String aliasWithAll = "[Monthly] PlainAlias (kWh) Phase-B ToU 3";

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;

    private ReadingTypeImpl readingType;


    @Before
    public void initBefore() {
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            return invocationOnMock.getArguments()[1];  // return the default value
        });
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        if (readingType == null) {
            readingType = new ReadingTypeImpl(dataModel, thesaurus).init(MRID, ALIAS);
        }
        return readingType;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ReadingTypeImpl(dataModel, thesaurus).init(MRID, ALIAS);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new ReadingTypeImpl(dataModel, thesaurus).init(MRID2, ALIAS));
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Before
    public void setUp() {
        when(dataModel.getInstance(ReadingTypeImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeImpl(dataModel, thesaurus);            }
        });
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetMRID() {
        assertThat(readingType.getMRID()).isEqualTo(MRID);
    }

    @Test
    public void testGetAliasName() {
        assertThat(readingType.getAliasName()).isEqualTo(ALIAS);
    }

    @Test
    public void testIsBulkQuantityReadingType() {
    	ReadingTypeImpl bulkReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(MRID3,"3");
        assertThat(readingType.isBulkQuantityReadingType(bulkReadingType)).isTrue();
    }

    @Test
    public void testCurrency() {
    	assertThat(ReadingTypeImpl.getCurrency(0, thesaurus)).isEqualTo(Currency.getInstance("XXX"));
    	assertThat(ReadingTypeImpl.getCurrency(999, thesaurus)).isEqualTo(Currency.getInstance("XXX"));
    	assertThat(ReadingTypeImpl.getCurrency(978, thesaurus)).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    public void testSelfIsBulkQuantityReadingType(){
        ReadingTypeImpl bulkReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2, ALIAS);
        assertThat(bulkReadingType.isCumulative()).isTrue();
    }

    @Test
    public void testSelfIsNotBulkQuantityReadingType(){
        ReadingTypeImpl bulkReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(MRID, ALIAS);
        assertThat(bulkReadingType.isCumulative()).isFalse();
    }

    @Test
    public void testUnexistingCalculatedReadingType(){
        ReadingTypeImpl bulkReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(MRID, ALIAS);
        assertThat(bulkReadingType.getCalculatedReadingType().isPresent()).isFalse();
    }

    @Test
    public void testCalculatedReadingType(){
        String expectedDeltaReadingTypeCode = "0.0.7.4.0.8.2.9.16.9.110.0.0.0.0.0.0.0";
        ReadingTypeImpl bulkReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2, ALIAS);
        ReadingType calcReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(expectedDeltaReadingTypeCode, "Calculated");
        DataMapper<ReadingType> mapper = mock(DataMapper.class);
        when(dataModel.mapper(ReadingType.class)).thenReturn(mapper);
        when(mapper.getOptional(expectedDeltaReadingTypeCode)).thenReturn(Optional.of(calcReadingType));
        Optional<ReadingType> calculatedReadingType = bulkReadingType.getCalculatedReadingType();
        assertThat(calculatedReadingType.isPresent()).isTrue();
        assertThat(calculatedReadingType.get().getMRID()).isEqualTo(expectedDeltaReadingTypeCode);
    }

    private ReadingType mockSimpleReadingTypeWithPlainAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.0.0", plainAlias);
    }

    private ReadingType mockReadingTypeWithDailyPeriodAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.0.0", plainAlias);

    }

    private ReadingType mockReadingTypeWith15MinPeriodAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.0.0", plainAlias);

    }

    private ReadingType mockReadingTypeWithUnitAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", plainAlias);

    }

    private ReadingType mockReadingTypeWithPhaseAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("0.0.0.4.1.1.12.0.0.0.0.0.0.0.128.0.0.0", plainAlias);

    }

    private ReadingType mockReadingTypeWithTOUAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("0.0.0.4.1.1.12.0.0.0.0.3.0.0.0.0.0.0", plainAlias);

    }
    private ReadingType mockReadingTypeWithAllAlias() {
        return new ReadingTypeImpl(dataModel, thesaurus).init("13.0.0.4.1.1.12.0.0.0.0.3.0.0.64.3.72.0", plainAlias);
    }

    @Test
    public void simpleReadingTypeWithPlainAliasTest() {
        ReadingType readingType = mockSimpleReadingTypeWithPlainAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(plainAlias);
    }

    @Test
    public void readingTypeWithDailyPeriodTest() {
        ReadingType readingType = mockReadingTypeWithDailyPeriodAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(aliasWithMacroPeriod);
    }

    @Test
    public void readingTypeWith15MinTest() {
        ReadingType readingType = mockReadingTypeWith15MinPeriodAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(aliasWithMeasuringPeriod);
    }

    @Test
    public void readingTypeWithUnitTest() {
        ReadingType readingType = mockReadingTypeWithUnitAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(aliasWithUnit);
    }

    @Test
    public void readingTypeWithPhaseTest() {
        ReadingType readingType = mockReadingTypeWithPhaseAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(aliasWithPhase);
    }

    @Test
    public void readingTypeWithTOUTest() {
        ReadingType readingType = mockReadingTypeWithTOUAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(aliasWithTOU);
    }

    @Test
    public void readingTypeWithAllTest() {
        ReadingType readingType = mockReadingTypeWithAllAlias();
        assertThat(readingType.getFullAliasName()).isEqualTo(aliasWithAll);
    }
}
