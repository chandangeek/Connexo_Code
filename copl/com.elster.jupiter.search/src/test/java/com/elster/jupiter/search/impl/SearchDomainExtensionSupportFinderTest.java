package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchDomainExtensionSupportFinderTest {
    @Mock
    private DataModel dataModel;
    @Mock
    private SearchDomain searchDomain;
    @Mock
    private Finder<?> domainFinder;


    @Test
    public void returnOriginalFinderIfNoExtensions() {
        doReturn(this.domainFinder).when(this.searchDomain).finderFor(anyList());

        Finder<?> finder = SearchDomainExtensionSupportFinder.getFinder(mock(OrmService.class), this.searchDomain, Collections.emptyList());
        assertThat(finder).isEqualTo(this.domainFinder);
    }

    @Test
    public void testAsFragment() {
        SearchDomainExtension domainExtension = mock(SearchDomainExtension.class);

        SearchablePropertyCondition condition1 = mock(SearchablePropertyCondition.class);
        SearchDomainExtensionSearchableProperty property1 = mock(SearchDomainExtensionSearchableProperty.class);
        when(condition1.getProperty()).thenReturn(property1);
        when(property1.getDomainExtension()).thenReturn(domainExtension);

        SearchablePropertyCondition condition2 = mock(SearchablePropertyCondition.class);
        SearchDomainExtensionSearchableProperty property2 = mock(SearchDomainExtensionSearchableProperty.class);
        when(condition2.getProperty()).thenReturn(property2);
        when(property2.getDomainExtension()).thenReturn(domainExtension);

        OrmService ormService = mock(OrmService.class);
        when(ormService.getDataModels()).thenReturn(Collections.singletonList(this.dataModel));
        doReturn(Object.class).when(this.searchDomain).getDomainClass();
        Table table = mock(Table.class);
        when(table.maps(Object.class)).thenReturn(true);
        Column primaryColumn = mock(Column.class);
        when(primaryColumn.getName()).thenReturn("id");
        when(table.getPrimaryKeyColumns()).thenReturn(Collections.singletonList(primaryColumn));
        doReturn(Collections.singletonList(table)).when(this.dataModel).getTables();

        List<SearchablePropertyCondition> conditions = Arrays.asList(condition1, condition2);
        doReturn(this.domainFinder).when(this.searchDomain).finderFor(anyList());
        when(this.domainFinder.asFragment("*")).thenReturn(new SqlBuilder("select * from table"));
        when(domainExtension.asFragment(conditions)).thenReturn(new SqlBuilder("select id from domain_table where condition1 = value1 and condition2 = value2"));

        SearchDomainExtensionSupportFinder finder = (SearchDomainExtensionSupportFinder) SearchDomainExtensionSupportFinder.getFinder(ormService, this.searchDomain, conditions);
        String text = finder.paged(1, 10).sorted("name", false).asFragment("id", "name").getText();
        assertThat(text).isEqualTo("select * from " +
                "(select x.*, ROWNUM rnum from (select id, name from " +
                "(select * from table) sd " /* original search domain query */ +
                "where (sd.id) IN " /* adding extension query */ +
                "(select id from domain_table where condition1 = value1 and condition2 = value2) " /* extension query */ +
                "order by name DESC  ) " + /* ordering */
                "x where ROWNUM <=  ? ) where rnum >=  ? ");
    }
}
