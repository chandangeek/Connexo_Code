package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LogBookIdentifierByIdImpl} component
 *
 * @author sva
 * @since 10/12/12 - 16:11
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookIdentifierByIdImplTest {

    private static final long LOGBOOK_ID = 1;
    private static final long LOGBOOK_2_ID = 2;

    @Mock
    private LogBookService logBookService;

    @Test(expected = NotFoundException.class)
    public void testLogBookDoesNotExist() {
        when(this.logBookService.findById(LOGBOOK_ID)).thenReturn(Optional.absent());

        // Business method
        new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService).getLogBook();

        // Expected a NotFoundException
    }

    @Test
    public void testOnlyOneLogBook() {
        LogBook logBook = mock(LogBook.class);
        when(this.logBookService.findById(LOGBOOK_ID)).thenReturn(Optional.of(logBook));

        // Business method
        BaseLogBook returnedLogBook = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService).getLogBook();

        // Asserts
        assertThat(logBook).isEqualTo(returnedLogBook);
    }

    @Test
    public void testEquals() {
        LogBookIdentifier identifier_A = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService);
        LogBookIdentifier identifier_B = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService);

        // Asserts
        assertThat(identifier_A).isEqualTo(identifier_B);
    }

    @Test
    public void testNotEquals() {
        LogBookIdentifier identifier_A = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService);
        LogBookIdentifier identifier_B = new LogBookIdentifierByIdImpl(LOGBOOK_2_ID, this.logBookService);

        // Asserts
        assertThat(identifier_A).isNotEqualTo(identifier_B);
    }

    @Test
    public void testNotEqualsToString() {
        LogBookIdentifier identifier_A = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService);

        // Asserts
        assertThat(identifier_A).isNotEqualTo("identifier_B");
    }

    @Test
    public void testNotEqualsToLong() {
        LogBookIdentifier identifier_A = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService);

        // Asserts
        assertThat(identifier_A).isNotEqualTo(Long.valueOf(LOGBOOK_2_ID));
    }

    @Test
    public void testNotEqualsToNull() {
        LogBookIdentifier identifier_A = new LogBookIdentifierByIdImpl(LOGBOOK_ID, this.logBookService);

        // Asserts
        assertThat(identifier_A).isNotEqualTo(null);
    }

}