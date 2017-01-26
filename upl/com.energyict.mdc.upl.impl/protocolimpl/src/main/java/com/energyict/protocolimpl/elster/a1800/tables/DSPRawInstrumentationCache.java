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

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author jsm
 */
public class DSPRawInstrumentationCache extends AbstractTable { 
    /*	
     * 
	Raw DSP instrumentation cache table


	This table contains the instrumentation cache values in raw DSP format. The values in the table are 
	typically refreshed every 0.5s. This is a read only table


	SPECIAL_PROPERTIES STORAGE 2 
	READ_ACCESS_PW 4 
	WRITE_ACCESS_PW 32 
    */    
    
	
	private BigDecimal valid_tx_freq;
	private BigDecimal phase_a_var;
	private BigDecimal phase_a_v_rms;
	private BigDecimal phase_a_i_rms;
	private BigDecimal phase_a_w;
	private BigDecimal phase_a_va;
	private BigDecimal phase_b_var;
	private BigDecimal phase_b_v_rms;
	private BigDecimal phase_b_i_rms;
	private BigDecimal phase_b_w;
	private BigDecimal phase_b_va;
	private BigDecimal phase_c_var;
	private BigDecimal phase_c_v_rms;
	private BigDecimal phase_c_i_rms;
	private BigDecimal phase_c_w;
	private BigDecimal phase_c_va;
	private BigDecimal line_a_to_b_voltage;
	private BigDecimal line_b_to_c_voltage;
	private BigDecimal line_c_to_a_voltage;
	
    
    /** Creates a new instance of DSPRawInstrumentationCache */
    public DSPRawInstrumentationCache(TableFactory tableFactory) {
        super(tableFactory,new TableIdentification(71,true));
    }
    
    public static void main(String [] args) throws Exception {
    	DSPRawInstrumentationCache a = new DSPRawInstrumentationCache(null);
    	byte[] tableData = {
    			(byte)0x5d,(byte)0x6b,(byte)0x75,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x67,(byte)0x50,(byte)0x1c,(byte)0x1c,(byte)0x00,(byte)0x0e,(byte)0x0e,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x1c,(byte)0x4f,(byte)0x0f,(byte)0x1c,(byte)0x00,(byte)0x17,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x25,(byte)0x1e,(byte)0x1c,(byte)0x00,(byte)0x58,(byte)0x62,(byte)0x10,(byte)0x1c,(byte)0x00,(byte)0x70,(byte)0x2e,(byte)0x27,(byte)0x1c,(byte)0x00

    	};
		a.parse(tableData );
		System.out.println(a);
		//
		
		System.out.println((a.getValid_tx_freq()));//0.01500
		
    }
 
    protected void parse(byte[] tableData) throws IOException {
//        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setValid_tx_freq(parseDSP(tableData, offset));
        offset+=5;
        setPhase_a_var(parseDSP(tableData, offset));
        offset+=5;
        setPhase_a_v_rms(parseDSP(tableData, offset));
        offset+=5;
        setPhase_a_i_rms(parseDSP(tableData, offset));
        offset+=5;
        setPhase_a_w(parseDSP(tableData, offset));
        offset+=5;
        setPhase_a_va(parseDSP(tableData, offset));
        offset+=5;
        setPhase_b_var(parseDSP(tableData, offset));
        offset+=5;
        setPhase_b_v_rms(parseDSP(tableData, offset));
        offset+=5;
        setPhase_b_i_rms(parseDSP(tableData, offset));
        offset+=5;
        setPhase_b_w(parseDSP(tableData, offset));
        offset+=5;
        setPhase_b_va(parseDSP(tableData, offset));
        offset+=5;
        setPhase_c_var(parseDSP(tableData, offset));
        offset+=5;
        setPhase_c_v_rms(parseDSP(tableData, offset));
        offset+=5;
        setPhase_c_i_rms(parseDSP(tableData, offset));
        offset+=5;
        setPhase_c_w(parseDSP(tableData, offset));
        offset+=5;
        setPhase_c_va(parseDSP(tableData, offset));
        offset+=5;
        setLine_a_to_b_voltage(parseDSP(tableData, offset));
        offset+=5;
        setLine_b_to_c_voltage(parseDSP(tableData, offset));
        offset+=5;
        setLine_c_to_a_voltage(parseDSP(tableData, offset));
        offset+=5;
//        setNrOfRings(C12ParseUtils.getInt(tableData,offset++));
//        setNrOfRingsOutside(C12ParseUtils.getInt(tableData,offset++));
//        setWindows(new Window[2]);
//        for (int i=0;i<getWindows().length;i++) {
//            getWindows()[i] = new Window(tableData, offset, getTableFactory());
//            offset+=Window.getSize(getTableFactory());
//        }
    }

	private BigDecimal parseDSP(byte[] byteBuffer, int offset) throws IOException {
		 long intval = 0;
	            for (int i = 0; i < 5; i++) {
	            	long temp;
	            	if (i<4) {
	            		temp = (long) (byteBuffer[offset + i] & 0x7F);
	            	} else {
	            		temp = (long) (byteBuffer[offset + i] );
	            	}
	            	intval += temp  * Math.pow(128, i);
	            }
	        return new BigDecimal(intval/Math.pow(128, 4));
	}

	@Override
	public String toString() {
		return "DSPRawInstrumentationCache [valid_tx_freq=" + valid_tx_freq
				+ ", phase_a_var=" + phase_a_var + ", phase_a_v_rms="
				+ phase_a_v_rms + ", phase_a_i_rms=" + phase_a_i_rms
				+ ", phase_a_w=" + phase_a_w + ", phase_a_va=" + phase_a_va
				+ ", phase_b_var=" + phase_b_var + ", phase_b_v_rms="
				+ phase_b_v_rms + ", phase_b_i_rms=" + phase_b_i_rms
				+ ", phase_b_w=" + phase_b_w + ", phase_b_va=" + phase_b_va
				+ ", phase_c_var=" + phase_c_var + ", phase_c_v_rms="
				+ phase_c_v_rms + ", phase_c_i_rms=" + phase_c_i_rms
				+ ", phase_c_w=" + phase_c_w + ", phase_c_va=" + phase_c_va
				+ ", line_a_to_b_voltage=" + line_a_to_b_voltage
				+ ", line_b_to_c_voltage=" + line_b_to_c_voltage
				+ ", line_c_to_a_voltage=" + line_c_to_a_voltage + "]";
	}

	public BigDecimal getValid_tx_freq() {
		return valid_tx_freq;
	}

	public void setValid_tx_freq(BigDecimal valid_tx_freq) {
		this.valid_tx_freq = valid_tx_freq;
	}

	public BigDecimal getPhase_a_var() {
		return phase_a_var;
	}

	public void setPhase_a_var(BigDecimal phase_a_var) {
		this.phase_a_var = phase_a_var;
	}

	public BigDecimal getPhase_a_v_rms() {
		return phase_a_v_rms;
	}

	public void setPhase_a_v_rms(BigDecimal phase_a_v_rms) {
		this.phase_a_v_rms = phase_a_v_rms;
	}

	public BigDecimal getPhase_a_i_rms() {
		return phase_a_i_rms;
	}

	public void setPhase_a_i_rms(BigDecimal phase_a_i_rms) {
		this.phase_a_i_rms = phase_a_i_rms;
	}

	public BigDecimal getPhase_a_w() {
		return phase_a_w;
	}

	public void setPhase_a_w(BigDecimal phase_a_w) {
		this.phase_a_w = phase_a_w;
	}

	public BigDecimal getPhase_a_va() {
		return phase_a_va;
	}

	public void setPhase_a_va(BigDecimal phase_a_va) {
		this.phase_a_va = phase_a_va;
	}

	public BigDecimal getPhase_b_var() {
		return phase_b_var;
	}

	public void setPhase_b_var(BigDecimal phase_b_var) {
		this.phase_b_var = phase_b_var;
	}

	public BigDecimal getPhase_b_v_rms() {
		return phase_b_v_rms;
	}

	public void setPhase_b_v_rms(BigDecimal phase_b_v_rms) {
		this.phase_b_v_rms = phase_b_v_rms;
	}

	public BigDecimal getPhase_b_i_rms() {
		return phase_b_i_rms;
	}

	public void setPhase_b_i_rms(BigDecimal phase_b_i_rms) {
		this.phase_b_i_rms = phase_b_i_rms;
	}

	public BigDecimal getPhase_b_w() {
		return phase_b_w;
	}

	public void setPhase_b_w(BigDecimal phase_b_w) {
		this.phase_b_w = phase_b_w;
	}

	public BigDecimal getPhase_b_va() {
		return phase_b_va;
	}

	public void setPhase_b_va(BigDecimal phase_b_va) {
		this.phase_b_va = phase_b_va;
	}

	public BigDecimal getPhase_c_var() {
		return phase_c_var;
	}

	public void setPhase_c_var(BigDecimal phase_c_var) {
		this.phase_c_var = phase_c_var;
	}

	public BigDecimal getPhase_c_v_rms() {
		return phase_c_v_rms;
	}

	public void setPhase_c_v_rms(BigDecimal phase_c_v_rms) {
		this.phase_c_v_rms = phase_c_v_rms;
	}

	public BigDecimal getPhase_c_i_rms() {
		return phase_c_i_rms;
	}

	public void setPhase_c_i_rms(BigDecimal phase_c_i_rms) {
		this.phase_c_i_rms = phase_c_i_rms;
	}

	public BigDecimal getPhase_c_w() {
		return phase_c_w;
	}

	public void setPhase_c_w(BigDecimal phase_c_w) {
		this.phase_c_w = phase_c_w;
	}

	public BigDecimal getPhase_c_va() {
		return phase_c_va;
	}

	public void setPhase_c_va(BigDecimal phase_c_va) {
		this.phase_c_va = phase_c_va;
	}

	public BigDecimal getLine_a_to_b_voltage() {
		return line_a_to_b_voltage;
	}

	public void setLine_a_to_b_voltage(BigDecimal line_a_to_b_voltage) {
		this.line_a_to_b_voltage = line_a_to_b_voltage;
	}

	public BigDecimal getLine_b_to_c_voltage() {
		return line_b_to_c_voltage;
	}

	public void setLine_b_to_c_voltage(BigDecimal line_b_to_c_voltage) {
		this.line_b_to_c_voltage = line_b_to_c_voltage;
	}

	public BigDecimal getLine_c_to_a_voltage() {
		return line_c_to_a_voltage;
	}

	public void setLine_c_to_a_voltage(BigDecimal line_c_to_a_voltage) {
		this.line_c_to_a_voltage = line_c_to_a_voltage;
	} 

}
