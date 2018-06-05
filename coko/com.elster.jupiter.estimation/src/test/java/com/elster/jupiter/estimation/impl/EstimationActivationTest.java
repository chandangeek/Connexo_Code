/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EstimationActivationTest {
    protected EstimationServiceImpl estimationService;

    @Mock
    protected ChannelsContainer channelsContainer;
    @Mock
    protected EstimationRuleSet estimationRuleSet;
    @Mock
    protected DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected Table table;
    @Mock
    protected QueryExecutor query;
    @Mock
    protected OrmService ormService;
    @Mock
    protected ValidatorFactory validatorFactory;
    @Mock
    protected Validator javaxValidator;


    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.query(ChannelsContainerEstimation.class)).thenReturn(query);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);

        this.estimationService = new EstimationServiceImpl(
                mock(MeteringService.class),
                mock(MetrologyConfigurationService.class),
                ormService,
                mock(QueryService.class),
                mock(NlsService.class),
                mock(EventService.class),
                mock(TaskService.class),
                mock(MeteringGroupsService.class),
                mock(MessageService.class),
                mock(TimeService.class),
                mock(UserService.class),
                mock(UpgradeService.class),
                mock(Clock.class));
    }

}
