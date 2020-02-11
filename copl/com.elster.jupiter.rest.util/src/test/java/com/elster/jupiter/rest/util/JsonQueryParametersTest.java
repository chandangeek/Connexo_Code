package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonQueryParametersTest {

    private String sort = "sort";
    private String dir = "dir";

    @Mock
    public UriInfo uriInfo;

    @Test
    public void testSqlInjectionOnSortParamWithDirection() {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        String field_name = "FIELD_NAME";
        params.putSingle(sort, field_name);
        params.putSingle(dir, "ASC");
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(params);

        List<Order> sortingColumns = new JsonQueryParameters(uriInfo).getSortingColumns();
        Assert.assertEquals(1, sortingColumns.size());
        Assert.assertEquals(Order.ascending(field_name), sortingColumns.get(0));
    }

    @Test
    public void testSqlInjectionOnSortParams() {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        String field1 = "FIELD_NAME1";
        String field2 = "FIELD_NAME2";
        params.putSingle(sort, "[{property:" + field1 + ",direction:DESC},{ property:" + field2 + " , direction:ASC}]");
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(params);

        List<Order> sortingColumns = new JsonQueryParameters(uriInfo).getSortingColumns();
        Assert.assertEquals(2, sortingColumns.size());
        Assert.assertEquals(Order.descending(field1), sortingColumns.get(0));
        Assert.assertEquals(Order.ascending(field2), sortingColumns.get(1));
    }

    @Test(expected = RuntimeException.class)
    public void testSqlInjectionOnSortParamFails() {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle( sort, "DROP TABLE X");
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(params);

        new JsonQueryParameters(uriInfo).getSortingColumns();
    }

    @Test
    public void testNoSortParam() {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(params);

        List<Order> sortingColumns = new JsonQueryParameters(uriInfo).getSortingColumns();
        Assert.assertEquals(0, sortingColumns.size());
    }


}
