/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchDomainExtensionSupportFinderTest {
    @Mock
    private DataModel dataModel;
    @Mock
    private Table<Object> table;
    @Mock
    private DataMapper<Object> dataMapper;
    @Mock
    private SearchDomain searchDomain;
    @Mock
    private Finder<?> domainFinder;

    @Test
    public void returnOriginalFinderIfNoExtensions() {
        doReturn(domainFinder).when(searchDomain).finderFor(anyListOf(SearchablePropertyCondition.class));

        Finder<?> finder = SearchDomainExtensionSupportFinder.getFinder(mock(OrmService.class), searchDomain, Collections.emptyList());
        assertThat(finder).isEqualTo(domainFinder);
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
        when(ormService.getDataModels()).thenReturn(Collections.singletonList(dataModel));
        doReturn(Object.class).when(searchDomain).getDomainClass();
        when(table.maps(Object.class)).thenReturn(true);
        Column primaryColumn1 = mockColumn("id1");
        Column primaryColumn2 = mockColumn("id2");
        doReturn(Arrays.asList(primaryColumn1, primaryColumn2)).when(table).getPrimaryKeyColumns();
        doReturn(Collections.singletonList(table)).when(dataModel).getTables(Version.latest());
        when(dataMapper.getQueryFields()).thenReturn(ImmutableSet.of("id1", "id2", "mrid", "name"));
        when(dataModel.mapper(Object.class)).thenReturn(dataMapper);

        List<SearchablePropertyCondition> conditions = Arrays.asList(condition1, condition2);
        doReturn(domainFinder).when(searchDomain).finderFor(anyListOf(SearchablePropertyCondition.class));
        when(domainFinder.asFragment(anyVararg())).thenAnswer(invocation -> new SqlBuilder("select "
                + Arrays.stream(invocation.getArguments())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
                + " from table"));
        String idColumns = table.getPrimaryKeyColumns().stream()
                .map(Column::getName)
                .collect(Collectors.joining(", "));
        when(domainExtension.asFragment(conditions)).thenReturn(new SqlBuilder("select "
                + idColumns
                + " from domain_table where condition1 = value1 and condition2 = value2"));

        SearchDomainExtensionSupportFinder finder = (SearchDomainExtensionSupportFinder) SearchDomainExtensionSupportFinder.getFinder(ormService, searchDomain, conditions);
        String text = finder.paged(1, 10).sorted("name", false).asFragment("id1", "name").getText();
        assertThat(text).isEqualTo("select * from " +
                "(select x.*, ROWNUM rnum from (select id1, name from " +
                "(select id1, id2, mrid, name from table) SD " /* original search domain query */ +
                "where (SD.id1, SD.id2) IN " /* adding extension query */ +
                "(select id1, id2 from domain_table where condition1 = value1 and condition2 = value2) " /* extension query */ +
                "order by name DESC  ) " + /* ordering */
                "x where ROWNUM <=  ? ) where rnum >=  ? ");
    }

    private static Column mockColumn(String name) {
        Column column = mock(Column.class);
        when(column.getName()).thenReturn(name);
        return column;
    }
}
