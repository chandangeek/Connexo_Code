package com.energyict.protocolimpl.instromet.connection;

import com.energyict.protocolimpl.instromet.core.InstrometProtocol;

import java.io.IOException;

public class StatusCommand extends AbstractCommand {

	private int status;

	public StatusCommand(InstrometProtocol instrometProtocol) {
		super(instrometProtocol);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void checkStatusCode(int statusCode) throws IOException {
		if (statusCode == 0) // OK
			return;
		else if (statusCode == 3)
			throw new IOException("Status code 3 was returned: Invalid function, function sent was invalid");
		else if (statusCode == 5)
			throw new IOException("Status code 5 was returned: Read address error, Read operation to invalid address outside table");
		else if (statusCode == 6)
			throw new IOException("Status code 6 was returned: Read length error, read of more than 65000 bytes was requested");
		else if (statusCode == 7)
			throw new IOException("Status code 7 was returned: Write length error, write of more than 220 bytes was requested");
		else if (statusCode == 8)
			throw new IOException("Status code 8 was returned: Invalid Write, Write operation cannot be performed on location");
		else if (statusCode == 9)
			throw new IOException("Status code 9 was returned: Write address error, Write operation to invalid address outside table");
		else if (statusCode == 10)
			throw new IOException("Status code 10 was returned: EEProm write protect, EEProm is write protected");
		else if (statusCode == 11)
			throw new IOException("Status code 11 was returned: Table Switch provided was invalid");
		else
			throw new IOException("Invalid status code returned: " + statusCode);

	}

}
