package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchCriteria;
import com.elster.jupiter.search.SearchCriteriaService;
import com.elster.jupiter.upgrade.UpgradeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.search.SearchCriteriaService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchCriteriaServiceImplTest {
    @Mock
    private OrmService ormService;
    @Mock
    DataModel dataModel;
    @Mock
    DataMapper<SearchCriteria> searchCriteriaDataMapper;

    @Test
    public void testSearchCriteria(){
        when(ormService.newDataModel(COMPONENT_NAME, "search_criteria")).thenReturn(dataModel);
        when(dataModel.mapper(SearchCriteria.class)).thenReturn(searchCriteriaDataMapper);

        SearchCriteriaServiceImpl searchCriteriaService = this.getTestInstance();
        searchCriteriaService.setOrmService(ormService);
        SearchCriteriaBuilder searchCriteriaBuilder =  searchCriteriaService.newSearchCriteria();

        assertThat(searchCriteriaBuilder).isNotEqualTo(null);

    }
    private SearchCriteriaServiceImpl getTestInstance() {
        return new SearchCriteriaServiceImpl();
        /*return new SearchCriteriaServiceImpl(this.ormService, this.queryService, this.nlsService, this.upgradeService,
        this.clock);*/
    }

}
