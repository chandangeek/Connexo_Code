package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages;

import com.energyict.cbo.TemporalExpression;
import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.tasks.NextExecutionSpecs;
import com.energyict.mdc.tasks.NextExecutionSpecsImpl;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects.CronTabStyleConverter;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/07/2015 - 10:40
 */
public class CronTabStyleConverterTest {

    @Test
    public void test() {
        NextExecutionSpecs nextExecutionSpecs = spy(new NextExecutionSpecsImpl(0));
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(5, TimeDuration.MINUTES), new TimeDuration(0)));
        String convert = CronTabStyleConverter.convert(nextExecutionSpecs);
        assertEquals("0 */5 * * * *", convert);

        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(2, TimeDuration.HOURS), new TimeDuration(3, TimeDuration.MINUTES)));
        convert = CronTabStyleConverter.convert(nextExecutionSpecs);
        assertEquals("0 3 */2 * * *", convert);

        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(2, TimeDuration.DAYS), new TimeDuration(6, TimeDuration.HOURS)));
        convert = CronTabStyleConverter.convert(nextExecutionSpecs);
        assertEquals("0 0 6 */2 * *", convert);

        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(2, TimeDuration.WEEKS), new TimeDuration(6, TimeDuration.HOURS)));
        convert = CronTabStyleConverter.convert(nextExecutionSpecs);
        assertEquals("0 0 6 */14 * 1", convert);

        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(3, TimeDuration.MONTHS), new TimeDuration(15, TimeDuration.DAYS)));
        convert = CronTabStyleConverter.convert(nextExecutionSpecs);
        assertEquals("0 0 0 16 */3 *", convert);
    }
}