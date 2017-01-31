/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import com.elster.jupiter.time.TimeDuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoadProfileIntervalsTest {
    private final static TimeDuration[] expected =  {new TimeDuration(1, TimeDuration.TimeUnit.MINUTES),   //0
                                           new TimeDuration(2, TimeDuration.TimeUnit.MINUTES),             //1
                                           new TimeDuration(3, TimeDuration.TimeUnit.MINUTES),             //2
                                           new TimeDuration(5, TimeDuration.TimeUnit.MINUTES),             //3
                                           new TimeDuration(10, TimeDuration.TimeUnit.MINUTES),            //4
                                           new TimeDuration(15, TimeDuration.TimeUnit.MINUTES),            //5
                                           new TimeDuration(20, TimeDuration.TimeUnit.MINUTES),            //6
                                           new TimeDuration(30, TimeDuration.TimeUnit.MINUTES),            //7
                                           new TimeDuration(1, TimeDuration.TimeUnit.HOURS),               //8
                                           new TimeDuration(2, TimeDuration.TimeUnit.HOURS),               //9
                                           new TimeDuration(3, TimeDuration.TimeUnit.HOURS),               //10
                                           new TimeDuration(4, TimeDuration.TimeUnit.HOURS),               //11
                                           new TimeDuration(6, TimeDuration.TimeUnit.HOURS),               //12
                                           new TimeDuration(12, TimeDuration.TimeUnit.HOURS),              //13
                                           new TimeDuration(1, TimeDuration.TimeUnit.DAYS),                //14
                                           new TimeDuration(1, TimeDuration.TimeUnit.MONTHS)};             //15

    @Test
    public void testHaveThemAll(){
        assertThat(LoadProfileIntervals.values().length).isEqualTo(expected.length);
        assertThat(LoadProfileIntervals.DAY_ONE.getTimeDuration()).isEqualTo(expected[14]);      //
        assertThat(LoadProfileIntervals.HOURS_FOUR.getTimeDuration()).isEqualTo(expected[11]);    //
        assertThat(LoadProfileIntervals.HOUR_ONE.getTimeDuration()).isEqualTo(expected[8]);      //
        assertThat(LoadProfileIntervals.HOURS_SIX.getTimeDuration()).isEqualTo(expected[12]);     //
        assertThat(LoadProfileIntervals.HOURS_THREE.getTimeDuration()).isEqualTo(expected[10]);   //
        assertThat(LoadProfileIntervals.HOURS_TWELVE.getTimeDuration()).isEqualTo(expected[13]);  //
        assertThat(LoadProfileIntervals.HOURS_TWO.getTimeDuration()).isEqualTo(expected[9]);      //
        assertThat(LoadProfileIntervals.MINUTE_ONE.getTimeDuration()).isEqualTo(expected[0]);     //
        assertThat(LoadProfileIntervals.MINUTES_FIFTEEN.getTimeDuration()).isEqualTo(expected[5]);//
        assertThat(LoadProfileIntervals.MINUTES_FIVE.getTimeDuration()).isEqualTo(expected[3]);   //
        assertThat(LoadProfileIntervals.MINUTES_TEN.getTimeDuration()).isEqualTo(expected[4]);    //
        assertThat(LoadProfileIntervals.MINUTES_THIRTY.getTimeDuration()).isEqualTo(expected[7]); //
        assertThat(LoadProfileIntervals.MINUTES_THREE.getTimeDuration()).isEqualTo(expected[2]);  //
        assertThat(LoadProfileIntervals.MINUTES_TWENTY.getTimeDuration()).isEqualTo(expected[6]); //
        assertThat(LoadProfileIntervals.MINUTES_TWO.getTimeDuration()).isEqualTo(expected[1]);    //
        assertThat(LoadProfileIntervals.MONTH_ONE.getTimeDuration()).isEqualTo(expected[15]);    //
    }

    @Test
    public void fromTimeDuration(){
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[0]).get()).isEqualTo(LoadProfileIntervals.MINUTE_ONE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[1]).get()).isEqualTo(LoadProfileIntervals.MINUTES_TWO);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[2]).get()).isEqualTo(LoadProfileIntervals.MINUTES_THREE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[3]).get()).isEqualTo(LoadProfileIntervals.MINUTES_FIVE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[4]).get()).isEqualTo(LoadProfileIntervals.MINUTES_TEN);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[5]).get()).isEqualTo(LoadProfileIntervals.MINUTES_FIFTEEN);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[6]).get()).isEqualTo(LoadProfileIntervals.MINUTES_TWENTY);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[7]).get()).isEqualTo(LoadProfileIntervals.MINUTES_THIRTY);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[8]).get()).isEqualTo(LoadProfileIntervals.HOUR_ONE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[9]).get()).isEqualTo(LoadProfileIntervals.HOURS_TWO);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[10]).get()).isEqualTo(LoadProfileIntervals.HOURS_THREE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[11]).get()).isEqualTo(LoadProfileIntervals.HOURS_FOUR);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[12]).get()).isEqualTo(LoadProfileIntervals.HOURS_SIX);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[13]).get()).isEqualTo(LoadProfileIntervals.HOURS_TWELVE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[14]).get()).isEqualTo(LoadProfileIntervals.DAY_ONE);
        assertThat(LoadProfileIntervals.fromTimeDuration(expected[15]).get()).isEqualTo(LoadProfileIntervals.MONTH_ONE);

        assertThat(LoadProfileIntervals.fromTimeDuration(TimeDuration.minutes(4)).isPresent()).isFalse();
        assertThat(LoadProfileIntervals.fromTimeDuration(TimeDuration.hours(5)).isPresent()).isFalse();
    }
}
