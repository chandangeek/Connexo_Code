package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Register;

import com.google.common.collect.Range;

import javax.ws.rs.WebApplicationException;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testing the reading resulting in calling the createNew::register method of the NumericalReadingInfo
 * A number of these tests do pass although I'm sure the persisted readings will lead to unexpected failures
 * Some fail with NPE due to bad initialized ifo objects....
 * Copyrights EnergyICT
 * Date: 2/05/2017
 * Time: 16:57
 */
@Ignore     // Test with fail with a few NPE's
@RunWith(MockitoJUnitRunner.class)
public class NumericalReadingInfoTest {

    @Mock
    Register register;
    @Mock
    ReadingType readingType;

    @Before
    public void prepareMokskes() {
        when(readingType.getMRID()).thenReturn("1.2.3.4.5.6");
        when(register.getReadingType()).thenReturn(readingType);
    }

    @Test
    public void createNewWithClosedIntervalForBillingRegisterTest(){
        when(register.isBilling()).thenReturn(true);

        LocalDate today  = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.interval = IntervalInfo.from(Range.closed(Instant.ofEpochMilli(yesterday.toEpochDay()), Instant.ofEpochMilli(today.toEpochDay())));

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimePeriod()).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();      // Do we have a problem here, no timestamp on the reading, only a timeperiod ?
    }

    @Test
    public void createNewWithClosedIntervalForNonBillingRegisterTest(){
        when(register.isBilling()).thenReturn(false);

        LocalDate today  = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.interval = IntervalInfo.from(Range.closed(Instant.ofEpochMilli(yesterday.toEpochDay()), Instant.ofEpochMilli(today.toEpochDay())));

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimePeriod()).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();      // Do we have a problem here, no timestamp on the reading, only a timeperiod ?
    }

    @Test   // Will fail with a NPE
    public void createNewWithNotEndingIntervalForBillingRegisterTest(){
        when(register.isBilling()).thenReturn(true);

        LocalDate today  = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.interval = IntervalInfo.from(Range.atMost(Instant.ofEpochMilli(yesterday.toEpochDay())));

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimePeriod()).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();      // Do we have a problem here, no timestamp on the reading, only a timeperiod ?
    }

    @Test   // Will fail with a NPE
    public void createNewWithNotEndingIntervalForNonBillingRegisterTest(){
        when(register.isBilling()).thenReturn(false);

        LocalDate today  = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.interval = IntervalInfo.from(Range.atMost(Instant.ofEpochMilli(yesterday.toEpochDay())));

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimePeriod()).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();      // Do we have a problem here, no timestamp on the reading, only a timeperiod ?
    }

    @Test  // Will fail with a NPE
    public void createNewWithEndingIntervalForBillingRegisterTest(){
        when(register.isBilling()).thenReturn(true);

        NumericalReadingInfo info = new NumericalReadingInfo();
        info.interval = IntervalInfo.from(Range.atLeast(Instant.now()));

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimePeriod()).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();      // Do we have a problem here, no timestamp on the reading, only a timeperiod ?
    }

    @Test
    public void createNewWithEndingIntervalForNonBillingRegisterTest(){
        when(register.isBilling()).thenReturn(false);

        NumericalReadingInfo info = new NumericalReadingInfo();
        info.interval = IntervalInfo.from(Range.atLeast(Instant.now()));

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
    }

    @Test
    public void createNewNotBillingWithTimeStampTest(){
        when(register.isBilling()).thenReturn(false);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.timeStamp = Instant.now();

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimeStamp()).isEqualTo(info.timeStamp);
        assertThat(reading.getTimePeriod().isPresent()).isFalse();
    }

    @Test
    public void createNewNotBillingWithoutTimeStampButEventTimeTest(){
        when(register.isBilling()).thenReturn(false);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.eventDate = Instant.now();

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();                // We have a problem here ???
        assertThat(reading.getTimePeriod().isPresent()).isFalse();  // We have a problem here: reading without timestamp nor period
    }

    @Test
    public void createNewNotBillingWithoutTimeStampNorEventTimeTest(){
        when(register.isBilling()).thenReturn(false);
        NumericalReadingInfo info = new NumericalReadingInfo();

        BaseReading reading = info.createNew(register);
        assertThat(reading).isNotNull();
        assertThat(reading.getTimeStamp()).isNull();                // We have a problem here ???
        assertThat(reading.getTimePeriod().isPresent()).isFalse();  // We have a problem here: reading without timestamp nor period
    }


    @Test(expected = WebApplicationException.class)
    public void createNewBillingWithTimeStampTest(){
        when(register.isBilling()).thenReturn(true);
        NumericalReadingInfo info = new NumericalReadingInfo();
        info.timeStamp = Instant.now();           // We have a timestamp,but a timeperiod is expected here? The user only gets a 'Bad Request' response

        info.createNew(register);
    }

    @Test(expected = WebApplicationException.class)
    public void createNewBillingWithoutTimeStampTest(){
        when(register.isBilling()).thenReturn(true);

        NumericalReadingInfo info = new NumericalReadingInfo();
        info.createNew(register);         // results in a 'Bad Request' response, no further explanation
    }
}
