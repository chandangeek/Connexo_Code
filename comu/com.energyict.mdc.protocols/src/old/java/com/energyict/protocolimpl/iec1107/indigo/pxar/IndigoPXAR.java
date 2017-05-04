/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.indigo.pxar;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * Protocol implementation for the Indigo PXA/R meter. The meter can be read with an Optical head or via RS232.
 * Both interfaces conform to IEC1107 mode C, with exception that the RS232 interface does not include baudrate switching.
 * <br><br>
 * There are two main versions of the PXA/R meter software, the latter version conforms to code of practice five.
 * The differences between those two are as follows:
 * <dd>- VTCT ration combinations are held in a different format<br>
 * <dd>- Tariff registers contain a different number of decimal places<br>
 * <dd>- The definition of which quantities are being stored in the interval data is different<br>
 * <dd>- Code 5 meters can store a new interval quantity of "status flags"<br>
 * <dd>- One of the non-code 5 rate dependent maximum demand registers has been re-defined in code 5
 * meters as a continuous maximum demand register of different units to the main demand registers.<br>
 * <dd>- Code 5 meters can be configured to provide password security read operations<br>
 * <dd>- On code 5 meters an IEC1107 secondary protocol can be used which is quicker<br>
 * <dd>- On code 5 meters it is possible to uniquely address a particular meter so that meters may share a modem<br>
 * <br>
 * Code 5 meters can be identified as such by:<br>
 * <dd>a/ Their dial plate (not so useful for protocol development<br>
 * <dd>b/ Their software version number (four BCD nibbles at address 0x0B00, 04.xx or 05.xx = code5, 02.xx = non-code5)<br>
 * <dd>c/ Their meter type as held at 0x0FA8 in two BCD nibbles, and also passed in the IEC sing on ident message.
 * (code 5 meter types have values 13 to 20, non-code 5 meter types are 04 to 11)<br>
 * <dd>d/ IEC meters (ie. interval recording meters) have a manufacturer ident message beginning with "SLb" as opposed to
 * non-code 5 meters which begin "SLB".
 * <br>
 * <b>Important Note:</b><br>
 * If the meter is in the sign-on state, then most of the data is <i>not</i> updated.
 * This means the date/time will be the same if you read it twice in the same session.
 *
 * @author gna
 * @since 5-feb-2010
 *
 */
public class IndigoPXAR extends AbstractIEC1107Protocol {

	@Override
	public String getProtocolDescription() {
		return "Actaris Indigo PXA/R IEC1107";
	}

	@Inject
	public IndigoPXAR(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	/**
	 * @return the ProtocolVerison
	 */
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    /**
     * Add additional functionality after a physical connect, like initializing some objects
     */
	@Override
	protected void doConnect() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @return a List of Strings of optional keys
	 */
	@Override
	protected List<String> doGetOptionalKeys() {
		return Collections.emptyList();
	}

	/**
	 * Validate some protocol specific properties
	 */
	@Override
	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
	}

}