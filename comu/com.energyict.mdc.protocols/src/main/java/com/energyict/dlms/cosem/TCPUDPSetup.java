/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;

import java.io.IOException;

/**
 * @author gna
 *
 * 		This IC allows modelling the setup of the TCP or UDP sub-layer of
 *         the COSEM TCP or UDP based transport layer of a TCP-UDP/IP based
 *         communication profile. In TCP-UDP/IP based communication profiles,
 *         all AAs between a physical device hosting one or more COSEM client
 *         application processes and a physical device hosting one or more COSEM
 *         server application processes rely on a single TCP or UDP connection.
 *         The TCP or UDP entity is wrapped in the COSEM TCP-UDP based transport
 *         layer. Within a physical device, each application process � client
 *         application process or server logical device � is bound to a Wrapper
 *         Port (WPort). The binding is done with the help of the SAP Assignment
 *         object. See 4.4.3. On the other hand, a COSEM TCP or UDP based
 *         transport layer may be capable to support more than one TCP or UDP
 *         connections, between a physical device and several peer physical
 *         devices hosting COSEM application processes.
 *
 *         NOTE: When a COSEM physical device supports various data link layers
 *         � for example Ethernet and PPP � an instance of the TCP-UDP setup
 *         object is necessary for each of them.
 */
public class TCPUDPSetup extends AbstractCosemObject {

	static final byte[] LN = new byte[] { 0, 0, 25, 0, 0, (byte) 255 };

	/** Attributes */
	private Unsigned16 port = null; // Holds the TCP/UDP port number on which
									// teh physical device is listening for the
									// DLMS/COSEM application
	private OctetString ipReference = null; // Reference an IP setup object by
											// its logical name
	private Unsigned16 mss = null; // Defines the Maximum Segment Size, if it's
									// not present the default is 576
	private Unsigned8 nbOfSimConn = null; // Maximum number of simultaneous
											// connections the cosem TCP/UDP
											// transport layer is able to
											// support
	private Unsigned16 inactivityTimeout = null; // Defines the time in seconds
													// over which, if no frame
													// is received from the
													// client, the TCP
													// connection
	// shall be aborted

	/** Attribute numbers */
	private static final int ATTRB_PORT = 2;
	private static final int ATTRB_IPREFERENCE = 3;
	private static final int ATTRB_MSS = 4;
	private static final int ATTRB_NBOFSIMCONN = 5;
	private static final int ATTRB_INACTIVITYTIMEOUT = 6;

	/** Method invoke */
	// none

	public TCPUDPSetup(ProtocolLink protocolLink,
			ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	/**
	 * Use in DLMS framework where you do not use the OBJECTLIST
	 * @param protocolLink
	 */
	public TCPUDPSetup(ProtocolLink protocolLink) {
		super(protocolLink, new ObjectReference(LN));
	}

	protected int getClassId() {
		return DLMSClassId.TCP_UDP_SETUP.getClassId();
	}

	public static ObisCode getDefaultObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	/**
	 * Read the inactivity timeout from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 readInactivityTimeout() throws IOException {
		try {
			return this.inactivityTimeout = new Unsigned16(
					getLNResponseData(ATTRB_INACTIVITYTIMEOUT), 0);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the inactivityTimeout. "
					+ e.getMessage());
		}
	}

	/**
	 * Get the inactivity timeout, if it's not read yet, then read if from the
	 * device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 getInactivityTimeout() throws IOException {
		if (this.inactivityTimeout == null) {
			return readInactivityTimeout();
		}
		return this.inactivityTimeout;
	}

	/**
	 * Write the given inactivityTimeout to the device
	 *
	 * @param inactivityTimeout
	 * @throws java.io.IOException
	 */
	public void writeInactivityTimeout(int inactivityTimeout)
			throws IOException {
		writeInactivityTimeout(new Unsigned16(inactivityTimeout));
	}

	/**
	 * Write the given inactivityTimeout to the device
	 *
	 * @param inactivityTimeout
	 * @throws java.io.IOException
	 */
	public void writeInactivityTimeout(Unsigned16 inactivityTimeout)
			throws IOException {
		try {
			write(ATTRB_INACTIVITYTIMEOUT, inactivityTimeout
					.getBEREncodedByteArray());
			this.inactivityTimeout = inactivityTimeout;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the inactivityTimeout. "
					+ e.getMessage());
		}
	}

	/**
	 * Read the logical name of the IPReference from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public OctetString readIPReference() throws IOException {
		try {
			return this.ipReference = new OctetString(
					getLNResponseData(ATTRB_IPREFERENCE), 0);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the ipReference. "
					+ e.getMessage());
		}
	}

	/**
	 * Get the logical name of the IPReference, if it's not read yet, then read
	 * it from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public OctetString getIPReference() throws IOException {
		if (this.ipReference == null) {
			return readIPReference();
		}
		return this.ipReference;
	}

	/**
	 * Write the given IPReference to the device
	 *
	 * @param ipReference
	 * @throws java.io.IOException
	 */
	public void writeIPReference(String ipReference) throws IOException {
		writeIPReference(OctetString.fromString(ipReference));
	}

	/**
	 * Write the given IPReference to the device
	 *
	 * @param ipReference
	 * @throws java.io.IOException
	 */
	public void writeIPReference(OctetString ipReference) throws IOException {
		try {
			write(ATTRB_IPREFERENCE, ipReference.getBEREncodedByteArray());
			this.ipReference = ipReference;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the ipReference. "
					+ e.getMessage());
		}
	}

	/**
	 * Read the maximum segment size from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 readMss() throws IOException {
		try {
			return this.mss = new Unsigned16(getLNResponseData(ATTRB_MSS), 0);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the mss. "
					+ e.getMessage());
		}
	}

	/**
	 * Get the maximum segment size, if it's not read yet, read if from the
	 * device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 getMss() throws IOException {
		if (this.mss == null) {
			return readMss();
		}
		return this.mss;
	}

	/**
	 * Write the maximum segment size to the device
	 *
	 * @param mss
	 * @throws java.io.IOException
	 */
	public void writeMss(int mss) throws IOException {
		writeMss(new Unsigned16(mss));
	}

	/**
	 * Write the maximum segment size to the device
	 *
	 * @param mss
	 * @throws java.io.IOException
	 */
	public void writeMss(Unsigned16 mss) throws IOException {
		try {
			write(ATTRB_MSS, mss.getBEREncodedByteArray());
			this.mss = mss;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the mss. " + e.getMessage());
		}
	}

	/**
	 * Read the TCP/UDP portnumber from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 readPort() throws IOException {
		try {
			return this.port = new Unsigned16(getLNResponseData(ATTRB_PORT), 0);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the port. "
					+ e.getMessage());
		}
	}

	/**
	 * Get the TCP/UDP port number, if it's not read yet, read it from the
	 * device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 getPort() throws IOException {
		if (this.port == null) {
			return readPort();
		}
		return this.port;
	}

	/**
	 * Write the given portnumber to the device
	 *
	 * @param port
	 * @throws java.io.IOException
	 */
	public void writePort(int port) throws IOException {
		writePort(new Unsigned16(port));
	}

	/**
	 * Write the given portnumber to the device
	 *
	 * @param port
	 * @throws java.io.IOException
	 */
	public void writePort(Unsigned16 port) throws IOException {
		try {
			write(ATTRB_PORT, port.getBEREncodedByteArray());
			this.port = port;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the port. " + e.getMessage());
		}
	}

	/**
	 * Read the maximum number of simultaneous connections from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned8 readNbOfSimConn() throws IOException {
		try {
			return this.nbOfSimConn = new Unsigned8(
					getLNResponseData(ATTRB_NBOFSIMCONN), 0);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the nbOfSimConn. "
					+ e.getMessage());
		}
	}

	/**
	 * Get the maximum number of simultaneous connections, if it's not read yet,
	 * read it from the device
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned8 getNbOfSimConn() throws IOException {
		if (this.nbOfSimConn == null) {
			return readNbOfSimConn();
		}
		return this.nbOfSimConn;
	}

	/**
	 * Write the given number of simultaneous connections to the device
	 *
	 * @param nbOfSimConn
	 * @throws java.io.IOException
	 */
	public void writeNbOfSimConn(int nbOfSimConn) throws IOException {
		writeNbOfSimConn(new Unsigned8(nbOfSimConn));
	}

	/**
	 * Write the given number of simultaneous connections to the device
	 *
	 * @param nbOfSimConn
	 * @throws java.io.IOException
	 */
	public void writeNbOfSimConn(Unsigned8 nbOfSimConn) throws IOException {
		try {
			write(ATTRB_NBOFSIMCONN, nbOfSimConn.getBEREncodedByteArray());
			this.nbOfSimConn = nbOfSimConn;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the nbOfSimConn. "
					+ e.getMessage());
		}
	}
}
