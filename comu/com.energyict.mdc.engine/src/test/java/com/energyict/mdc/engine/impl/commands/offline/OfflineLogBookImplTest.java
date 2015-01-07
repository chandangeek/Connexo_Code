package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import org.junit.*;
import org.junit.runner.*;

import java.time.Instant;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link OfflineLogBookImpl} component
 *
 * @author sva
 * @since 10/12/12 - 15:23
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineLogBookImplTest {

    private static final long LOGBOOK_ID = 1;
    private static final long DEVICE_ID = 1;
    private static final long LOGBOOK_TYPE_ID = 123;
    private static final String DEVICE_SERIAL = "SerialNumber";
    private static final Instant LAST_LOGBOOK = Instant.ofEpochMilli(1355150108000L);  // Mon, 10 Dec 2012 14:35:08 GMT
    @Mock
    private IdentificationService identificationService;

    @Test
    public void goOfflineTest() {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getId()).thenReturn(LOGBOOK_TYPE_ID);
        LogBookSpec logBookSpec = mock(LogBookSpec.class);
        when(logBookSpec.getLogBookType()).thenReturn(logBookType);

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getSerialNumber()).thenReturn(DEVICE_SERIAL);

        LogBook logBook = mock(LogBook.class);
        when(logBook.getId()).thenReturn(LOGBOOK_ID);
        when(logBook.getLogBookSpec()).thenReturn(logBookSpec);
        when(logBook.getLogBookType()).thenReturn(logBookType);
        when(logBook.getDevice()).thenReturn(device);
        when(logBook.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK));

        //Business Methods
        OfflineLogBook offlineLogBook = new OfflineLogBookImpl(logBook, this.identificationService);

        // asserts
        assertNotNull(offlineLogBook);
        assertEquals("Expected the correct LogBook Id", LOGBOOK_ID, offlineLogBook.getLogBookId());
        assertEquals("Expected the correct device Id", device.getId(), offlineLogBook.getDeviceId());
        assertEquals("Expected the correct device serial number", device.getSerialNumber(), offlineLogBook.getMasterSerialNumber());
        assertTrue(offlineLogBook.getLastLogBook().isPresent());
        assertEquals("Expected the correct lastLogBook date", LAST_LOGBOOK, offlineLogBook.getLastLogBook().get());
        assertEquals("Expected the correct LogBookType", LOGBOOK_TYPE_ID, offlineLogBook.getLogBookTypeId());
    }
}
