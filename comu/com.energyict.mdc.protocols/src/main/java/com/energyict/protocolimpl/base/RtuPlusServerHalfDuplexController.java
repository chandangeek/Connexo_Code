package com.energyict.protocolimpl.base;

import com.energyict.dialer.core.HalfDuplexController;

import java.io.IOException;

/**
 * This class is used to replace the default HalfDuplexController, received from
 * mdw during the init. On the RTU+ Server, the RS485 driver uses inverted logic
 * to drive the RTS pin in halfduplex mode. This could be avoided to patch all
 * the request2Receive() and request2Send() calls to the request2ReceiveRS485()
 * and request2SendRS485() methods. This is what this class does.
 * @author jme
 * @since 26-aug-2009
 */
public class RtuPlusServerHalfDuplexController implements HalfDuplexController {

	private HalfDuplexController innerHalfDuplexController;

	/**
	 * Default constructor for the RtuPlusServerHalfDuplexController
	 * @param controller The HalfDuplexController to patch the methods to.
	 */
	public RtuPlusServerHalfDuplexController(HalfDuplexController controller) {
		this.innerHalfDuplexController = controller;
	}

	/**
	 * Getter for the current inner halfDuplexController
	 * @return The current inner HalfDuplexController
	 */
	public HalfDuplexController getInnerHalfDuplexController() {
		return this.innerHalfDuplexController;
	}

	/**
	 * Setter to change the inner HalfDuplexController
	 * @param innerHalfDuplexController
	 */
	public void setInnerHalfDuplexController(HalfDuplexController innerHalfDuplexController) {
		this.innerHalfDuplexController = innerHalfDuplexController;
	}

	/**
	 * This method is patched to the request2ReceiveRS485(int nrOfBytes) method of the inner {@link HalfDuplexController}
	 */
	public void request2Receive(int nrOfBytes) {
		getInnerHalfDuplexController().request2ReceiveRS485(nrOfBytes);
	}

	/**
	 * This method is patched to the request2SendRS485() method of the inner {@link HalfDuplexController}
	 */
	public void request2Send(int nrOfBytes) {
		getInnerHalfDuplexController().request2SendRS485();
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void request2ReceiveRS485(int nrOfBytes) {
		getInnerHalfDuplexController().request2ReceiveRS485(nrOfBytes);
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void request2ReceiveV25(int nrOfBytes) {
		getInnerHalfDuplexController().request2SendV25(nrOfBytes);
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void request2SendRS485() {
		getInnerHalfDuplexController().request2SendRS485();
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void request2SendV25(int nrOfBytes) {
		getInnerHalfDuplexController().request2SendV25(nrOfBytes);
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void setDTR(boolean dtr) throws IOException {
		getInnerHalfDuplexController().setDTR(dtr);
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void setDelay(long halfDuplexTXDelay) {
		getInnerHalfDuplexController().setDelay(halfDuplexTXDelay);
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public void setRTS(boolean rts) throws IOException {
		getInnerHalfDuplexController().setRTS(rts);
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public boolean sigCD() throws IOException {
		return getInnerHalfDuplexController().sigCD();
	}

	/**
	 * This method is patched to the same method of the inner {@link HalfDuplexController}
	 */
	public boolean sigCTS() throws IOException {
		return getInnerHalfDuplexController().sigCTS();
	}

}
