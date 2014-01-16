package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MeterProtocolClockAdapter}
 *
 * @author gna
 * @since 4/04/12 - 15:54
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolClockAdapterTest {

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


    /**
     * IOExceptions should be properly handled by the adapter
     *
     * @throws java.io.IOException if a direct call to {@link MeterProtocol#setTime()} is made
     */
    @Test(expected = LegacyProtocolException.class)
    public void setTimeTest() throws IOException {
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        doThrow(new IOException("Couldn't set the time")).when(meterProtocol).setTime();
        MeterProtocolClockAdapter meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        meterProtocolClockAdapter.setTime(new Date());
    }

    @Test(expected = LegacyProtocolException.class)
    public void getTimeExceptionTest() throws IOException {
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        when(meterProtocol.getTime()).thenThrow(new IOException("Failed to get the time"));
        MeterProtocolClockAdapter meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        meterProtocolClockAdapter.getTime();
    }

    @Test
    public void getCorrectTimeTest() throws IOException {
        final Long currentTime = System.currentTimeMillis();
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        when(meterProtocol.getTime()).thenReturn(new Date(currentTime));
        MeterProtocolClockAdapter meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        assertEquals(new Date(currentTime), meterProtocolClockAdapter.getTime());
    }
}
