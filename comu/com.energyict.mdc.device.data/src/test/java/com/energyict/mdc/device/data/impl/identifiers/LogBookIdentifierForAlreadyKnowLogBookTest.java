package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 06/05/15
 * Time: 10:18
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookIdentifierForAlreadyKnowLogBookTest {

    @Mock
    private Device device;
    @Mock
    private LogBook logBook;

    @Before
    public void setup() {
        when(logBook.getDevice()).thenReturn(device);
    }

    @Test
    public void serialNumberDeviceIdentifierShouldBeUsedTest() {
        LogBookIdentifierForAlreadyKnowLogBook logBookIdentifierForAlreadyKnowLogBook = new LogBookIdentifierForAlreadyKnowLogBook(logBook);

        assertThat(logBookIdentifierForAlreadyKnowLogBook.getDeviceIdentifier().getDeviceIdentifierType()).isEqualTo(DeviceIdentifierType.SerialNumber);
    }

}