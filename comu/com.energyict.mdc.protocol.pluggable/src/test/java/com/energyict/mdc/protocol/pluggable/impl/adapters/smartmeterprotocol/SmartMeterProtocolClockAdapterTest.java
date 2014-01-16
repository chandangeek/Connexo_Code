package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SmartMeterProtocolClockAdapter}
 *
 * @author gna
 * @since 5/04/12 - 13:33
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolClockAdapterTest {

    private static final long setTimeValue = 1333626134000L;

    @Mock
    private static UserEnvironment userEnvironment = mock(UserEnvironment.class);
    @Mock
    private Environment environment;

    @BeforeClass
    public static void initializeUserEnvironment() {
        UserEnvironment.setDefault(userEnvironment);
        when(userEnvironment.getErrorMsg(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });
    }

    @AfterClass
    public static void cleanupUserEnvironment() {
        UserEnvironment.setDefault(null);
    }

    @Before
    public void initializeEnvironment() {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
    }

    @After
    public void cleanupEnvironment() {
        Environment.DEFAULT.set(null);
    }
    @Test
    public void getCorrectTimeTest() throws IOException {
        final long currentTime = System.currentTimeMillis();
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getTime()).thenReturn(new Date(currentTime));
        SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(smartMeterProtocol);
        assertEquals(new Date(currentTime), smartMeterProtocolClockAdapter.getTime());
    }

    @Test(expected = LegacyProtocolException.class)
    public void getTimeExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getTime()).thenThrow(new IOException("Could not get the time."));
        SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(smartMeterProtocol);
        smartMeterProtocolClockAdapter.getTime();
    }

    @Test
    public void setTimeSuccessfulTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(smartMeterProtocol);
        smartMeterProtocolClockAdapter.setTime(new Date(setTimeValue));
        verify(smartMeterProtocol).setTime(argThat(new TimingArgumentMatcher()));
    }

    @Test(expected = LegacyProtocolException.class)
    public void setTimeExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        doThrow(new IOException("Could not set the time")).when(smartMeterProtocol).setTime(Matchers.<Date>any());
        SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(smartMeterProtocol);
        smartMeterProtocolClockAdapter.setTime(new Date(setTimeValue));
    }

    /**
     * Argument Matcher for the setTime
     */
    private class TimingArgumentMatcher extends ArgumentMatcher<Date> {

        /**
         * Returns whether this matcher accepts the given argument.
         * <p/>
         * The method should <b>never</b> assert if the argument doesn't match. It
         * should only return false.
         *
         * @param argument the argument
         * @return whether this matcher accepts the given argument.
         */
        @Override
        public boolean matches(final Object argument) {
            if (argument instanceof Date) {
                Date sTime = (Date) argument;
                return sTime.getTime() == setTimeValue;
            }
            return false;
        }
    }
}
