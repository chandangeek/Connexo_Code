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
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author jsm
 */
public class ActualService extends AbstractTable { 
    /*	
     * 
    Actual Service (corresponds to Class 51


	After the meter detects and locks the service this table holds the parameters from MT-57 (service voltage) 
	and MT-58 (service current) that apply to the detected service. This table can also be written to "pre-lock" 
	the service. Only the 1st 5 bytes of MT-51 can be written; the remainder of MT-51 is copied from or calculated 
	from information in the MT-57 and MT-58 entries for the locked service. To manually lock a service via 
	communications, write the service parameters to MT-51 to set the parameters and set Locked_Status = 1. In 
	manufacturing, this table can be written to unlock the service prior to shipment. When MT-51 is written and 
	the service is unlocked, the meter will not automatically attempt to determine the service. Per the requirements 
	document, the meter is required to determine the service after a data altering communication session. Service 
	handler treats write to MT-57 and MT-58 and MP-9 as data altering communication sessions. When the communication 
	session is terminated after one of these writes, the meter will attempt to determine the service is configured 
	to do so.


	SPECIAL_PROPERTIES STORAGE 1 
	READ_ACCESS_PW 4 
	WRITE_ACCESS_PW 16 
    */    
    
	private BigDecimal def_absolute_min_current_threshold;
	private int service_voltage_index;
	private int service_current_index;
	private boolean locked_status;
	private int rotation; //0=CBA, 1=ABC
	private int serviceType;
	private int specialExtention;
	private int meterElements;
	private int phaseARatio;
	private int phaseBRatio;
	private int phaseCRatio;
	private int b_to_a_voltage_angle;
	private int c_to_a_voltage_angle;
	private int calculated_pi_threshold; //TODO: DSP_INT16
	private int nominal_service_voltage;
	private int phase_a_nominal_voltage;
	private int phase_b_nominal_voltage;
	private int phase_c_nominal_voltage;
	private int phase_a_service_current_test_ctrl; //TODO: BFLD 8
	private int phase_b_service_current_test_ctrl; //TODO: BFLD 8
	private int phase_c_service_current_test_ctrl; //TODO: BFLD 8
	private int absolute_min_current;
	private int phase_a_min_current;
	private int phase_b_min_current;
	private int phase_c_min_current;
	private int absolute_max_current;
	private int phase_a_min_lagging_pf;
	private int phase_a_min_leading_pf;
	private int phase_b_min_lagging_pf;
	private int phase_b_min_leading_pf;
	private int phase_c_min_lagging_pf;
	private int phase_c_min_leading_pf;
	
    
    /** Creates a new instance of ActualService */
    public ActualService(TableFactory tableFactory) {
        super(tableFactory,new TableIdentification(51,true));
    }
    
    public static void main(String [] args) throws Exception {
    	ActualService a = new ActualService(null);
    	byte[] tableData = {
    			0x0D, (byte) 0x80, 
    			0x02, 0x02, 
    			0x03, 
    			(byte) 0xC1, 0x24, 
    			0x00, 
    			0x00, 
    			0x39, 0x0B, (byte) 0xB0, 0x04, (byte) 0xB0, 0x04, (byte) 0xB0, 0x04, (byte) 0xB0, 0x04, 0x1F, 0x1F, 0x1F, 0x0D, (byte) 0x80, (byte) 0xAE, 0x19, (byte) 0xAE, 0x19, (byte) 0xAE, 0x19, 0x31, 0x1C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    	};
		a.parse(tableData );
		
		a.calc();
    }
 
    private void calc() {
//    	0 - Read MT51 & verify service is 3-wire delta - ERROR is anything other than 3WD
//    	1 - From MT51, determine if phase rotation is ABC or CBA
//    	2 - Read MT55 to get multiplier values to convert raw Voltage, Current & Energy values from MT71
//    	Do following steps on each read of MT71
//    	3 - Read MT71 and convert raw values with MT55 multipliers to engineering units
    	
    	double voltA = 480.5;
    	double currA = 10.0;
    	double wattA = 4769.9357570954;
    	double varA = -579.4289198713;
    	double voltC = 482.3;
    	double currC = 9.0;
    	double wattC = 2893.5071;
    	double varC = 3235.6287316055;
    	double voltAC = 479.2;
    	int rotation = 0;

//    	4 - Use Wa and VARa to determine Phase angle of Ia with respect to Vab
//			=MOD(DEGREES(ATAN(VARa/Wa))+IF(Wa<0,180,0),360)
    	double pA = Math.toDegrees(Math.atan(varA/wattA));
    	if (wattA<0) {
    		pA += 180;
    	}    	
    	if (pA<0) {
    		pA = pA+360;
    	}
    	System.out.println("pA " + pA);

//    	4a - Use Ia magnitude & phase angle to get real and imaginary components of Ia
//			Ia_real=Ia*COS(RADIANS(Pa))
//    		Ia_imag=Ia*SIN(RADIANS(Pa))
    	double currA_real = currA * Math.cos(Math.toRadians(pA));
    	double currA_imag = currA * Math.sin(Math.toRadians(pA));
    	System.out.println("currA_real " + currA_real);
    	System.out.println("currA_imag " + currA_imag);

//
//    	5 - use three line to line voltage magnitudes to determine the actual angle of Vcb to Vab
//    	6 - use phase rotation or nominal angle Vcb to Vab to determine the sign of the angle from step 2
//			PVac=
    	int mult = 1;
    	if (rotation==1) {
    		mult = -1;
    	}
    	double pvCA = 360+(mult*Math.toDegrees((Math.acos((Math.pow(voltA,2)+Math.pow(voltC,2)-Math.pow(voltAC,2))/(2*voltA*voltC))))); 
    	pvCA = pvCA % 360;
    	System.out.println("pvCA " + pvCA);

//    	7 - Use Wc and VARc to determine Phase angle of Ic with respect to Vcb
//			Pc=MOD(DEGREES(ATAN(VARc/Wc))+IF(Wc<0,180,0),360)
    	double pC = Math.toDegrees(Math.atan(varC/wattC));
    	if (wattC<0) {
    		pC += 180;
    	}    	
    	if (pC<0) {
    		pC = pC+360;
    	}
    	System.out.println("pC " + pC);

//    	8 - Add the angle from step 4 to the resultant angle from steps 2 & 3 to get the angle of Ic to Vab
//			Pca=MOD(Pc+PVca,360)
    	double pCA=(pC+pvCA)%360;
    	System.out.println("pCA " + pCA);
//    	8a - Use Ic magnitude & phase angle to get real and imaginary components of Ic
//			Ic_real=Ic*COS(RADIANS(Pca))
//    		Ic_imag=Ic*SIN(RADIANS(Pca))
    	double currC_real = currC * Math.cos(Math.toRadians(pCA));
    	double currC_imag = currC * Math.sin(Math.toRadians(pCA));
    	System.out.println("currC_real " + currC_real);
    	System.out.println("currC_imag " + currC_imag);

//
//    	9 - Negate the sum of the real components of Ia and Ic to get the real component of Ib
//			Ib_real=-(Ia_real+Ic_real)
    	double currB_real = -1 * (currA_real+currC_real);
    	System.out.println("currB_real " + currB_real);

//    	10 - Negate the sum of the imaginary components of Ia and Ic to get the imaginary component of Ib
//			Ib_imag=-(Ia_imag+Ic_imag)
    	double currB_imag = -1 * (currA_imag+currC_imag);
    	System.out.println("currB_imag " + currB_imag);

//    	11 - Use the real and imaginary components of Ib to calculate a magnitude and phase angle for Ib
//			Ib_mag=(Ib_real^2+Ib_imag^2)^0.5
//    		Pba=MOD(DEGREES(ATAN(Ib_imag/Ib_real))+IF(Ib_real<0,180,0),360)
    	double currB =  Math.sqrt(Math.pow(currB_real, 2)+Math.pow(currB_imag, 2));
    	System.out.println("currB " + currB);
    	double pBA = Math.toDegrees(Math.atan(currB_imag/currB_real));
    	if (currB_real < 0 ) {
    		pBA += 180;
    	}
    	pBA = pBA % 360;
    	System.out.println("pBA " + pBA);

//
//    	12 - When summing like currents for a given phase, it needs to be a vector sum to get a true total
//    	12a - Sum real components of all currents of a given phase
//			Ia_real_total=Ia_real_1+Ia_real2+Ia_real3+Ia_real4+Ia_real5

//    	12b - Sum imaginary components of all  currents of a given phase
//			Ia_imag_total=Ia_imag_1+Ia_imag2+Ia_imag3+Ia_imag4+Ia_imag5

//    	12c - Use the total real and total imaginary components to calculate a total current magnitude for the given phase
//	    	Ia_rms_total=(Ia_real_total^2+Ia_imag_total^2)^0.5


		
	}

	protected void parse(byte[] tableData) throws IOException {
//        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        int temp = ProtocolUtils.getIntLE(tableData, offset, 2);
        offset+=2;
        setService_voltage_index(ProtocolUtils.getIntLE(tableData, offset++, 1));
        setService_current_index(ProtocolUtils.getIntLE(tableData, offset++, 1));
        int lockedServiceStatusBFD = ProtocolUtils.getIntLE(tableData, offset++, 1);
        setLocked_status((lockedServiceStatusBFD&0x01)==0x01);
        setRotation((lockedServiceStatusBFD&0x02)>>1);
        int serviceDefinitionBFD = ProtocolUtils.getIntLE(tableData, offset, 2);
        setServiceType(serviceDefinitionBFD&0x07);
        setSpecialExtention((serviceDefinitionBFD&0x18)>>3);
        setMeterElements((serviceDefinitionBFD&0x60)>>5);
        setPhaseARatio((serviceDefinitionBFD&0x380)>>7);
        setPhaseBRatio((serviceDefinitionBFD&0x1C00)>>10);
        setPhaseCRatio((serviceDefinitionBFD&0xE000)>>13);
        offset+=2;

        //        setNrOfRings(C12ParseUtils.getInt(tableData,offset++));
//        setNrOfRingsOutside(C12ParseUtils.getInt(tableData,offset++));
//        setWindows(new Window[2]);
//        for (int i=0;i<getWindows().length;i++) {
//            getWindows()[i] = new Window(tableData, offset, getTableFactory());
//            offset+=Window.getSize(getTableFactory());
//        }
    }

	@Override
	public String toString() {
		return "ActualService [def_absolute_min_current_threshold="
				+ def_absolute_min_current_threshold
				+ ", service_voltage_index=" + service_voltage_index
				+ ", service_current_index=" + service_current_index
				+ ", locked_status=" + locked_status + ", rotation=" + rotation
				+ ", serviceType=" + serviceType + ", specialExtention="
				+ specialExtention + ", meterElements=" + meterElements
				+ ", phaseARatio=" + phaseARatio + ", phaseBRatio="
				+ phaseBRatio + ", phaseCRatio=" + phaseCRatio
				+ ", b_to_a_voltage_angle=" + b_to_a_voltage_angle
				+ ", c_to_a_voltage_angle=" + c_to_a_voltage_angle
				+ ", calculated_pi_threshold=" + calculated_pi_threshold
				+ ", nominal_service_voltage=" + nominal_service_voltage
				+ ", phase_a_nominal_voltage=" + phase_a_nominal_voltage
				+ ", phase_b_nominal_voltage=" + phase_b_nominal_voltage
				+ ", phase_c_nominal_voltage=" + phase_c_nominal_voltage
				+ ", phase_a_service_current_test_ctrl="
				+ phase_a_service_current_test_ctrl
				+ ", phase_b_service_current_test_ctrl="
				+ phase_b_service_current_test_ctrl
				+ ", phase_c_service_current_test_ctrl="
				+ phase_c_service_current_test_ctrl + ", absolute_min_current="
				+ absolute_min_current + ", phase_a_min_current="
				+ phase_a_min_current + ", phase_b_min_current="
				+ phase_b_min_current + ", phase_c_min_current="
				+ phase_c_min_current + ", absolute_max_current="
				+ absolute_max_current + ", phase_a_min_lagging_pf="
				+ phase_a_min_lagging_pf + ", phase_a_min_leading_pf="
				+ phase_a_min_leading_pf + ", phase_b_min_lagging_pf="
				+ phase_b_min_lagging_pf + ", phase_b_min_leading_pf="
				+ phase_b_min_leading_pf + ", phase_c_min_lagging_pf="
				+ phase_c_min_lagging_pf + ", phase_c_min_leading_pf="
				+ phase_c_min_leading_pf + "]";
	}

	public BigDecimal getDef_absolute_min_current_threshold() {
		return def_absolute_min_current_threshold;
	}

	public void setDef_absolute_min_current_threshold(
			BigDecimal def_absolute_min_current_threshold) {
		this.def_absolute_min_current_threshold = def_absolute_min_current_threshold;
	}

	public int getService_voltage_index() {
		return service_voltage_index;
	}

	public void setService_voltage_index(int service_voltage_index) {
		this.service_voltage_index = service_voltage_index;
	}

	public int getService_current_index() {
		return service_current_index;
	}

	public void setService_current_index(int service_current_index) {
		this.service_current_index = service_current_index;
	}

	public boolean getLocked_status() {
		return locked_status;
	}

	public void setLocked_status(boolean locked_status) {
		this.locked_status = locked_status;
	}
	
	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getServiceType() {
		return serviceType;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

	public int getSpecialExtention() {
		return specialExtention;
	}

	public void setSpecialExtention(int specialExtention) {
		this.specialExtention = specialExtention;
	}

	public int getMeterElements() {
		return meterElements;
	}

	public void setMeterElements(int meterElements) {
		this.meterElements = meterElements;
	}

	public int getPhaseARatio() {
		return phaseARatio;
	}

	public void setPhaseARatio(int phaseARatio) {
		this.phaseARatio = phaseARatio;
	}

	public int getPhaseBRatio() {
		return phaseBRatio;
	}

	public void setPhaseBRatio(int phaseBRatio) {
		this.phaseBRatio = phaseBRatio;
	}

	public int getPhaseCRatio() {
		return phaseCRatio;
	}

	public void setPhaseCRatio(int phaseCRatio) {
		this.phaseCRatio = phaseCRatio;
	}

	public int getB_to_a_voltage_angle() {
		return b_to_a_voltage_angle;
	}

	public void setB_to_a_voltage_angle(int b_to_a_voltage_angle) {
		this.b_to_a_voltage_angle = b_to_a_voltage_angle;
	}

	public int getC_to_a_voltage_angle() {
		return c_to_a_voltage_angle;
	}

	public void setC_to_a_voltage_angle(int c_to_a_voltage_angle) {
		this.c_to_a_voltage_angle = c_to_a_voltage_angle;
	}

	public int getCalculated_pi_threshold() {
		return calculated_pi_threshold;
	}

	public void setCalculated_pi_threshold(int calculated_pi_threshold) {
		this.calculated_pi_threshold = calculated_pi_threshold;
	}

	public int getNominal_service_voltage() {
		return nominal_service_voltage;
	}

	public void setNominal_service_voltage(int nominal_service_voltage) {
		this.nominal_service_voltage = nominal_service_voltage;
	}

	public int getPhase_a_nominal_voltage() {
		return phase_a_nominal_voltage;
	}

	public void setPhase_a_nominal_voltage(int phase_a_nominal_voltage) {
		this.phase_a_nominal_voltage = phase_a_nominal_voltage;
	}

	public int getPhase_b_nominal_voltage() {
		return phase_b_nominal_voltage;
	}

	public void setPhase_b_nominal_voltage(int phase_b_nominal_voltage) {
		this.phase_b_nominal_voltage = phase_b_nominal_voltage;
	}

	public int getPhase_c_nominal_voltage() {
		return phase_c_nominal_voltage;
	}

	public void setPhase_c_nominal_voltage(int phase_c_nominal_voltage) {
		this.phase_c_nominal_voltage = phase_c_nominal_voltage;
	}

	public int getPhase_a_service_current_test_ctrl() {
		return phase_a_service_current_test_ctrl;
	}

	public void setPhase_a_service_current_test_ctrl(
			int phase_a_service_current_test_ctrl) {
		this.phase_a_service_current_test_ctrl = phase_a_service_current_test_ctrl;
	}

	public int getPhase_b_service_current_test_ctrl() {
		return phase_b_service_current_test_ctrl;
	}

	public void setPhase_b_service_current_test_ctrl(
			int phase_b_service_current_test_ctrl) {
		this.phase_b_service_current_test_ctrl = phase_b_service_current_test_ctrl;
	}

	public int getPhase_c_service_current_test_ctrl() {
		return phase_c_service_current_test_ctrl;
	}

	public void setPhase_c_service_current_test_ctrl(
			int phase_c_service_current_test_ctrl) {
		this.phase_c_service_current_test_ctrl = phase_c_service_current_test_ctrl;
	}

	public int getAbsolute_min_current() {
		return absolute_min_current;
	}

	public void setAbsolute_min_current(int absolute_min_current) {
		this.absolute_min_current = absolute_min_current;
	}

	public int getPhase_a_min_current() {
		return phase_a_min_current;
	}

	public void setPhase_a_min_current(int phase_a_min_current) {
		this.phase_a_min_current = phase_a_min_current;
	}

	public int getPhase_b_min_current() {
		return phase_b_min_current;
	}

	public void setPhase_b_min_current(int phase_b_min_current) {
		this.phase_b_min_current = phase_b_min_current;
	}

	public int getPhase_c_min_current() {
		return phase_c_min_current;
	}

	public void setPhase_c_min_current(int phase_c_min_current) {
		this.phase_c_min_current = phase_c_min_current;
	}

	public int getAbsolute_max_current() {
		return absolute_max_current;
	}

	public void setAbsolute_max_current(int absolute_max_current) {
		this.absolute_max_current = absolute_max_current;
	}

	public int getPhase_a_min_lagging_pf() {
		return phase_a_min_lagging_pf;
	}

	public void setPhase_a_min_lagging_pf(int phase_a_min_lagging_pf) {
		this.phase_a_min_lagging_pf = phase_a_min_lagging_pf;
	}

	public int getPhase_a_min_leading_pf() {
		return phase_a_min_leading_pf;
	}

	public void setPhase_a_min_leading_pf(int phase_a_min_leading_pf) {
		this.phase_a_min_leading_pf = phase_a_min_leading_pf;
	}

	public int getPhase_b_min_lagging_pf() {
		return phase_b_min_lagging_pf;
	}

	public void setPhase_b_min_lagging_pf(int phase_b_min_lagging_pf) {
		this.phase_b_min_lagging_pf = phase_b_min_lagging_pf;
	}

	public int getPhase_b_min_leading_pf() {
		return phase_b_min_leading_pf;
	}

	public void setPhase_b_min_leading_pf(int phase_b_min_leading_pf) {
		this.phase_b_min_leading_pf = phase_b_min_leading_pf;
	}

	public int getPhase_c_min_lagging_pf() {
		return phase_c_min_lagging_pf;
	}

	public void setPhase_c_min_lagging_pf(int phase_c_min_lagging_pf) {
		this.phase_c_min_lagging_pf = phase_c_min_lagging_pf;
	}

	public int getPhase_c_min_leading_pf() {
		return phase_c_min_leading_pf;
	}

	public void setPhase_c_min_leading_pf(int phase_c_min_leading_pf) {
		this.phase_c_min_leading_pf = phase_c_min_leading_pf;
	} 

}
