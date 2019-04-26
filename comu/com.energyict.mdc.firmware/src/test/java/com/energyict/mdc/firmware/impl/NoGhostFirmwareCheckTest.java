/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class NoGhostFirmwareCheckTest extends AbstractFirmwareCheckTest {

    private QueryStream<ActivatedFirmwareVersion> activatedFirmwareVersionStream;
    @Captor
    private ArgumentCaptor<Condition> conditionCaptor;

    public NoGhostFirmwareCheckTest() {
        super(null, NoGhostFirmwareCheck.class);
    }

    @Override
    public void setUp() {
        super.setUp();
        when(dataModel.stream(ActivatedFirmwareVersion.class)).thenAnswer(invocation -> activatedFirmwareVersionStream);
    }

    @Test
    public void testGhostFound() {
        activatedFirmwareVersionStream = FakeBuilder.initBuilderStub(true, QueryStream.class);

        expectError("There is firmware with \"Ghost\" status on the device.");
    }

    @Test
    public void testNoGhostFound() {
        activatedFirmwareVersionStream = FakeBuilder.initBuilderStub(false, QueryStream.class);

        expectSuccess();

        verify(activatedFirmwareVersionStream, times(2)).filter(conditionCaptor.capture());
        List<Condition> filterConditions = conditionCaptor.getAllValues();
        Optional<Comparison> deviceConditionOptional = filterConditions.stream()
                .filter(Comparison.class::isInstance)
                .findAny()
                .map(Comparison.class::cast);
        assertThat(deviceConditionOptional).isPresent();
        assertThat(deviceConditionOptional.map(Comparison::getFieldName)).isPresent();
        assertThat(deviceConditionOptional.map(Comparison::getFieldName).get()).contains("device");
        assertThat(deviceConditionOptional.map(Comparison::getValues)).contains(new Device[]{device});
        Optional<Contains> firmwareTypeConditionOptional = filterConditions.stream()
                .filter(Contains.class::isInstance)
                .findAny()
                .map(Contains.class::cast);
        assertThat(firmwareTypeConditionOptional).isPresent();
        assertThat(firmwareTypeConditionOptional.map(Contains::getFieldName)).isPresent();
        assertThat(firmwareTypeConditionOptional.map(Contains::getFieldName).get()).contains("firmwareType");
        assertThat(firmwareTypeConditionOptional.map(Contains::getCollection)).contains(Arrays.asList(FirmwareType.METER, FirmwareType.COMMUNICATION));

        verify(activatedFirmwareVersionStream).anyMatch(conditionCaptor.capture());
        Condition condition = conditionCaptor.getValue();
        assertThat(condition).isInstanceOf(Comparison.class);
        Comparison firmwareStatusCondition = (Comparison) condition;
        assertThat(firmwareStatusCondition.getFieldName()).contains("firmwareStatus");
        assertThat(firmwareStatusCondition.getValues()).containsOnly(FirmwareStatus.GHOST);
    }
}
