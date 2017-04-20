/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationContextImpl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataValidationTaskExecutorTest {
    private static final ZonedDateTime START_DATE = ZonedDateTime.now(ZoneId.systemDefault());
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private ValidationServiceImpl validationService;
    @Mock
    private Clock clock;
    @Mock
    private User user;
    @Mock
    private DataValidationTask task;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private DataValidationOccurrence dataValidationOccurrence;

    private DataValidationTaskExecutor executor;
    private MetrologyPurpose purpose1, purpose2;
    private List<MetrologyContract> contracts, contractsForPurpose1, contractsForPurpose2;
    private List<ChannelsContainer> mdcChannelsContainers, mdmChannelsContainers,
            mdmChannelsContainersForPurpose1, mdmChannelsContainersForPurpose2;

    @Before
    public void setUp() {
        when(validationService.createValidationOccurrence(taskOccurrence)).thenReturn(dataValidationOccurrence);
        when(validationService.findDataValidationOccurrence(taskOccurrence)).thenReturn(Optional.of(dataValidationOccurrence));
        when(validationService.findAndLockDataValidationOccurrence(taskOccurrence)).thenReturn(dataValidationOccurrence);
        when(taskOccurrence.createTaskLogHandler()).thenReturn(() -> new LogRecorder(Level.OFF));
        when(dataValidationOccurrence.getTask()).thenReturn(task);
        when(dataValidationOccurrence.getStartDate()).thenReturn(Optional.of(START_DATE.toInstant()));
        when(threadPrincipalService.getLocale()).thenReturn(Locale.CHINA);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[1]).run();
            return null;
        }).when(threadPrincipalService).runAs(eq(user), any(Runnable.class), any(Locale.class));
        executor = new DataValidationTaskExecutor(validationService, transactionService,
                thesaurus, threadPrincipalService, clock, user);
    }

    @Test
    public void testExecuteMdcTask() {
        setUpMdcTask("MDC1", "MDC2");

        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);

        mdcChannelsContainers.forEach(container -> verify(validationService).validate(
                refEq(new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDC), container))));
    }

    @Test
    public void testExecuteMdmTask() {
        setUpMdmTask("MDM1", "MDM2");
        assertThat(mdmChannelsContainers).hasSize(contracts.size());
        assertThat(mdmChannelsContainersForPurpose1).hasSize(contractsForPurpose1.size());
        assertThat(mdmChannelsContainersForPurpose2).hasSize(contractsForPurpose2.size());
        when(task.getMetrologyPurpose()).thenReturn(Optional.empty());

        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);

        for (int i = 0; i < contracts.size(); ++i) {
            verify(validationService).validate(
                    refEq(new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDM),
                            mdmChannelsContainers.get(i), contracts.get(i))));
        }
    }

    @Test
    public void testExecuteMdmTaskWithPurpose1() {
        setUpMdmTask("MDM1", "MDM2");
        assertThat(mdmChannelsContainers).hasSize(contracts.size());
        assertThat(mdmChannelsContainersForPurpose1).hasSize(contractsForPurpose1.size());
        assertThat(mdmChannelsContainersForPurpose2).hasSize(contractsForPurpose2.size());
        when(task.getMetrologyPurpose()).thenReturn(Optional.of(purpose1));

        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);

        for (int i = 0; i < contractsForPurpose1.size(); ++i) {
            verify(validationService).validate(
                    refEq(new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDM),
                            mdmChannelsContainersForPurpose1.get(i), contractsForPurpose1.get(i))));
        }
    }

    @Test
    public void testExecuteMdmTaskWithPurpose2() {
        setUpMdmTask("MDM1", "MDM2");
        assertThat(mdmChannelsContainers).hasSize(contracts.size());
        assertThat(mdmChannelsContainersForPurpose1).hasSize(contractsForPurpose1.size());
        assertThat(mdmChannelsContainersForPurpose2).hasSize(contractsForPurpose2.size());
        when(task.getMetrologyPurpose()).thenReturn(Optional.of(purpose2));

        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);

        for (int i = 0; i < contractsForPurpose2.size(); ++i) {
            verify(validationService).validate(
                    refEq(new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDM),
                            mdmChannelsContainersForPurpose2.get(i), contractsForPurpose2.get(i))));
        }
    }

    private void setUpMdcTask(String... deviceNames) {
        AmrSystem amrSystem = mock(AmrSystem.class);
        List<Meter> meters = Arrays.stream(deviceNames)
                .map(name -> {
                    Meter meter = mock(Meter.class);
                    when(meter.getName()).thenReturn(name);
                    when(meter.getAmrSystem()).thenReturn(amrSystem);
                    when(meter.getAmrId()).thenReturn(name);
                    when(amrSystem.findMeter(name)).thenReturn(Optional.of(meter));
                    return meter;
                })
                .collect(Collectors.toList());
        List<EndDevice> endDevices = new ArrayList<>(meters);
        EndDeviceGroup deviceGroup = mock(EndDeviceGroup.class);
        when(deviceGroup.getMembers(any(Instant.class))).thenReturn(endDevices);
        when(deviceGroup.getMembers(any(Range.class))).thenReturn(endDevices);

        when(task.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(task.getEndDeviceGroup()).thenReturn(Optional.of(deviceGroup));

        mdcChannelsContainers = meters.stream()
                .flatMap(meter -> {
                    List<ChannelsContainer> containers = mockRandomNumberOf(ChannelsContainer.class, 5);
                    when(meter.getChannelsContainers()).thenReturn(containers);
                    return containers.stream();
                })
                .collect(Collectors.toList());
    }

    private void setUpMdmTask(String... usagePointNames) {
        List<UsagePoint> usagePoints = Arrays.stream(usagePointNames)
                .map(name -> {
                    UsagePoint usagePoint = mock(UsagePoint.class);
                    when(usagePoint.getName()).thenReturn(name);
                    return usagePoint;
                })
                .collect(Collectors.toList());
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getMembers(any(Instant.class))).thenReturn(usagePoints);
        when(usagePointGroup.getMembers(any(Range.class))).thenReturn(usagePoints);

        when(task.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(task.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));

        purpose1 = mock(MetrologyPurpose.class);
        purpose2 = mock(MetrologyPurpose.class);
        contracts = new ArrayList<>();
        contractsForPurpose1 = new ArrayList<>();
        contractsForPurpose2 = new ArrayList<>();
        mdmChannelsContainers = new ArrayList<>();
        mdmChannelsContainersForPurpose1 = new ArrayList<>();
        mdmChannelsContainersForPurpose2 = new ArrayList<>();
        usagePoints.forEach(usagePoint -> {
            List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMCs = mockRandomNumberOf(EffectiveMetrologyConfigurationOnUsagePoint.class, 5);
            when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(effectiveMCs);
            when(usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(effectiveMCs);
            effectiveMCs.forEach(effectiveMC -> {
                UsagePointMetrologyConfiguration mc = mock(UsagePointMetrologyConfiguration.class);
                when(effectiveMC.getMetrologyConfiguration()).thenReturn(mc);
                when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
                List<MetrologyContract> metrologyContracts = mockRandomNumberOf(MetrologyContract.class, 5);
                when(mc.getContracts()).thenReturn(metrologyContracts);
                for (int i = 0; i < metrologyContracts.size(); ++i) {
                    MetrologyContract contract = metrologyContracts.get(i);
                    ChannelsContainer container = mock(ChannelsContainer.class);
                    when(effectiveMC.getChannelsContainer(contract)).thenReturn(Optional.of(container));
                    when(effectiveMC.getChannelsContainer(eq(contract), any(Instant.class))).thenReturn(Optional.of(container));
                    if (i % 2 == 0) {
                        when(contract.getMetrologyPurpose()).thenReturn(purpose1);
                        contractsForPurpose1.add(contract);
                        mdmChannelsContainersForPurpose1.add(container);
                    } else {
                        when(contract.getMetrologyPurpose()).thenReturn(purpose2);
                        contractsForPurpose2.add(contract);
                        mdmChannelsContainersForPurpose2.add(container);
                    }
                    contracts.add(contract);
                    mdmChannelsContainers.add(container);
                }
            });
        });
    }

    private static <T> List<T> mockRandomNumberOf(Class<T> theClass, int upTo) {
        Random random = new Random();
        return IntStream.range(0, random.nextInt(upTo) + 1)
                .mapToObj(i -> mock(theClass))
                .collect(Collectors.toList());
    }
}
