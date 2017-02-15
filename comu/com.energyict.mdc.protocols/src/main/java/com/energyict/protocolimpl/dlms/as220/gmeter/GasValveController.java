/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.protocolimpl.base.AbstractContactorController;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

import java.io.IOException;

/**
 * This class is used to change the valve state of an G-Meter connected to the AS220 device
 *
 * @author jme
 */
public class GasValveController extends AbstractContactorController {

	public static final ObisCode	VALVE_DISCONNECTOR_OBISCODE	= ObisCode.fromString("0.0.24.4.0.255");

	private static final int ARM = 2;

	public GasValveController(AS220 protocol) {
		super(protocol);
	}

	/**
	 * Getter for the {@link GasDevice}
	 *
	 * @return the parent {@link GasDevice}
	 */
	public GasDevice getGasDevice() {
		return (GasDevice) getProtocol();
	}

	/**
	 * {@inheritDoc}
	 */
	public void doArm() throws IOException {
		getGasDevice().getLogger().info("ARM message received");
		// TOTEST Don't know if what the functionality is
		getGasDevice().getCosemObjectFactory().getDisconnector(VALVE_DISCONNECTOR_OBISCODE).writeControlState(new TypeEnum(ARM));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doConnect() throws IOException {
		getGasDevice().getLogger().info("CONNECT message received");
		getGasDevice().getCosemObjectFactory().getDisconnector(getGasDevice().getMeterConfig().
				getMbusDisconnectControl(getGasDevice().getPhysicalAddress()).getObisCode()).remoteReconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	public void doDisconnect() throws IOException {
		getGasDevice().getLogger().info("DISCONNECT message received");
		getGasDevice().getCosemObjectFactory().getDisconnector(getGasDevice().getMeterConfig().
				getMbusDisconnectControl(getGasDevice().getPhysicalAddress()).getObisCode()).remoteDisconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	public BreakerStatus getContactorState() throws IOException {
		return BreakerStatus.values()[getGasDevice().getCosemObjectFactory().getDisconnector(getGasDevice().getMeterConfig().
				getMbusDisconnectControl(getGasDevice().getPhysicalAddress()).getObisCode()).getControlState().getValue()];
	}

}
