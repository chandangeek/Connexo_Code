package com.energyict.mdc.masterdata;

import com.elster.jupiter.time.TimeDuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 13/01/2017
 * Time: 13:37
 */
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
        assertThat(LoadProfileIntervals.DAYS_ONE.getTimeDuration()).isEqualTo(expected[14]);      //
        assertThat(LoadProfileIntervals.HOURS_FOUR.getTimeDuration()).isEqualTo(expected[11]);    //
        assertThat(LoadProfileIntervals.HOURS_ONE.getTimeDuration()).isEqualTo(expected[8]);      //
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
        assertThat(LoadProfileIntervals.MONTHS_ONE.getTimeDuration()).isEqualTo(expected[15]);    //
    }
}
