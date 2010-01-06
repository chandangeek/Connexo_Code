package com.energyict.protocolimpl.base;

import java.util.Date;

import com.energyict.protocol.tools.InputStreamObserver;
import com.energyict.protocol.tools.OutputStreamObserver;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Stream observer for debugging purposes. This observer formats the
 * communication in a readable form, and writes it to the screen or to a file.
 *
 * @author jme
 */
public class DebuggingObserver implements InputStreamObserver, OutputStreamObserver {


	private static final int	DIRECTION_UNKNOWN	= 0;
	private static final int	DIRECTION_READING	= 1;
	private static final int	DIRECTION_WRITING	= 2;

	private static final String	CRLF				= "\r\n";
	private static final long	TIME_DIFF			= 2000;

	private final boolean		showCommunication;
	private final String		fileName;

	private int					lastDirection		= DIRECTION_UNKNOWN;
	private long				lastAction			= 0L;

	/**
	 * Create a new {@link DebuggingObserver}.
	 * @param fileName
	 * @param showCommunication
	 */
	public DebuggingObserver(String fileName, boolean showCommunication) {
		this.fileName = fileName;
		this.showCommunication = showCommunication;
		log(CRLF + CRLF + "Observer started " + new Date() + "[" + getClass().getCanonicalName() + "]" + CRLF);
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.tools.InputStreamObserver#read(byte[])
	 */
	public void read(byte[] b) {
		if (!isReading() || isLastActionLongTimeAgo()) {
			log(CRLF);
			log("RX[" + System.currentTimeMillis() + "] <= ");
			setLastDirection(DIRECTION_READING);
		}
		log(ProtocolTools.getHexStringFromBytes(b) + " ");
		setLastAction();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.tools.InputStreamObserver#threw(java.lang.Throwable)
	 */
	public void threw(Throwable ex) {
		setLastDirection(DIRECTION_UNKNOWN);
		ex.printStackTrace();
		setLastAction();
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocol.tools.OutputStreamObserver#wrote(byte[])
	 */
	public void wrote(byte[] b) {
		if (!isWriting() || isLastActionLongTimeAgo()) {
			log(CRLF);
			log("TX[" + System.currentTimeMillis() + "] => ");
			setLastDirection(DIRECTION_WRITING);
		}
		log(ProtocolTools.getHexStringFromBytes(b) + " ");
		setLastAction();
	}

	private boolean isReading() {
		return (lastDirection == DIRECTION_READING);
	}

	private boolean isWriting() {
		return (lastDirection == DIRECTION_WRITING);
	}

	private void setLastDirection(int lastDirection) {
		this.lastDirection = lastDirection;
	}

	private void setLastAction() {
		this.lastAction = System.currentTimeMillis();
	}

	private boolean isLastActionLongTimeAgo() {
		return (Math.abs(lastAction - System.currentTimeMillis()) > TIME_DIFF);
	}

	private void log(String message) {
		if (showCommunication) {
			System.out.print(message);
		}
		if (fileName != null) {
			ProtocolTools.writeBytesToFile(fileName, message.getBytes(), true);
		}
	}

}
