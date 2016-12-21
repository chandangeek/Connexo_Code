package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channels.serial.modem.postdialcommand.AbstractAtPostDialCommand;

import com.energyict.cbo.ApplicationException;

import java.util.List;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author sva
 * @since 22/04/13 - 12:06
 */
public class TypedAtModemPropertiesTest {

    @Test
    public void testSuccessfulValidatePostDialCommand() throws Exception {
        TypedAtModemProperties modemProperties = new TypedAtModemProperties();
        String postDialCommands = "(D)(D:50)(F)(F:100)(W:\\(data\\))(S:7E1)";

        // Business method
        List<AbstractAtPostDialCommand> postDialCommandList = modemProperties.parseAndValidatePostDialCommands(postDialCommands);

        // Asserts
        assertEquals("Expecting 6 dial commands.", 6, postDialCommandList.size());
    }

    @Test(expected = ApplicationException.class)
    public void testInvalidValidatePostDialCommand() throws Exception {
        TypedAtModemProperties modemProperties = new TypedAtModemProperties();
        String postDialCommands = "(D(D:50)(F)(F:100)(W:\\(data\\))(S:7E1)";

        // Business method - Expecting an ApplicationException, cause the postDialCommands string is missing a ')'.
        modemProperties.parseAndValidatePostDialCommands(postDialCommands);
    }

    @Test(expected = ApplicationException.class)
    public void testAnotherInvalidValidatePostDialCommand() throws Exception {
        TypedAtModemProperties modemProperties = new TypedAtModemProperties();
        String postDialCommands = "(D)(D:50)(F)(F:100)(W:data)(S:0E1)";

        // Business method - Expecting an ApplicationException, cause the serialCommunicationSettingsCommand contains invalid nr of data bits.
        modemProperties.parseAndValidatePostDialCommands(postDialCommands);
    }

}
