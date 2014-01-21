package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeImplTest extends EqualsContractTest {

    private static final String MRID = "0.0.7.4.0.8.2.8.16.9.11.0.0.0.0.0.0.0";
    private static final String MRID2 = "0.0.7.1.0.8.2.9.16.9.110.0.0.0.0.0.0.0";
    private static final String MRID3 = "0.0.7.1.0.8.2.8.16.9.11.0.0.0.0.0.0.0";
    private static final String ALIAS = "alias";

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;

    private ReadingTypeImpl readingType;


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
}
