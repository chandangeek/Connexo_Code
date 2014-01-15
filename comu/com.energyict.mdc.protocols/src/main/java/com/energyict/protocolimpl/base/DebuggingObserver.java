package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.dialer.core.InputStreamObserver;
import com.energyict.mdc.protocol.api.dialer.core.OutputStreamObserver;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Date;

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
	private final boolean		asciiMode;
    private final boolean       mode7e1;

	private int					lastDirection		= DIRECTION_UNKNOWN;
	private long				lastAction			= 0L;

	/**
	 * Create a new {@link DebuggingObserver}.
	 * @param fileName
	 * @param showCommunication
	 */
	public DebuggingObserver(String fileName, boolean showCommunication) {
		this(fileName, showCommunication, false);
	}

	/**
	 * Create a new {@link DebuggingObserver}.
	 * @param fileName
	 * @param showCommunication
	 */
	public DebuggingObserver(String fileName, boolean showCommunication, boolean asciiMode) {
        this(fileName, showCommunication, asciiMode, false);
	}

    public DebuggingObserver(String fileName, boolean showCommunication, boolean asciiMode, boolean mode7E1) {
        this.fileName = fileName;
        this.showCommunication = showCommunication;
        this.asciiMode  = asciiMode;
        log(CRLF + CRLF + "Observer started " + new Date() + "[" + getClass().getCanonicalName() + "]" + CRLF);
        this.mode7e1 = mode7E1;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.tools.InputStreamObserver#read(byte[])
      */
	public void read(byte[] b) {
		byte[] bytes = convertRead7E1(b.clone());
        if (!isReading() || isLastActionLongTimeAgo()) {
			log(CRLF);
			log("RX[" + System.currentTimeMillis() + "] <= ");
			setLastDirection(DIRECTION_READING);
		}
		log(asciiMode ? ProtocolTools.getAsciiFromBytes(bytes) : ProtocolTools.getHexStringFromBytes(bytes));
		setLastAction();
	}

    private byte[] convertRead7E1(byte[] bytes) {
        if (mode7e1) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] &= 0x7F;
            }
            return bytes;
        } else {
            return bytes;
        }
    }

    private byte[] convertWrite7E1(byte[] bytes) {
        if (mode7e1) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] &= 0x7F;
            }
            return bytes;
        } else {
            return bytes;
        }
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
		byte[] bytes = convertWrite7E1(b.clone());
        if (!isWriting() || isLastActionLongTimeAgo()) {
			log(CRLF);
			log("TX[" + System.currentTimeMillis() + "] => ");
			setLastDirection(DIRECTION_WRITING);
		}
		log(asciiMode ? ProtocolTools.getAsciiFromBytes(bytes) : ProtocolTools.getHexStringFromBytes(bytes));
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
