/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * Default readCommand
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public class ReadCommand extends AbstractCommand {

	/** Used for reading a simple object */
	protected static String SIMPLE_READ_COMMAND = "R1";
	
	protected String address;
	private static byte readData = 0x31;

	/**
	 * Default constructor
	 */
	public ReadCommand(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	/**
	 * Setter for the startAddress
	 * 
	 * @param address
	 *            - the address to start reading
	 */
	public void setStartAddress(String address) {
		this.address = address;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command prepareBuild() {
		Command command = new Command(SIMPLE_READ_COMMAND.getBytes());
		command.setStartAddress(address);
		command.setData(new byte[] { readData });
		return command;
	}

	/**
	 * Invoke the command
	 * 
	 * @return the response
	 * 
	 * @throws IOException
	 *             when a logical exception occurred
	 */
	public String invoke() throws IOException {
		Command command = prepareBuild();
        getConnection().sendRawCommandFrame(command.getCommand(), command.getConstructedData());
        String received = getConnection().receiveString();

        StringBuilder response = new StringBuilder();

        int inEscape = 0;
        int ub = 0;
        int lb;
        for (char ch : received.toCharArray()) {
            if ((ch != '\\') && (inEscape == 0)) {
                response.append(ch);
            } else {
                inEscape++;
                switch (inEscape) {
                    case 1:
                        break;
                    case 2: // x
                        if ((ch != 'x') && (ch != 'X')) {
                            response.append('\\');
                            if (ch != '\\') {
                                response.append(ch);
                            }
                            inEscape = 0;
                        }
                        break;
                    case 3:
                        ub = Character.digit(ch, 16);
                        break;
                    case 4:
                        lb = Character.digit(ch, 16);
                        int c = ((ub << 4) + lb);
                        response.append((char) c);
                        inEscape = 0;
                        break;
                    default:
                        inEscape = 0;
                }
            }
        }
        return checkResponseForErrors(response.toString());
    }

}
