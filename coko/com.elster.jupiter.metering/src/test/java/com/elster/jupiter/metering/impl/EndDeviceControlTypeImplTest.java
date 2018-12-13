/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceControlTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;

import com.google.common.collect.ImmutableList;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceControlTypeImplTest extends EqualsContractTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private EndDeviceControlTypeImpl instanceA;

    @Test
    public void testPersist() throws SQLException {
        final ServerMeteringService meteringService = getMeteringService();
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                String code = EndDeviceControlTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                        .domain(EndDeviceDomain.RCDSWITCH)
                        .subDomain(EndDeviceSubDomain.NA)
                        .eventOrAction(EndDeviceEventOrAction.CLOSE)
                        .toCode();
                EndDeviceControlTypeImpl endDeviceControlType = meteringService.createEndDeviceControlType(code);
                Optional<EndDeviceControlType> found = meteringService.getDataModel()
                        .mapper(EndDeviceControlType.class)
                        .getOptional(code);
                assertThat(found.get()).isEqualTo(endDeviceControlType);
            }
        });
    }

    private ServerMeteringService getMeteringService() {
        return inMemoryBootstrapModule.getMeteringService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    private EndDeviceControlTypeImpl createEndDeviceControlType() {
        return new EndDeviceControlTypeImpl(getMeteringService().getDataModel(), getMeteringService().getThesaurus());
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = createEndDeviceControlType().init(EndDeviceControlTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                    .domain(EndDeviceDomain.RCDSWITCH)
                    .subDomain(EndDeviceSubDomain.NA)
                    .eventOrAction(EndDeviceEventOrAction.CLOSE)
                    .toCode());
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return createEndDeviceControlType().init(EndDeviceControlTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.RCDSWITCH)
                .subDomain(EndDeviceSubDomain.NA)
                .eventOrAction(EndDeviceEventOrAction.CLOSE)
                .toCode());
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                createEndDeviceControlType().init(EndDeviceControlTypeCodeBuilder.type(EndDeviceType.GAS_METER)
                        .domain(EndDeviceDomain.BATTERY)
                        .subDomain(EndDeviceSubDomain.CHARGE)
                        .eventOrAction(EndDeviceEventOrAction.DECREASED)
                        .toCode()),
                createEndDeviceControlType().init(EndDeviceControlTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                        .domain(EndDeviceDomain.CLOCK)
                        .subDomain(EndDeviceSubDomain.CHARGE)
                        .eventOrAction(EndDeviceEventOrAction.DECREASED)
                        .toCode()),
                createEndDeviceControlType().init(EndDeviceControlTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                        .domain(EndDeviceDomain.BATTERY)
                        .subDomain(EndDeviceSubDomain.TIME)
                        .eventOrAction(EndDeviceEventOrAction.DECREASED)
                        .toCode()),
                createEndDeviceControlType().init(EndDeviceControlTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                        .domain(EndDeviceDomain.BATTERY)
                        .subDomain(EndDeviceSubDomain.CHARGE)
                        .eventOrAction(EndDeviceEventOrAction.INCREASED)
                        .toCode())
        );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}

