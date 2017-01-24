package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author fbo
 *
 */
public class ProfileParser {

	private PPM ppm = null;
	private RegisterFactory rFactory = null;
	private Date meterTime;

	/* constants Byte Size */
	static final int AA_BS = 3;

	private Assembler[] assemblerTable = new Assembler[256];
	private Assembly assembly = null;

	private ProfileData targetProfileData = null;

	private int nrOfChannels = 1;
	private int integrationPeriod = 30;
	private ScalingFactor scalingFactor = null;
	private LoadProfileDefinition loadDef = null;

	private FFAssembler ffAssembler = new FFAssembler();
	private DayAssembler dayAssembler = new DayAssembler(this);
	private NumberAssembler numberAssembler = new NumberAssembler(this);
	private AAAssembler aaAssembler = new AAAssembler(this);

	public ProfileParser(PPM ppm, RegisterFactory registerFactory, Date meterTime, LoadProfileDefinition loadDef) throws IOException {
		this.ppm = ppm;
		this.rFactory = registerFactory;
		this.meterTime = meterTime;
		this.loadDef = loadDef;
		this.nrOfChannels = loadDef.getNrOfChannels();

		this.integrationPeriod = this.rFactory.getIntegrationPeriod().intValue() * 60;
		this.scalingFactor = this.rFactory.getScalingFactor();

		setAssemblerTable(0x00, 0xFF, this.numberAssembler);
		setAssemblerTable(0xFF, 0xFF, this.ffAssembler);
		setAssemblerTable(0xAA, 0xAA, this.aaAssembler);
		setAssemblerTable(0xE4, 0xE4, this.dayAssembler);
		setAssemblerTable(0x00, 0x99, this.numberAssembler);

	}

	public void setInput(InputStream inputStream) {
		this.assembly = new Assembly(inputStream);
	}

	private void setAssemblerTable(int from, int to, Assembler assembler) {
		for (int i = from; i <= to; i++) {
			if ((i >= 0) && (i < this.assemblerTable.length)) {
				this.assemblerTable[i] = assembler;
			}
		}
	}

	public int getNrOfChannels() {
		return this.nrOfChannels;
	}

	public NumberAssembler getNumberAssembler() {
		return numberAssembler;
	}

	public void match() throws IOException {

		int character = this.assembly.read();
		do {
			this.assembly.push(new Byte((byte) character));
			this.assemblerTable[character].workOn(this.assembly);
			character = this.assembly.read();
		} while (character != -1);

		if (this.assembly.getTarget() != null) {
			this.dayAssembler.createProfileData((Day) this.assembly.getTarget());
		}

	}

	public ProfileData getProfileData() throws IOException {
		if (this.targetProfileData == null) {
			this.targetProfileData = new ProfileData();
		}
		this.targetProfileData.setChannelInfos(this.loadDef.toChannelInfoList());
		match();

		return this.targetProfileData;
	}

	public PPM getPpm() {
		return ppm;
	}

	public int getIntegrationPeriod() {
		return integrationPeriod;
	}

	public LoadProfileDefinition getLoadDef() {
		return loadDef;
	}

	public ScalingFactor getScalingFactor() {
		return scalingFactor;
	}

	public Date getMeterTime() {
		return meterTime;
	}

	protected ProfileData getTargetProfileData() {
		return targetProfileData;
	}

	protected Assembly getAssembly() {
		return assembly;
	}

}


