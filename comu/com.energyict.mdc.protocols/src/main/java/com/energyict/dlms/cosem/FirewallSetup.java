/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.FirewallSetupAttributes;

import java.io.IOException;

import static com.energyict.dlms.cosem.attributes.FirewallSetupAttributes.ENABLED_BY_DEFAULT;
import static com.energyict.dlms.cosem.attributes.FirewallSetupAttributes.GPRS_SETUP;
import static com.energyict.dlms.cosem.attributes.FirewallSetupAttributes.IS_ACTIVE;
import static com.energyict.dlms.cosem.attributes.FirewallSetupAttributes.LAN_SETUP;
import static com.energyict.dlms.cosem.attributes.FirewallSetupAttributes.WAN_SETUP;
import static com.energyict.dlms.cosem.methods.FirewallSetupMethods.ACTIVATE;
import static com.energyict.dlms.cosem.methods.FirewallSetupMethods.DEACTIVATE;

/**
 * Firewall setup object (RTU+Server custom IC).
 *
 * @author alex
 */
public final class FirewallSetup extends AbstractCosemObject {

	/** The default OBIS code. */
	private static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.128.0.1.255");

	/**
	 * Firewall configuration for an interface.
	 *
	 * @author alex
	 */
	public static final class InterfaceFirewallConfiguration {

		/** Allow DLMS. */
		private final boolean allowDLMS;

		/** Allow HTTP. */
		private final boolean allowHTTP;

		/** Allow SSH. */
		private final boolean allowSSH;

		/**
		 * Create a new instance.
		 *
		 * @param 	allowDLMS		Indicates whether to allow DLMS.
		 * @param 	allowHTTP		Indicates whether to allow HTTP.
		 * @param 	allowSSH		Indicates whether to allow SSH.
		 */
		public InterfaceFirewallConfiguration(final boolean allowDLMS, final boolean allowHTTP, final boolean allowSSH) {
			this.allowDLMS = allowDLMS;
			this.allowHTTP = allowHTTP;
			this.allowSSH = allowSSH;
		}

		/**
		 * Parses the structure and returns an {@link InterfaceFirewallConfiguration}.
		 *
		 * @param 		structure		The structure to parse.
		 *
		 * @return		The parsed structure.
		 */
		private static final InterfaceFirewallConfiguration fromStructure(final Structure structure) throws IOException {
			if (structure.nrOfDataTypes() == 3) {
				final boolean allowSSH = structure.getDataType(0, BooleanObject.class).getState();
				final boolean allowDLMS = structure.getDataType(1, BooleanObject.class).getState();
				final boolean allowHTTP = structure.getDataType(2, BooleanObject.class).getState();

				return new InterfaceFirewallConfiguration(allowDLMS, allowHTTP, allowSSH);
			} else {
				throw new ProtocolException("Port setup structure has wrong number of elements : expected [3] elements, but got [" + structure.nrOfDataTypes() + "]");
			}
		}

		/**
		 * Creates a structure from this object to send to the device.
		 *
		 * @return		A structure.
		 */
		private final Structure toStructure() {
			return new Structure(new BooleanObject(this.allowSSH), new BooleanObject(this.allowDLMS), new BooleanObject(this.allowHTTP));
		}

		/**
		 * @return the allowDLMS
		 */
		public final boolean isAllowDLMS() {
			return allowDLMS;
		}

		/**
		 * @return the allowHTTP
		 */
		public final boolean isAllowHTTP() {
			return allowHTTP;
		}

		/**
		 * @return the allowSSH
		 */
		public final boolean isAllowSSH() {
			return allowSSH;
		}
	}

	/**
	 * Create a new instance.
	 *
	 * @param 	protocolLink			The protocol link.
	 * @param 	objectReference			The object reference.
	 */
	public FirewallSetup(final ProtocolLink protocolLink, final ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final int getClassId() {
		return DLMSClassId.FIREWALL_SETUP.getClassId();
	}

	/**
	 * Indicates whether the firewall is enabled by default.
	 *
	 * @return		<code>true</code> if it is enabled by default, <code>false</code> if not.
	 *
	 * @throws java.io.IOException        If something goes wrong during the read.
	 */
	public final boolean isEnabledByDefault() throws IOException {
		return this.readDataType(ENABLED_BY_DEFAULT, BooleanObject.class).getState();
	}

	/**
	 * Sets whether the FW is enabled by default.
	 *
	 * @param 		enabled			<code>true</code> if the FW should be enabled by default, <code>false</code> if not.
	 *
	 * @throws java.io.IOException        In case of an IO error.
	 */
	public final void setEnabledByDefault(final boolean enabled) throws IOException {
		this.write(ENABLED_BY_DEFAULT, new BooleanObject(enabled));
	}

	/**
	 * Returns the port setup for the WAN port.
	 *
	 * @return		The port setup for the WAN port.
	 *
	 * @throws java.io.IOException        In case of an IO error.
	 */
	public final InterfaceFirewallConfiguration getWANPortSetup() throws IOException {
		return this.getPortSetup(WAN_SETUP);
	}

	/**
	 * Sets the port setup for the WAN port.
	 *
	 * @param 		setup			The new WAN port setup.
	 *
	 * @throws java.io.IOException        In case of an IO during the write.
	 */
	public final void setWANPortSetup(final InterfaceFirewallConfiguration setup) throws IOException {
		this.setPortsetup(WAN_SETUP, setup);
	}

	/**
	 * Returns the port setup for the LAN port.
	 *
	 * @return		The port setup for the LAN port.
	 *
	 * @throws java.io.IOException        In case of an IO error.
	 */
	public final InterfaceFirewallConfiguration getLANPortSetup() throws IOException {
		return this.getPortSetup(LAN_SETUP);
	}

	/**
	 * Sets the port setup for the LAN port.
	 *
	 * @param 		setup			The new LAN port setup.
	 *
	 * @throws java.io.IOException        In case of an IO during the write.
	 */
	public final void setLANPortSetup(final InterfaceFirewallConfiguration setup) throws IOException {
		this.setPortsetup(LAN_SETUP, setup);
	}

	/**
	 * Returns the port setup for the GPRS port.
	 *
	 * @return		The port setup for the GPRS port.
	 *
	 * @throws java.io.IOException        In case of an IO error.
	 */
	public final InterfaceFirewallConfiguration getGPRSPortSetup() throws IOException {
		return this.getPortSetup(GPRS_SETUP);
	}

	/**
	 * Sets the port setup for the GPRS port.
	 *
	 * @param 		setup			The new GPRS port setup.
	 *
	 * @throws java.io.IOException        In case of an IO during the write.
	 */
	public final void setGPRSPortSetup(final InterfaceFirewallConfiguration setup) throws IOException {
		this.setPortsetup(GPRS_SETUP, setup);
	}

	/**
	 * Indicates whether the firewall is active on the device.
	 *
	 * @return		<code>true</code> if the FW is active, <code>false</code> if it's not.
	 *
	 * @throws java.io.IOException        If an IO error occurs during the read.
	 */
	public final boolean isActive() throws IOException {
		return this.readDataType(IS_ACTIVE, BooleanObject.class).getState();
	}

	/**
	 * Returns the default OBIS code for this IC.
	 *
	 * @return	The default OBIS code for this IC.
	 */
	public static final ObisCode getDefaultObisCode() {
		return DEFAULT_OBIS_CODE;
	}

	/**
	 * Returns the port setup for a particular port.
	 *
	 * @param 		attribute		The attribute to write.
	 *
	 * @return		The corresponding port setup.
	 *
	 * @throws java.io.IOException        If an IO error occurs while reading the port setup.
	 */
	private final InterfaceFirewallConfiguration getPortSetup(final FirewallSetupAttributes attribute) throws IOException {
		return InterfaceFirewallConfiguration.fromStructure(this.readDataType(attribute, Structure.class));
	}

	/**
	 * Sets the port setup for the particular attribute.
	 *
	 * @param 	attribute			The attribute.
	 * @param 	setup				The setup.
	 *
	 * @throws java.io.IOException            If an IO error occurs while doing the write.
	 */
	private final void setPortsetup(final FirewallSetupAttributes attribute, final InterfaceFirewallConfiguration setup) throws IOException {
		this.write(attribute, setup.toStructure());
	}

	/**
	 * Activates the firewall.
	 *
	 * @throws java.io.IOException        If an IO error occurs during the method invocation.
	 */
	public final void activate() throws IOException {
		this.methodInvoke(ACTIVATE);
	}

	/**
	 * Deactivates the firewall.
	 *
	 * @throws java.io.IOException        If an IO error occurs during the method invocation.
	 */
	public final void deactivate() throws IOException {
		this.methodInvoke(DEACTIVATE);
	}
}
