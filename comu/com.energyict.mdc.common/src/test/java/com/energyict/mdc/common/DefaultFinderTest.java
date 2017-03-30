/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for DefaultFinder in combination with QueryParameters
 * @author bvn
 */
public class DefaultFinderTest extends JerseyTest {
    static DataModel dataModel;
    static QueryExecutor query;

    @BeforeClass
    public static void setUpClass() throws Exception {
        dataModel = mock(DataModel.class);
        query = mock(QueryExecutor.class);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        reset(query, dataModel);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(BogusResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(dataModel).to(DataModel.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(dataModel.query(Matchers.any())).thenReturn(query);
        when(query.select(Matchers.any(Condition.class), Matchers.any(Order[].class), anyBoolean(), Matchers.any(String[].class), anyInt(), anyInt())).thenReturn(Collections.emptyList());
    }

    @Test
    public void testQueryParameters() throws Exception {
        final Response response = target("/bogus/").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testQueryParametersPagedNoSorting() throws Exception {
        final Response response = target("/bogus/").queryParam("start",5).queryParam("limit", 100).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<Integer> startCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(query).select(Matchers.any(Condition.class), Matchers.any(Order[].class), anyBoolean(), Matchers.any(String[].class), startCaptor.capture(), indexCaptor.capture());
        assertThat(startCaptor.getValue()).isEqualTo(5+1); // start is 1 based in SQL
        assertThat(indexCaptor.getValue()).isEqualTo(100 + 5 + 1); // always ask for 1 more
    }

    @Test
    public void testQueryParametersSortedDescendingWithSingleSortFromQueryParameters() throws Exception {
        final Response response = target("/bogus/").queryParam("sort", "name").queryParam("dir", "DESC").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(query).select(Matchers.any(Condition.class), orderCaptor.capture());
        assertThat(orderCaptor.getValue().getName()).isEqualTo("name");
        assertThat(orderCaptor.getValue().ascending()).isEqualTo(false);
    }

    @Test
    public void testQueryParametersSortedOnThreeColumnBuildFromQueryParameters() throws Exception {
        final Response response = target("/bogus/").queryParam("sort",sorting("name", "DESC", "field2", "ASC", "field3", "DESC")).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(query).select(Matchers.any(Condition.class), orderCaptor.capture(), orderCaptor.capture(), orderCaptor.capture());
        assertThat(orderCaptor.getAllValues().get(0).getName()).isEqualTo("name");
        assertThat(orderCaptor.getAllValues().get(0).ascending()).isEqualTo(false);
        assertThat(orderCaptor.getAllValues().get(1).getName()).isEqualTo("field2");
        assertThat(orderCaptor.getAllValues().get(1).ascending()).isEqualTo(true);
        assertThat(orderCaptor.getAllValues().get(2).getName()).isEqualTo("field3");
        assertThat(orderCaptor.getAllValues().get(2).ascending()).isEqualTo(false);
    }

    @Test
    public void testQueryParametersSortedOnThreeColumnAndPagedFromQueryParameters() throws Exception {
        final Response response = target("/bogus/").queryParam("sort",sorting("name", "DESC", "field2", "ASC", "field3", "DESC")).queryParam("start",50).queryParam("limit", 100).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<Order[]> orderCaptor = ArgumentCaptor.forClass(Order[].class);
        ArgumentCaptor<Integer> startCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(query).select(Matchers.any(Condition.class), orderCaptor.capture(), anyBoolean(), Matchers.any(String[].class), startCaptor.capture(), indexCaptor.capture());
        assertThat(orderCaptor.getValue()[0].getName()).isEqualTo("name");
        assertThat(orderCaptor.getValue()[0].ascending()).isEqualTo(false);
        assertThat(orderCaptor.getValue()[1].getName()).isEqualTo("field2");
        assertThat(orderCaptor.getValue()[1].ascending()).isEqualTo(true);
        assertThat(orderCaptor.getValue()[2].getName()).isEqualTo("field3");
        assertThat(orderCaptor.getValue()[2].ascending()).isEqualTo(false);
        assertThat(startCaptor.getValue()).isEqualTo(50 + 1); // start is 1 based in SQL
        assertThat(indexCaptor.getValue()).isEqualTo(100 + 50 + 1); // always ask for 1 more
    }

    private String sorting(String property, String direction, String ...fields) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("[{\"property\":\"%s\",\"direction\":\"%s\"}", property, direction));
        for (int i=0; i<fields.length; i+=2) {
            stringBuilder.append(String.format(",{\"property\":\"%s\",\"direction\":\"%s\"}", fields[i], fields[i+1]));
        }
        stringBuilder.append("]");
        return URLEncoder.encode(stringBuilder.toString(), "UTF-8");
    }



}