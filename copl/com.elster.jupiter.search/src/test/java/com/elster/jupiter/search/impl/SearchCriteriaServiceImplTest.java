package com.elster.jupiter.search.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchCriteria;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.search.SearchCriteriaService.COMPONENT_NAME;
import static com.elster.jupiter.search.SearchCriteriaService.SearchCriteriaBuilder;
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
