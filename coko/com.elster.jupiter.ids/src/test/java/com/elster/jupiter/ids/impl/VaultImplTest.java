package com.elster.jupiter.ids.impl;

import org.mockito.Mock;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.SqlDialect;

import java.time.Clock;

import com.google.common.collect.ImmutableList;
import javax.inject.Provider;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class VaultImplTest extends EqualsContractTest {

    private static long ID = 15L;
    private static int SLOT_COUNT = 54;
    private static String COMPONENT_NAME = "CMP";
    private static String DESCRIPTION = "description";

    private DataModel dataModel;
    @Mock
    private Clock clock;
    @Mock
    private Provider<TimeSeriesImpl> provider;
    
    private Object a;
    
    @Override
    protected Object getInstanceA() {
    	if (a == null) {
    		dataModel = mock(DataModel.class);
    		when(dataModel.getSqlDialect()).thenReturn(SqlDialect.H2);
    		a = new VaultImpl(dataModel, provider).init(COMPONENT_NAME,ID,DESCRIPTION,SLOT_COUNT,0,true);
    	}
    	return a;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new VaultImpl(dataModel, provider).init(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, 0,true);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new VaultImpl(dataModel ,provider).init(COMPONENT_NAME, ID + 1, DESCRIPTION, SLOT_COUNT,0, true));
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return new SubVault(dataModel,clock,provider).init(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, 0,true);
    }
    
    private class SubVault extends VaultImpl {

		SubVault(DataModel dataModel, Clock clock,Provider<TimeSeriesImpl> timeSeriesProvider) {
			super(dataModel, timeSeriesProvider);
		}
    	
    }
}
