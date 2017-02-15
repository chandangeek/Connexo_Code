package com.energyict.protocolimpl.iec1107.indigo.pxar;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;

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
@Deprecated
public class IndigoPXAR extends AbstractIEC1107Protocol {

	public IndigoPXAR(PropertySpecService propertySpecService, NlsService nlsService) {
		super(propertySpecService, nlsService);
	}

	@Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:27 +0200 (Thu, 26 Nov 2015)$";
    }

	@Override
	protected void doConnect() {
	}

}