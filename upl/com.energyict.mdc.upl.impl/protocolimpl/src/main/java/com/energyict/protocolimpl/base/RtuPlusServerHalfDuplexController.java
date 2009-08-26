package com.energyict.protocolimpl.base;

import java.io.IOException;

import com.energyict.dialer.core.HalfDuplexController;

/**
 * @author jme
 * @since 26-aug-2009
 */
public class RtuPlusServerHalfDuplexController implements HalfDuplexController {

	private HalfDuplexController innerHalfDuplexController;

	public RtuPlusServerHalfDuplexController(HalfDuplexController controller) {
		this.innerHalfDuplexController = controller;
	}

	public HalfDuplexController getInnerHalfDuplexController() {
		return this.innerHalfDuplexController;
	}

	public void setInnerHalfDuplexController(HalfDuplexController innerHalfDuplexController) {
		this.innerHalfDuplexController = innerHalfDuplexController;
	}

	public void request2Receive(int nrOfBytes) {
		getInnerHalfDuplexController().request2ReceiveRS485(nrOfBytes);
	}

	public void request2Send(int nrOfBytes) {
		getInnerHalfDuplexController().request2SendRS485();
	}

	public void request2ReceiveRS485(int nrOfBytes) {
		getInnerHalfDuplexController().request2ReceiveRS485(nrOfBytes);
	}

	public void request2ReceiveV25(int nrOfBytes) {
		getInnerHalfDuplexController().request2SendV25(nrOfBytes);
	}

	public void request2SendRS485() {
		getInnerHalfDuplexController().request2SendRS485();
	}

	public void request2SendV25(int nrOfBytes) {
		getInnerHalfDuplexController().request2SendV25(nrOfBytes);
	}

	public void setDTR(boolean dtr) throws IOException {
		getInnerHalfDuplexController().setDTR(dtr);
	}

	public void setDelay(long halfDuplexTXDelay) {
		getInnerHalfDuplexController().setDelay(halfDuplexTXDelay);
	}

	public void setRTS(boolean rts) throws IOException {
		getInnerHalfDuplexController().setRTS(rts);
	}

	public boolean sigCD() throws IOException {
		return getInnerHalfDuplexController().sigCD();
	}

	public boolean sigCTS() throws IOException {
		return getInnerHalfDuplexController().sigCTS();
	}

}
