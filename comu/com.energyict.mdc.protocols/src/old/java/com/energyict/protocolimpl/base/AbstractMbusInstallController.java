/**
 *
 */
package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

/**
 * @author gna
 * @since 16-feb-2010
 *
 */
public abstract class AbstractMbusInstallController implements MbusInstallController {

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
	public AbstractMbusInstallController(MeterProtocol protocol) {
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
}
