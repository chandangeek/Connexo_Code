/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.readings.Reading;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreStoreRegistersTest extends AbstractCollectedDataIntegrationTest {

    @Mock
    private ComServerDAO comServerDAO;

    Date fromClock = new DateTime(2013, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();

    @Test
    @Transactional
    public void scalerTestFoMultipleCIMUnit() {
        int scale = -4;
        BigDecimal amount = BigDecimal.valueOf(100500);
        getMeteringService().createReadingType("0.0.0.12.1.2.38.0.0.0.0.0.0.0.0.0.0.0", "type");
        OfflineRegister offlineRegister = mock(OfflineRegister.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineRegister.getReadingTypeMRID()).thenReturn("0.0.0.12.1.2.38.0.0.0.0.0.0.0.0.0.0.0");
        when(offlineRegister.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(this.comServerDAO.findOfflineRegister(any(), any())).thenReturn(Optional.of(offlineRegister));
        PreStoreRegisters preStoreRegisters = new PreStoreRegisters(getMdcReadingTypeUtilService(), comServerDAO);
        CollectedRegisterList collectedRegisterList = mock(CollectedRegisterList.class);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        Quantity quantity = mock(Quantity.class);
        Unit unit = Unit.get(BaseUnit.UNITLESS, scale);
        when(collectedRegisterList.getCollectedRegisters()).thenReturn(Collections.singletonList(collectedRegister));
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        when(collectedRegister.isTextRegister()).thenReturn(false);
        when(collectedRegister.getCollectedQuantity()).thenReturn(quantity);
        when(collectedRegister.getReadTime()).thenReturn(fromClock);
        when(quantity.getAmount()).thenReturn(amount);
        when(quantity.getUnit()).thenReturn(unit);
        Map<DeviceIdentifier, List<Reading>> preStoreReading = preStoreRegisters.preStore(collectedRegisterList);
        assertThat(preStoreReading.get(deviceIdentifier).get(0).getValue()).isEqualTo(amount.scaleByPowerOfTen(scale));
    }

    @Test
    @Transactional
    public void scalerTestFoSingleCIMUnit() {
        int scale = -4;
        BigDecimal amount = BigDecimal.valueOf(100500);
        getMeteringService().createReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "type");
        OfflineRegister offlineRegister = mock(OfflineRegister.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineRegister.getReadingTypeMRID()).thenReturn("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(offlineRegister.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(this.comServerDAO.findOfflineRegister(any(), any())).thenReturn(Optional.of(offlineRegister));
        PreStoreRegisters preStoreRegisters = new PreStoreRegisters(getMdcReadingTypeUtilService(), comServerDAO);
        CollectedRegisterList collectedRegisterList = mock(CollectedRegisterList.class);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        Quantity quantity = mock(Quantity.class);
        Unit unit = Unit.get(BaseUnit.WATTHOUR, scale);
        when(collectedRegisterList.getCollectedRegisters()).thenReturn(Collections.singletonList(collectedRegister));
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        when(collectedRegister.isTextRegister()).thenReturn(false);
        when(collectedRegister.getCollectedQuantity()).thenReturn(quantity);
        when(collectedRegister.getReadTime()).thenReturn(fromClock);
        when(quantity.getAmount()).thenReturn(amount);
        when(quantity.getUnit()).thenReturn(unit);
        Map<DeviceIdentifier, List<Reading>> preStoreReading = preStoreRegisters.preStore(collectedRegisterList);
        assertThat(preStoreReading.get(deviceIdentifier).get(0).getValue()).isEqualTo(amount.scaleByPowerOfTen(scale));
    }
}



