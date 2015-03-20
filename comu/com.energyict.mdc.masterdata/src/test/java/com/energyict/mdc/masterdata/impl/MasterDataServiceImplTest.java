package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.RegisterType;
import java.util.List;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 3/10/15.
 */
public class MasterDataServiceImplTest extends PersistenceTest {

    @Test
    public void testGetAllRegisterTypesIsSortedByReadingTypesFullAliasName() throws Exception {
        List<RegisterType> registerTypes = inMemoryPersistence.getMasterDataService().findAllRegisterTypes().find();
        assertThat(registerTypes).isNotEmpty();
        assertThat(registerTypes).isSortedAccordingTo((a,b)->a.getReadingType().getFullAliasName().toUpperCase().compareTo(b.getReadingType().getFullAliasName().toUpperCase()));
    }

    @Test
    public void testGetAllRegisterTypesSupportsPaging() throws Exception {
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(10);
        when(queryParameters.getStart()).thenReturn(10);
        List<RegisterType> registerTypes = inMemoryPersistence.getMasterDataService().findAllRegisterTypes().from(queryParameters).find();
        assertThat(registerTypes).hasSize(11);
    }
}
