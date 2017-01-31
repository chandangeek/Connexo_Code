/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import oracle.AQ.AQException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class QueueTableSpecImplTest {

    private static final String NAME = "name";
    private static final String PAYLOAD_TYPE = "SYS.AQ$_JMS_RAW";

    private TransientQueueTableSpec queueTableSpec;

    @Mock
    private DataMapper<QueueTableSpec> queueTableSpecFactory;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() throws SQLException, JMSException {
        queueTableSpec = TransientQueueTableSpec.createQueue(thesaurus, NAME, PAYLOAD_TYPE);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetName() {
        assertThat(queueTableSpec.getName()).isEqualTo(NAME);
    }

    @Test
    public void testActivateJms() throws JMSException, AQException {
        queueTableSpec.activate();

        assertThat(queueTableSpec.isActive()).isTrue();
    }

    @Test
    public void testDeactivate() throws SQLException {
        queueTableSpec.activate();

        assertThat(queueTableSpec.isActive()).isTrue();

        queueTableSpec.deactivate();

        assertThat(queueTableSpec.isActive()).isFalse();

    }

}
