/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionWrapperTest extends JerseyTest {
    static TransactionService transactionService;
    static TransactionContext context;
    static DataModel dataModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
        transactionService = mock(TransactionService.class);
        context = mock(TransactionContext.class);
        dataModel = mock(DataModel.class);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        reset(dataModel, context, transactionService);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(TransactionResource.class, TransactionWrapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(transactionService).to(TransactionService.class);
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
        when(transactionService.getContext()).thenReturn(context);
    }

    @Test
    public void testTransactional() throws Exception {
        final Response response = target("/wrapper/transactional").request().get(Response.class);
        verify(transactionService, times(1)).getContext();
        verify(context, times(1)).commit();
        verify(context, times(1)).close();
    }

    @Test
    public void testUnTransactional() throws Exception {
        final Response response = target("/wrapper/notransaction").request().get(Response.class);
        verify(transactionService, never()).getContext();
        verify(context, never()).commit();
        verify(context, never()).close();
    }

    @Test
    public void testWithError() throws Exception {
        final Response response = target("/wrapper/exception").request().get(Response.class);
        verify(transactionService, times(1)).getContext();
        verify(context, never()).commit();
        verify(context, times(1)).close();
    }

    @Test
    public void testNotFound() throws Exception {
        final Response response = target("/wrapper/does_not_exist").request().get(Response.class); // will be 404
        verify(transactionService, never()).getContext();
        verify(context, never()).commit();
        verify(context, never()).close();
    }



}