package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.CtrTest;
import com.energyict.protocol.MessageEntry;
import org.junit.Test;

import static com.energyict.genericprotocolimpl.elster.ctr.messaging.WriteGasParametersMessage.getMessageSpec;
import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 10/03/11
 * Time: 14:30
 */
public class WriteGasParametersMessageTest extends CtrTest {

    private final MessageEntry[] incorrectCanExecute = new MessageEntry[]{
            new MessageEntry("<WriteGasParameters GasDensity=\"a\" AirDensity=\"b\" RelativeDensity=\"c\" N2_Percentage=\"d\" CO2_Percentage=\"e\" H2_Percentage=\"f\" HigherCalorificValue=\"g\"> </WriteGasParameters>", ""),
            new MessageEntry("<WriteGasParameters GasDensity=\"0.0000009\" AirDensity=\"0\" RelativeDensity=\"0\" N2_Percentage=\"0\" CO2_Percentage=\"0\" H2_Percentage=\"0\" HigherCalorificValue=\"0\"> </WriteGasParameters>", ""),
            new MessageEntry("<WriteGasParameters GasDensity=\"0\" AirDensity=\"32767.1\" RelativeDensity=\"0\" N2_Percentage=\"0\" CO2_Percentage=\"0\" H2_Percentage=\"0\" HigherCalorificValue=\"0\"> </WriteGasParameters>", "")
    };

    private final MessageEntry[] cannotExecute = new MessageEntry[]{
            new MessageEntry("<WriteGlasParameters GasDensity=\"a\" AirDensity=\"b\" RelativeDensity=\"c\" N2_Percentage=\"d\" CO2_Percentage=\"e\" H2_Percentage=\"f\" HigherCalorificValue=\"g\"> </WriteGasParameters>", "")
    };

    @Test
    public void testCanExecuteThisMessage() throws Exception {
        for (MessageEntry messageEntry : incorrectCanExecute) {
            assertTrue(getWriteGasParametersMessage().canExecuteThisMessage(messageEntry));
        }
        for (MessageEntry messageEntry : cannotExecute) {
            assertFalse(getWriteGasParametersMessage().canExecuteThisMessage(messageEntry));
        }
    }

    @Test
    public void testExecuteMessage() throws Exception {
        for (MessageEntry messageEntry : incorrectCanExecute) {
            try {
                getWriteGasParametersMessage().executeMessage(messageEntry);
                fail("Message with content [" + messageEntry.getContent() + "] should fail!");
            } catch (BusinessException e) {
                // Absorb exceptions, because it MUST occur
            }
        }
    }

    @Test
    public void testGetMessageSpec() throws Exception {
        assertNotNull(getMessageSpec(true));
        assertNotNull(getMessageSpec(false));
    }

    @Test
    public void testFailingValidateAndGetDensity() {
        String[] values = new String[]{null, "", "167.77216", "167.772151", "0.16777215", "0.1677701", "167.77216", "10.0000001"};
        for (String value : values) {
            try {
                int intValue = getWriteGasParametersMessage().validateAndGetDensity(value);
                fail("[" + value + "] should have thrown an exception but returned [" + intValue + "]");
            } catch (BusinessException e) {
            }
        }
    }

    @Test
    public void testFailingValidateAndGetHCV() {
        String[] values = new String[]{null, "", "167.77216", "167.772151", "0.16777215", "0.1677701", "167.77216", "10.0000001"};
        for (String value : values) {
            try {
                int intValue = getWriteGasParametersMessage().validateAndGetHCV(value);
                fail("[" + value + "] should have thrown an exception but returned [" + intValue + "]");
            } catch (BusinessException e) {
            }
        }
    }

    @Test
    public void testFailingValidateAndGetPercentage() {
        String[] values = new String[]{null, "", "167.77215", "167.772151", "0.16777215", "0.1677701", "167.77216", "10.000001"};
        for (String value : values) {
            try {
                int intValue = getWriteGasParametersMessage().validateAndGetPercentage(value);
                fail("[" + value + "] should have thrown an exception but returned [" + intValue + "]");
            } catch (BusinessException e) {
            }
        }
    }

    private WriteGasParametersMessage getWriteGasParametersMessage() {
        return new WriteGasParametersMessage(new MTU155MessageExecutor(getLogger(), getDummyRequestFactory(new byte[0]), null, getDummyStoreObject()));
    }

}
