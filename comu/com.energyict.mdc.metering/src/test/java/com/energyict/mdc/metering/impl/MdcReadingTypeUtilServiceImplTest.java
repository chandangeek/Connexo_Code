package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MdcReadingTypeUtilServiceImplTest {

    @Mock
    private MeteringService meteringService;

    private final String plainAlias = "PlainAlias";
    private final String aliasWithMacroPeriod = "[Daily] PlainAlias";
    private final String aliasWithMeasuringPeriod = "[15-minute] PlainAlias";
    private final String aliasWithUnit = "PlainAlias (Wh)";
    private final String aliasWithPhase = "PlainAlias Phase-A";
    private final String aliasWithTOU = "PlainAlias ToU 3";
    private final String aliasWithAll = "[Monthly] PlainAlias (kWh) Phase-B ToU 3";

    private ReadingType mockSimpleReadingTypeWithPlainAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        return readingType;
    }

    private ReadingType mockReadingTypeWithDailyPeriodAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        return readingType;
    }

    private ReadingType mockReadingTypeWith15MinPeriodAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        return readingType;
    }

    private ReadingType mockReadingTypeWithUnitAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        return readingType;
    }

    private ReadingType mockReadingTypeWithPhaseAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        return readingType;
    }

    private ReadingType mockReadingTypeWithTOUAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        return readingType;
    }
    private ReadingType mockReadingTypeWithAllAlias() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getAliasName()).thenReturn(plainAlias);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getPhases()).thenReturn(Phase.PHASEB);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        return readingType;
    }

    @Test
    public void simpleReadingTypeWithPlainAliasTest() {
        ReadingType readingType = mockSimpleReadingTypeWithPlainAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(plainAlias);
    }

    @Test
    public void readingTypeWithDailyPeriodTest() {
        ReadingType readingType = mockReadingTypeWithDailyPeriodAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(aliasWithMacroPeriod);
    }

    @Test
    public void readingTypeWith15MinTest() {
        ReadingType readingType = mockReadingTypeWith15MinPeriodAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(aliasWithMeasuringPeriod);
    }

    @Test
    public void readingTypeWithUnitTest() {
        ReadingType readingType = mockReadingTypeWithUnitAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(aliasWithUnit);
    }

    @Test
    public void readingTypeWithPhaseTest() {
        ReadingType readingType = mockReadingTypeWithPhaseAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(aliasWithPhase);
    }

    @Test
    public void readingTypeWithTOUTest() {
        ReadingType readingType = mockReadingTypeWithTOUAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(aliasWithTOU);
    }

    @Test
    public void readingTypeWithAllTest() {
        ReadingType readingType = mockReadingTypeWithAllAlias();
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingType);
        assertThat(fullAlias).isEqualTo(aliasWithAll);
    }

    @Test
    public void getWithStringRequestTest() {
        String readingTypeName = "MyReadingType";
        ReadingType readingType = mockReadingTypeWithAllAlias();
        when(meteringService.getReadingType(readingTypeName)).thenReturn(Optional.of(readingType));
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingTypeName);
        assertThat(fullAlias).isEqualTo(aliasWithAll);
    }

    @Test
    public void getWithUnknownReadingTypeTest() {
        String readingTypeName = "MyReadingType";
        when(meteringService.getReadingType(readingTypeName)).thenReturn(Optional.empty());
        MdcReadingTypeUtilServiceImpl mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl(meteringService);
        String fullAlias = mdcReadingTypeUtilService.getFullAlias(readingTypeName);
        assertThat(fullAlias).isEqualTo("Unknown readingtype");
    }
}