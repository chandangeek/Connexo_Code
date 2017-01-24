package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import java.io.IOException;

/**
 * @author jme
 */
public abstract class AbstractContactorController implements ContactorController {

	/**
	 * The {@link MeterProtocol} who's owning this {@link ContactorController}
	 */
	private final MeterProtocol	protocol;

	/**
	 * Constructor given a {@link MeterProtocol}
	 *
	 * @param protocol The {@link MeterProtocol} who's owning this
	 * {@link ContactorController}
	 */
	public AbstractContactorController(MeterProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * Getter for the {@link MeterProtocol} who's owning this
	 * {@link ContactorController}
	 *
	 * @return the {@link MeterProtocol}
	 */
	protected MeterProtocol getProtocol() {
		return protocol;
	}

	public void doArm() throws IOException {
		throw new UnsupportedException("com.energyict.protocolimpl.base.AbstractContactorController.doArm() not implemented.");
	}

	public void doConnect() throws IOException {
		throw new UnsupportedException("com.energyict.protocolimpl.base.AbstractContactorController.doConnect() not implemented.");
	}

	public void doDisconnect() throws IOException {
		throw new UnsupportedException("com.energyict.protocolimpl.base.AbstractContactorController.doDisconnect() not implemented.");
	}

	public BreakerStatus getContactorState() throws IOException {
		throw new UnsupportedException("com.energyict.protocolimpl.base.AbstractContactorController.getContactorState() not implemented.");
	}

}
