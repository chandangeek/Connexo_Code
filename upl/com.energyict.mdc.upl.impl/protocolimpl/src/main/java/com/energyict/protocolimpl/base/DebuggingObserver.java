package com.energyict.protocolimpl.base;

import java.util.Date;

import com.energyict.protocol.tools.InputStreamObserver;
import com.energyict.protocol.tools.OutputStreamObserver;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class DebuggingObserver implements InputStreamObserver, OutputStreamObserver {

	private static final String	CRLF				= "\r\n";
	private static final int	DIRECTION_UNKNOWN	= 0;
	private static final int	DIRECTION_READING	= 1;
	private static final int	DIRECTION_WRITING	= 2;

	private static final long	TIME_DIFF			= 2000;

	private int					lastDirection		= DIRECTION_UNKNOWN;
	private long				lastAction			= 0L;
	private boolean				showCommunication	= false;
	private String				fileName			= null;

	public DebuggingObserver(String fileName, boolean showCommunication) {
		this.fileName = fileName;
		this.showCommunication = showCommunication;
		log(CRLF + CRLF + "Observer started " + new Date() + "[" + getClass().getCanonicalName() + "]" + CRLF);
	}

	public DebuggingObserver(String fileName) {
		this(fileName, false);
	}

	public DebuggingObserver(boolean showCommunication) {
		this(null, showCommunication);
	}

	public DebuggingObserver() {
		this(null, false);
	}

	public void read(byte[] b) {
		if (!isReading() || isLastActionLongTimeAgo()) {
			log(CRLF);
			log("RX[" + System.currentTimeMillis() + "] <= ");
			setLastDirection(DIRECTION_READING);
		}
		log(ProtocolTools.getHexStringFromBytes(b) + " ");
		setLastAction();
	}

	public void threw(Throwable ex) {
		setLastDirection(DIRECTION_UNKNOWN);
		ex.printStackTrace();
		setLastAction();
	}

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

	public void setLastDirection(int lastDirection) {
		this.lastDirection = lastDirection;
	}

	public void setLastAction() {
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
