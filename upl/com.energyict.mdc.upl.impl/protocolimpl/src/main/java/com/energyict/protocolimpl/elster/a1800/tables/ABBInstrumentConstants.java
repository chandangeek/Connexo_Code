/*
 * AnswerParametersTableForRemotePorts.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a1800.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author jsm
 */
public class ABBInstrumentConstants extends AbstractTable { 
    /*	
     * 
	Instrumentation Constants (corresponds to Class 54)


	MT-55 contains the constants needed for instrumentation sources. The harmonic constants are the 
	default "starting point" and are adjusted based on the actual line frequency. The frequency 
	adjustment is performed one time for request that requires multiple harmonic sources (i.e. THD).


	SPECIAL_PROPERTIES STORAGE 1 
	READ_ACCESS_PW 4 
	WRITE_ACCESS_PW 1 
    */    
    
	private String dsp_version;
	private String dsp_revision; //TODO: HEX?
	private int harmonic_number;
	private BigDecimal phase_c_constant; //TODO: SF 48:16?
	private BigDecimal phase_b_constant; //TODO: SF 48:16?
	private BigDecimal phase_a_constant; //TODO: SF 48:16?
	private BigDecimal fundamental_constant;  //TODO: SF 48:32?
	private BigDecimal watts_mult;
	private BigDecimal harmonic_current_mult; //TODO: SF 48:24?
	private BigDecimal harmonic_voltage_mult; //TODO: SF 48:24?
	private BigDecimal frequency_mult; //TODO: SF 48:24?
	private BigDecimal voltage_mult; //TODO: SF 48:24?
	private BigDecimal current_mult; //TODO: SF 48:24?
    
    /** Creates a new instance of ABBInstrumentConstants */
    public ABBInstrumentConstants(TableFactory tableFactory) {
        super(tableFactory,new TableIdentification(55,true));
    }
 
    public static void main(String [] args) throws Exception {
    	ABBInstrumentConstants a = new ABBInstrumentConstants(null);
    	byte[] tableData = {
    			0x41, 0x47, 
    			(byte) 0xD8, 
    			0x0F, 
    			(byte) 0xCD, 0x0A, 0x0E, 0x01, 0x00, 0x00, 
    			(byte) 0xCD, 0x0A, 0x0E, 0x01, 0x00, 0x00, 
    			(byte) 0xCD, 0x0A, 0x0E, 0x01, 0x00, 0x00, 
    			
    			(byte) 0xB5, (byte) 0xF5, 0x66, 0x00, 0x00, 0x00, 
    			(byte) 0x8A, (byte) 0x91, 0x7C, 0x77, 0x02, 0x00, 
    			(byte) 0xF0, 0x16, 0x7B, 0x6A, 0x00, 0x00, 
    			(byte) 0xB0, 0x14, 0x6D, (byte) 0xDC, 0x0B, 0x00, 
    			0x00, 0x00, 0x60, (byte) 0x9F, 0x0F, 0x00, 
    			(byte) 0xF9, (byte) 0xD5, 0x13, 0x63, 0x08, 0x00, 
    			0x4D, 0x16, 0x4B, 0x4B, 0x00, 0x00
    	};
		a.parse(tableData );
		System.out.println(a);
    }
    
    protected void parse(byte[] tableData) throws IOException {
//        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setDsp_version(""+(char)tableData[offset++]+(char)tableData[offset++]);
        setDsp_revision(ProtocolUtils.hex2String(C12ParseUtils.getInt (tableData,offset++)));
        
        setHarmonic_number(tableData[offset++]);
        //48:16
        //DIVISOR 100 * 256 * 256 
        setPhase_c_constant(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 4)).divide(new BigDecimal(100*256*256)));
        offset+=6;
        //48:16
        //DIVISOR 100 * 256 * 256 
        setPhase_b_constant(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 4)).divide(new BigDecimal(100*256*256)));
        offset+=6;
        //48:16
        //DIVISOR 100 * 256 * 256 
        setPhase_a_constant(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(100*256*256)));
        offset+=6;

       //DIVISOR 256 * 256 * 256 * 256 
        setFundamental_constant(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(256*256)).divide(new BigDecimal(256*256)));
        offset+=6;
        
        setWatts_mult(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(256*256)));
        offset+=6;
        setHarmonic_current_mult(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(256*256*256)));
        offset+=6;
        setHarmonic_voltage_mult(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(256*256*256)));
        offset+=6;
        setFrequency_mult(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(256*256*256)));
        offset+=6;
        setVoltage_mult(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 6)).divide(new BigDecimal(256*256*256)));
        offset+=6;
        setCurrent_mult(new BigDecimal(ProtocolUtils.getLongLE(tableData, offset, 4)).divide(new BigDecimal(256*256*256)));
        offset+=6;
        
        
//        setNrOfRingsOutside(C12ParseUtils.getInt(tableData,offset++));
//        setWindows(new Window[2]);
//        for (int i=0;i<getWindows().length;i++) {
//            getWindows()[i] = new Window(tableData, offset, getTableFactory());
//            offset+=Window.getSize(getTableFactory());
//        }
    }

	@Override
	public String toString() {
		return "ABBInstrumentConstants [dsp_version=" + dsp_version
				+ ", dsp_revision=" + dsp_revision + ", harmonic_number="
				+ harmonic_number + ", phase_c_constant=" + phase_c_constant
				+ ", phase_b_constant=" + phase_b_constant
				+ ", phase_a_constant=" + phase_a_constant
				+ ", fundamental_constant=" + fundamental_constant
				+ ", watts_mult=" + watts_mult + ", harmonic_current_mult="
				+ harmonic_current_mult + ", harmonic_voltage_mult="
				+ harmonic_voltage_mult + ", frequency_mult=" + frequency_mult
				+ ", voltage_mult=" + voltage_mult + ", current_mult="
				+ current_mult + "]";
	}


	public String getDsp_version() {
		return dsp_version;
	}

	public void setDsp_version(String dsp_version) {
		this.dsp_version = dsp_version;
	}

	public String getDsp_revision() {
		return dsp_revision;
	}

	public void setDsp_revision(String dsp_revision) {
		this.dsp_revision = dsp_revision;
	}

	public int getHarmonic_number() {
		return harmonic_number;
	}

	public void setHarmonic_number(int harmonic_number) {
		this.harmonic_number = harmonic_number;
	}

	public BigDecimal getPhase_c_constant() {
		return phase_c_constant;
	}

	public void setPhase_c_constant(BigDecimal phase_c_constant) {
		this.phase_c_constant = phase_c_constant;
	}

	public BigDecimal getPhase_b_constant() {
		return phase_b_constant;
	}

	public void setPhase_b_constant(BigDecimal phase_b_constant) {
		this.phase_b_constant = phase_b_constant;
	}

	public BigDecimal getPhase_a_constant() {
		return phase_a_constant;
	}

	public void setPhase_a_constant(BigDecimal phase_a_constant) {
		this.phase_a_constant = phase_a_constant;
	}

	public BigDecimal getFundamental_constant() {
		return fundamental_constant;
	}

	public void setFundamental_constant(BigDecimal fundamental_constant) {
		this.fundamental_constant = fundamental_constant;
	}

	public BigDecimal getWatts_mult() {
		return watts_mult;
	}

	public void setWatts_mult(BigDecimal watts_mult) {
		this.watts_mult = watts_mult;
	}

	public BigDecimal getHarmonic_current_mult() {
		return harmonic_current_mult;
	}

	public void setHarmonic_current_mult(BigDecimal harmonic_current_mult) {
		this.harmonic_current_mult = harmonic_current_mult;
	}

	public BigDecimal getHarmonic_voltage_mult() {
		return harmonic_voltage_mult;
	}

	public void setHarmonic_voltage_mult(BigDecimal harmonic_voltage_mult) {
		this.harmonic_voltage_mult = harmonic_voltage_mult;
	}

	public BigDecimal getFrequency_mult() {
		return frequency_mult;
	}

	public void setFrequency_mult(BigDecimal frequency_mult) {
		this.frequency_mult = frequency_mult;
	}

	public BigDecimal getVoltage_mult() {
		return voltage_mult;
	}

	public void setVoltage_mult(BigDecimal voltage_mult) {
		this.voltage_mult = voltage_mult;
	}

	public BigDecimal getCurrent_mult() {
		return current_mult;
	}

	public void setCurrent_mult(BigDecimal current_mult) {
		this.current_mult = current_mult;
	} 
    

}
