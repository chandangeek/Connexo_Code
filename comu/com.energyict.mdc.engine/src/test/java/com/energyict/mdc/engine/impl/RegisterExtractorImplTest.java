package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.TextReading;
import com.energyict.mdc.device.data.TextRegister;
import com.energyict.mdc.upl.messages.legacy.RegisterExtractor;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RegisterExtractorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (09:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterExtractorImplTest {

    private static final String EXPECTED_TEXT_READING_VALUE = "RegisterExtractorImplTest";
    @Mock
    private NumericalRegister numericalRegister;
    @Mock
    private NumericalReading numericalReading;
    @Mock
    private TextRegister textRegister;
    @Mock
    private TextReading textReading;

    @Before
    public void initializeMocks() {
        when(this.numericalRegister.getLastReading()).thenReturn(Optional.of(this.numericalReading));
        when(this.textRegister.getLastReading()).thenReturn(Optional.of(this.textReading));
        when(this.textReading.getValue()).thenReturn(EXPECTED_TEXT_READING_VALUE);
    }

    @Test
    public void lastReadingDelegatesToActualRegister() {
        RegisterExtractor extractor = this.getInstance();

        // Business method
        Optional<RegisterExtractor.RegisterReading> reading = extractor.lastReading(this.numericalRegister);

        // Asserts
        assertThat(reading).isPresent();
        verify(this.numericalRegister).getLastReading();
    }

    @Test
    public void extractTextFromNumericalRegisterReturnsNull() {
        RegisterExtractor extractor = this.getInstance();
        RegisterExtractor.RegisterReading reading = extractor.lastReading(this.numericalRegister).get();

        // Business method + asserts
        assertThat(reading.text()).isNull();
    }

    @Test
    public void extractTextFromTextRegister() {
        RegisterExtractor extractor = this.getInstance();
        RegisterExtractor.RegisterReading reading = extractor.lastReading(this.textRegister).get();

        // Business method + asserts
        assertThat(reading.text()).isEqualTo(EXPECTED_TEXT_READING_VALUE);
    }

    private RegisterExtractor getInstance() {
        return new RegisterExtractorImpl();
    }

}