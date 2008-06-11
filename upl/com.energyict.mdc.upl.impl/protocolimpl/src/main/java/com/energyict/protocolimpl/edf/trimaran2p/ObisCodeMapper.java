/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.trimaran2p.core.ArreteJournalier;
import com.energyict.protocolimpl.edf.trimaran2p.core.Energies;
import com.energyict.protocolimpl.edf.trimaran2p.core.Programmables;
import com.energyict.protocolimpl.edf.trimaran2p.core.TempsFonctionnement;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import com.energyict.protocolimpl.edf.trimarandlms.common.RegisterNameFactory;

/**
 * @author gna
 *
 */
public class ObisCodeMapper {
	
	Trimaran2P trimaran2P;

	/**
	 * 
	 */
	public ObisCodeMapper() {
	}

	public ObisCodeMapper(Trimaran2P trimaran2P) {
		this.trimaran2P = trimaran2P;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(RegisterNameFactory.findObisCode(obisCode));
    }

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		
		Quantity quan = null;
		Date toTime = new Date();
		Calendar tempCalendar = Calendar.getInstance(getTrimaran2P().getTimeZone());
		Register register = getTrimaran2P().getRegisterFactory().findRegister(obisCode);
		if(obisCode.getD() == 8){
			if(register.getVariableName().isENERGIE()){
				if(obisCode.getF() == 255){
					Energies energie = getTrimaran2P().getTrimaranObjectFactory().readEnergieIndex().getEnergie(register.getVariableName().getCode());
					toTime.setTime(energie.getDernierHoroDate().getCalendar().getTimeInMillis());
					switch(obisCode.getC()){
					case 1:{quan = energie.getIxNRJact(0).add(energie.getNRJact_Reste(0));};break;
					case 2:{quan = energie.getIxNRJact(1).add(energie.getNRJact_Reste(1));};break;
					case 5:{quan = energie.getIxNRJind(0).add(energie.getNRJind_Reste(0));};break;
					case 6:{quan = energie.getIxNRJcap(0).add(energie.getNRJcap_Reste(0));};break;
					case 7:{quan = energie.getIxNRJind(1).add(energie.getNRJind_Reste(1));};break;
					case 8:{quan = energie.getIxNRJcap(1).add(energie.getNRJcap_Reste(1));};break;
					
					case 140:{quan = energie.getIxNRJact(0).add(energie.getNRJact_Reste(0));};break;
					case 141:{quan = energie.getIxNRJact(1).add(energie.getNRJact_Reste(1));};break;
					case 142:{quan = energie.getIxNRJind(0).add(energie.getNRJind_Reste(0));};break;
					case 143:{quan = energie.getIxNRJcap(0).add(energie.getNRJcap_Reste(0));};break;
					case 144:{quan = energie.getIxNRJind(1).add(energie.getNRJind_Reste(1));};break;
					case 145:{quan = energie.getIxNRJcap(1).add(energie.getNRJcap_Reste(1));};break;
					
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, toTime);
				}
			}
			
			else if(register.getVariableName().isARRETE_JOURNALIER()){
				ArreteJournalier arreteJournalier = getTrimaran2P().getTrimaranObjectFactory().readArreteJournalier();
				tempCalendar.setTime(arreteJournalier.getDernierHoroDate().getCalendar().getTime());
				tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
				tempCalendar.set(Calendar.MINUTE, 0);
				tempCalendar.set(Calendar.SECOND, 0);
				tempCalendar.set(Calendar.MILLISECOND, 0);
				toTime.setTime(arreteJournalier.getDernierHoroDate().getCalendar().getTimeInMillis());
				if(Math.abs(obisCode.getF()) == 0){
					switch(obisCode.getC()){
					case 1:{quan = arreteJournalier.getIxJour(0);};break;
					case 2:{quan = arreteJournalier.getIxJour(1);};break;
					case 5:{quan = arreteJournalier.getIxJour(2);};break;
					case 6:{quan = arreteJournalier.getIxJour(4);};break;
					case 7:{quan = arreteJournalier.getIxJour(3);};break;
					case 8:{quan = arreteJournalier.getIxJour(5);};break;
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, tempCalendar.getTime(), toTime);
				}
				
				else if(Math.abs(obisCode.getF()) == 1){
//					arreteJournalier = getTrimaran2P().getTrimaranObjectFactory().readArreteJournalier();
					toTime.setTime(tempCalendar.getTimeInMillis());
					tempCalendar.add(Calendar.DAY_OF_MONTH, -1);
					switch(obisCode.getC()){
					case 1:{quan = arreteJournalier.getIxJourmoins1(0);};break;
					case 2:{quan = arreteJournalier.getIxJourmoins1(1);};break;
					case 5:{quan = arreteJournalier.getIxJourmoins1(2);};break;
					case 6:{quan = arreteJournalier.getIxJourmoins1(4);};break;
					case 7:{quan = arreteJournalier.getIxJourmoins1(3);};break;
					case 8:{quan = arreteJournalier.getIxJourmoins1(5);};break;
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, tempCalendar.getTime(), toTime);
				}
				
				else if(Math.abs(obisCode.getF()) == 2){
//					arreteJournalier = getTrimaran2P().getTrimaranObjectFactory().readArreteJournalier();
					tempCalendar.add(Calendar.DAY_OF_MONTH, -1);
					toTime.setTime(tempCalendar.getTimeInMillis());
					tempCalendar.add(Calendar.DAY_OF_MONTH, -1);
					switch(obisCode.getC()){
					case 1:{quan = arreteJournalier.getIxJourmoins2(0);};break;
					case 2:{quan = arreteJournalier.getIxJourmoins2(1);};break;
					case 5:{quan = arreteJournalier.getIxJourmoins2(2);};break;
					case 6:{quan = arreteJournalier.getIxJourmoins2(4);};break;
					case 7:{quan = arreteJournalier.getIxJourmoins2(3);};break;
					case 8:{quan = arreteJournalier.getIxJourmoins2(5);};break;
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, tempCalendar.getTime(), toTime);
				}
				
				else if(Math.abs(obisCode.getF()) == 3){
//					arreteJournalier = getTrimaran2P().getTrimaranObjectFactory().readArreteJournalier();
					tempCalendar.add(Calendar.DAY_OF_MONTH, -2);
					toTime.setTime(tempCalendar.getTimeInMillis());
					tempCalendar.add(Calendar.DAY_OF_MONTH, -1);
					switch(obisCode.getC()){
					case 1:{quan = arreteJournalier.getIxJourmoins3(0);};break;
					case 2:{quan = arreteJournalier.getIxJourmoins3(1);};break;
					case 5:{quan = arreteJournalier.getIxJourmoins3(2);};break;
					case 6:{quan = arreteJournalier.getIxJourmoins3(4);};break;
					case 7:{quan = arreteJournalier.getIxJourmoins3(3);};break;
					case 8:{quan = arreteJournalier.getIxJourmoins3(5);};break;
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, tempCalendar.getTime(), toTime);
				}
			}
			
			else if(register.getVariableName().isARRETES_PROGRAMMABLES()){
				Programmables programmables = getTrimaran2P().getTrimaranObjectFactory().readProgrammablesIndex().getProgrammalbes(register.getVariableName().getCode());
				tempCalendar.setTime(programmables.getDebutPeriode().getCalendar().getTime());
				toTime.setTime(programmables.getDernierHorodate().getCalendar().getTimeInMillis());
				int periodicite = programmables.getNombre();
				if((Math.abs(obisCode.getF()) == 10) || (Math.abs(obisCode.getF()) == 20)){
					switch(obisCode.getC()){
					case 1:{quan = programmables.getIxProg(0);};break;
					case 2:{quan = programmables.getIxProg(1);};break;
					case 5:{quan = programmables.getIxProg(2);};break;
					case 6:{quan = programmables.getIxProg(4);};break;
					case 7:{quan = programmables.getIxProg(3);};break;
					case 8:{quan = programmables.getIxProg(5);};break;
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, tempCalendar.getTime(), toTime);
				}
				
				else if((Math.abs(obisCode.getF()) == 11) || (Math.abs(obisCode.getF()) == 21)){
					toTime.setTime(tempCalendar.getTimeInMillis());
					if(register.getVariableName().getCode() == 120)
						tempCalendar.add(Calendar.DAY_OF_MONTH, -periodicite);
					else 
						tempCalendar.add(Calendar.MONTH, -periodicite);
					switch(obisCode.getC()){
					case 1:{quan = programmables.getIxProgMoins1(0);};break;
					case 2:{quan = programmables.getIxProgMoins1(1);};break;
					case 5:{quan = programmables.getIxProgMoins1(2);};break;
					case 6:{quan = programmables.getIxProgMoins1(4);};break;
					case 7:{quan = programmables.getIxProgMoins1(3);};break;
					case 8:{quan = programmables.getIxProgMoins1(5);};break;
					default:{throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");}
					}
					return new RegisterValue(obisCode, quan, null, tempCalendar.getTime(), toTime);
				}
			}
			
			else if(register.getVariableName().isTEMPS_FONCTIONNEMENT()){
				if(obisCode.getF() == 255){
					TempsFonctionnement tempsFonctionnement = getTrimaran2P().getTrimaranObjectFactory().readTempsFonctionnement();
					toTime.setTime(tempsFonctionnement.getDernierHoroDate().getCalendar().getTimeInMillis());
					return new RegisterValue(obisCode, tempsFonctionnement.getTempsFonct(), null, toTime);
				}
				else 
					throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");
			}
		}
		throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " is not supported.");
	}

	/**
	 * @return the trimaran2P
	 */
	protected Trimaran2P getTrimaran2P() {
		return trimaran2P;
	}

}
